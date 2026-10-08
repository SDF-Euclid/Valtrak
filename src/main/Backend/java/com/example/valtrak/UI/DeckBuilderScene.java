package com.example.valtrak.UI;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.GameData.Config.DeckRules;
import com.example.valtrak.Data.GameData.DataTransfer.DeckData.DeckDtos.DeckDto;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import com.example.valtrak.UI.components.CardTile;
import com.example.valtrak.UI.net.AccountSession;
import com.example.valtrak.UI.net.ServerApi;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import javafx.stage.Stage;

import java.util.*;

public class DeckBuilderScene {

    private static final String BG     = "#1a1a2e";
    private static final String PANEL  = "#16213e";
    private static final String ACCENT = "#e8b84b";
    private static final String TEXT   = "#d4d4d4";
    private static final String DIM    = "#555555";

    private static final int MAX_DECK   = DeckRules.MAX_DECK_SIZE;
    private static final int MAX_COPIES = DeckRules.MAX_COPIES;

    private enum Filter { ALL, FAVORITES, VEHICLES, ITEMS }

    private enum Grouping {
        CATEGORY("Category"), RARITY("Rarity"), NATION("Nation"), NONE("None");
        final String label;
        Grouping(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final Stage stage;
    private final List<CardDto> cards;
    /** The signed-in player's favorite card ids; null when playing as a guest. */
    private final Set<Long> favorites;

    private final Map<Long, Integer> deckCounts = new LinkedHashMap<>();
    private final Map<Long, String>  deckNames  = new LinkedHashMap<>();

    private Filter currentFilter = Filter.ALL;
    private Grouping currentGrouping = Grouping.CATEGORY;
    private VBox grid;
    private Label hint;
    private PauseTransition noticeReset;
    private Label deckCountLabel;
    private VBox  deckListBox;

    /** Saved decks of the signed-in player; null for guests. */
    private final List<DeckDto> savedDecks;
    private final Map<Long, CardDto> cardsById = new HashMap<>();
    private Long currentDeckId;          // null = a deck that hasn't been saved yet
    private boolean dirty;
    private boolean loading;             // true while we fill the editor programmatically
    private TextField deckNameField;
    private ComboBox<DeckDto> deckPicker;
    private Label deckStatus;
    private Label problemLabel;
    private Button saveBtn;
    private Button deleteBtn;

    public DeckBuilderScene(Stage stage, List<CardDto> catalog, Set<Long> favoriteIds, List<DeckDto> decks) {
        this.stage = stage;
        this.favorites = favoriteIds;
        this.savedDecks = decks;
        for (CardDto c : catalog) cardsById.put(c.id(), c);
        List<CardDto> all = new ArrayList<>(catalog);
        // vehicles first, then items; within each group by rarity, then name
        all.sort(Comparator
                .comparingInt((CardDto c) -> isVehicle(c) ? 0 : 1)
                .thenComparingInt(DeckBuilderScene::rarityOrder)
                .thenComparing(CardDto::name));
        this.cards = all;
    }

    public Scene build() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG + ";");
        root.setTop(buildTopBar());

        SplitPane split = new SplitPane(buildLibrary(), buildDeckPanel());
        split.setDividerPositions(0.72);
        split.setStyle("-fx-background-color: " + BG + "; -fx-box-border: transparent;");
        root.setCenter(split);

        return new Scene(root, 1100, 700);
    }

    // ── Top bar ───────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        Button backBtn = new Button("← BACK");
        backBtn.setStyle(btnStyle(false));
        backBtn.setOnMouseEntered(e -> backBtn.setStyle(btnStyle(true)));
        backBtn.setOnMouseExited(e -> backBtn.setStyle(btnStyle(false)));
        backBtn.setOnAction(e -> {
            if (!confirmDiscard()) return;
            stage.setResizable(false);
            stage.setScene(new MainMenuScene(stage).build());
            stage.sizeToScene();
            stage.centerOnScreen();
        });

        Label title = new Label("DECK BUILDER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ACCENT));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        hint = new Label(defaultHint());
        hint.setFont(Font.font("Arial", 11));
        hint.setTextFill(Color.web(DIM));

        HBox bar = new HBox(16, backBtn, title, spacer, hint);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setStyle("-fx-background-color: #0d0d1a; -fx-border-color: " + ACCENT
                + "; -fx-border-width: 0 0 1 0;");
        return bar;
    }

    // ── Card library ──────────────────────────────────────────────────────────

    private ScrollPane buildLibrary() {
        ToggleGroup filterGroup = new ToggleGroup();
        ToggleButton allBtn  = filterToggle("ALL",         Filter.ALL,       filterGroup);
        ToggleButton favBtn  = filterToggle("★ FAVORITES", Filter.FAVORITES, filterGroup);
        ToggleButton vehBtn  = filterToggle("VEHICLES",    Filter.VEHICLES,  filterGroup);
        ToggleButton itemBtn = filterToggle("ITEMS",       Filter.ITEMS,     filterGroup);
        allBtn.setSelected(true);

        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        Label groupLbl = new Label("Group by:");
        groupLbl.setTextFill(Color.web(DIM));
        groupLbl.setFont(Font.font("Arial", 11));
        ComboBox<Grouping> groupBox = new ComboBox<>();
        groupBox.getItems().addAll(Grouping.values());
        groupBox.setValue(currentGrouping);
        groupBox.setOnAction(e -> { currentGrouping = groupBox.getValue(); populateGrid(); });

        HBox filters = new HBox(8, allBtn, favBtn, vehBtn, itemBtn, filterSpacer, groupLbl, groupBox);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(10, 16, 10, 16));
        filters.setStyle("-fx-background-color: " + BG + ";");

        grid = new VBox(14);
        grid.setPadding(new Insets(6, 16, 16, 16));
        grid.setStyle("-fx-background-color: " + BG + ";");
        populateGrid();

        filterGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal == null) { filterGroup.selectToggle(allBtn); return; }
            currentFilter = (Filter) newVal.getUserData();
            populateGrid();
        });

        VBox content = new VBox(filters, grid);
        content.setStyle("-fx-background-color: " + BG + ";");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    private void populateGrid() {
        grid.getChildren().clear();
        Map<String, List<CardDto>> groups = new LinkedHashMap<>();
        for (CardDto c : cards) {
            if (matchesFilter(c)) groups.computeIfAbsent(groupKey(c), k -> new ArrayList<>()).add(c);
        }
        if (groups.isEmpty()) {
            Label empty = new Label(currentFilter == Filter.FAVORITES
                    ? (favorites == null
                            ? "Sign in to save favorites. Guests can build decks but favorites need an account."
                            : "No favorites yet — click the ☆ on any card to add it here.")
                    : "No cards to show.");
            empty.setFont(Font.font("Arial", 13));
            empty.setTextFill(Color.web(DIM));
            grid.getChildren().add(empty);
            return;
        }
        groups.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, List<CardDto>> e) -> groupOrder(e.getValue().get(0)))
                        .thenComparing(Map.Entry::getKey))
                .forEach(e -> {
                    if (currentGrouping != Grouping.NONE) {
                        Label header = new Label(e.getKey().toUpperCase() + "  (" + e.getValue().size() + ")");
                        header.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                        header.setTextFill(Color.web(ACCENT));
                        header.setMaxWidth(Double.MAX_VALUE);
                        header.setPadding(new Insets(0, 0, 4, 0));
                        header.setStyle("-fx-border-color: #444466; -fx-border-width: 0 0 1 0;");
                        grid.getChildren().add(header);
                    }
                    FlowPane row = new FlowPane(10, 10);
                    for (CardDto c : e.getValue()) {
                        row.getChildren().add(new CardTile(
                                c,
                                isFavorite(c),
                                favorites != null,
                                () -> addCard(c.id(), c.name()),
                                () -> toggleFavorite(c)
                        ).build());
                    }
                    grid.getChildren().add(row);
                });
    }

    private String groupKey(CardDto c) {
        return switch (currentGrouping) {
            case NONE     -> "All cards";
            case RARITY   -> c.level() != null ? title(c.level()) : "Unknown";
            case NATION   -> c.nation() != null ? c.nation() : "Supplies";
            case CATEGORY -> categoryOf(c);
        };
    }

    /** Sort position of a group (all cards in a group share it). */
    private int groupOrder(CardDto c) {
        return switch (currentGrouping) {
            case RARITY   -> rarityOrder(c);
            case CATEGORY -> isVehicle(c) && c.vehicleClass() != null
                    ? VehicleClass.valueOf(c.vehicleClass()).ordinal() : 100;
            case NATION   -> isVehicle(c) ? 0 : 1;
            case NONE     -> 0;
        };
    }

    private String categoryOf(CardDto c) {
        return switch (c.category()) {
            case "VEHICLE"    -> c.vehicleClass() != null ? title(c.vehicleClass()) : "Vehicles";
            case "AMMUNITION" -> "Ammunition";
            case "FUEL"       -> "Fuel";
            case "REPAIR"     -> "Repair";
            default           -> "Other Items";
        };
    }

    private static boolean isTank(CardDto c) {
        return isVehicle(c) && switch (String.valueOf(c.vehicleClass())) {
            case "LIGHT_TANK", "MEDIUM_TANK", "HEAVY_TANK", "MAIN_BATTLE_TANK" -> true;
            default -> false;
        };
    }

    private static boolean isVehicle(CardDto c) { return "VEHICLE".equals(c.category()); }

    private static int rarityOrder(CardDto c) {
        return c.level() != null ? CardLevel.valueOf(c.level()).ordinal() : 99;
    }

    private static String title(String enumName) {
        StringBuilder sb = new StringBuilder();
        for (String w : enumName.toLowerCase().split("_")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }

    private boolean matchesFilter(CardDto c) {
        return switch (currentFilter) {
            case ALL       -> true;
            case FAVORITES -> isFavorite(c);
            case VEHICLES  -> isVehicle(c);
            case ITEMS     -> !isVehicle(c);
        };
    }

    // ── Deck panel ────────────────────────────────────────────────────────────

    private VBox buildDeckPanel() {
        Label title = new Label("YOUR DECK");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setTextFill(Color.web(ACCENT));

        VBox top = new VBox(8, title);
        if (savedDecks != null) {
            deckPicker = new ComboBox<>();
            deckPicker.setPromptText("Load a saved deck...");
            deckPicker.setMaxWidth(Double.MAX_VALUE);
            deckPicker.setConverter(new StringConverter<>() {
                @Override public String toString(DeckDto d) {
                    return d == null ? "" : d.name() + " (" + d.totalCards() + ")" + (d.playable() ? "" : " ⚠");
                }
                @Override public DeckDto fromString(String s) { return null; }
            });
            Ui.styleCombo(deckPicker);
            deckPicker.getItems().setAll(savedDecks);
            deckPicker.setOnAction(e -> {
                DeckDto picked = deckPicker.getValue();
                if (loading || picked == null) return;
                if (!confirmDiscard()) { syncPicker(); return; }
                loadDeck(picked);
            });
            HBox.setHgrow(deckPicker, Priority.ALWAYS);

            Button newBtn = smallButton("NEW");
            newBtn.setOnAction(e -> {
                if (!confirmDiscard()) return;
                clearEditor();
            });
            top.getChildren().add(new HBox(6, deckPicker, newBtn));
        } else {
            Label guest = new Label("Guest mode: sign in to save decks.");
            guest.setFont(Font.font("Arial", 11));
            guest.setTextFill(Color.web(DIM));
            top.getChildren().add(guest);
        }

        deckNameField = Ui.field("Deck name");
        deckNameField.setMaxWidth(Double.MAX_VALUE);
        deckNameField.setTextFormatter(new TextFormatter<String>(
                c -> c.getControlNewText().length() <= DeckRules.MAX_NAME_LENGTH ? c : null));
        deckNameField.textProperty().addListener((o, was, now) -> { if (!loading) markDirty(); });

        deckCountLabel = new Label("0 / " + MAX_DECK + " cards");
        deckCountLabel.setFont(Font.font("Arial", 12));
        deckCountLabel.setTextFill(Color.web(TEXT));

        deckStatus = new Label();
        deckStatus.setFont(Font.font("Arial", 11));
        problemLabel = new Label();
        problemLabel.setFont(Font.font("Arial", 11));
        problemLabel.setTextFill(Color.web("#e8a04b"));
        problemLabel.setWrapText(true);

        HBox countRow = new HBox(8, deckCountLabel, deckStatus);
        countRow.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();

        deckListBox = new VBox(4);
        deckListBox.setPadding(new Insets(4, 0, 4, 0));

        ScrollPane deckScroll = new ScrollPane(deckListBox);
        deckScroll.setFitToWidth(true);
        deckScroll.setStyle("-fx-background: " + PANEL + "; -fx-background-color: " + PANEL + ";");
        VBox.setVgrow(deckScroll, Priority.ALWAYS);

        Label copiesHint = new Label("Max " + MAX_COPIES + " copies of each card");
        copiesHint.setFont(Font.font("Arial", 9));
        copiesHint.setTextFill(Color.web(DIM));

        saveBtn = Ui.button("SAVE DECK", 100);
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> saveDeck());
        HBox.setHgrow(saveBtn, Priority.ALWAYS);

        deleteBtn = new Button("DELETE");
        deleteBtn.setStyle(dangerStyle());
        deleteBtn.setOnAction(e -> deleteDeck());

        Button clearBtn = new Button("CLEAR");
        clearBtn.setStyle(dangerStyle());
        clearBtn.setOnAction(e -> {
            deckCounts.clear();
            deckNames.clear();
            markDirty();
        });

        HBox buttons = new HBox(6, saveBtn, deleteBtn, clearBtn);

        VBox panel = new VBox(8, top, deckNameField, countRow, problemLabel, sep, deckScroll, copiesHint, buttons);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + PANEL + ";");
        panel.setMinWidth(280);
        refreshDeckList();
        return panel;
    }

    private static String dangerStyle() {
        return "-fx-background-color: #2a0f0f; -fx-text-fill: #ff6b6b; -fx-font-weight: bold; "
                + "-fx-background-radius: 4; -fx-border-color: #ff6b6b; -fx-border-radius: 4; -fx-border-width: 1;";
    }

    private Button smallButton(String text) {
        Button b = new Button(text);
        b.setStyle(btnStyle(false).replace("-fx-padding: 6 14 6 14;", "-fx-padding: 5 10 5 10;"));
        b.setOnMouseEntered(e -> b.setStyle(btnStyle(true).replace("-fx-padding: 6 14 6 14;", "-fx-padding: 5 10 5 10;")));
        b.setOnMouseExited(e -> b.setStyle(btnStyle(false).replace("-fx-padding: 6 14 6 14;", "-fx-padding: 5 10 5 10;")));
        return b;
    }

    // ── Saving and loading decks ──────────────────────────────────────────────

    private void markDirty() {
        dirty = true;
        refreshDeckList();
    }

    /** Asks before throwing away unsaved work. */
    private boolean confirmDiscard() {
        if (!dirty || deckCounts.isEmpty() && deckNameField.getText().isBlank()) return true;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "You have unsaved changes to this deck. Discard them?", ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.setTitle("Unsaved changes");
        alert.initOwner(stage);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }

    private void clearEditor() {
        loading = true;
        deckCounts.clear();
        deckNames.clear();
        deckNameField.setText("");
        currentDeckId = null;
        dirty = false;
        loading = false;
        syncPicker();
        refreshDeckList();
    }

    private void loadDeck(DeckDto deck) {
        loading = true;
        deckCounts.clear();
        deckNames.clear();
        int missing = 0;
        for (Map.Entry<Long, Integer> e : deck.cardCounts().entrySet()) {
            CardDto card = cardsById.get(e.getKey());
            if (card == null) { missing++; continue; }
            deckCounts.put(card.id(), e.getValue());
            deckNames.put(card.id(), card.name());
        }
        deckNameField.setText(deck.name());
        currentDeckId = deck.id();
        dirty = missing > 0;
        loading = false;
        syncPicker();
        refreshDeckList();
        if (missing > 0) notice(missing + " card(s) in this deck no longer exist and were removed.", true);
    }

    /** Makes the dropdown show the deck being edited (or nothing for a new deck). */
    private void syncPicker() {
        if (deckPicker == null) return;
        loading = true;
        deckPicker.getItems().setAll(savedDecks);
        DeckDto current = savedDecks.stream().filter(d -> d.id().equals(currentDeckId)).findFirst().orElse(null);
        deckPicker.setValue(current);
        loading = false;
    }

    private void saveDeck() {
        if (savedDecks == null) {
            notice("Sign in to save decks (MY ACCOUNT on the main menu).", false);
            return;
        }
        String name = deckNameField.getText().trim();
        if (name.isEmpty()) {
            notice("Give your deck a name first.", true);
            deckNameField.requestFocus();
            return;
        }
        Map<Long, Integer> snapshot = new LinkedHashMap<>(deckCounts);
        Long id = currentDeckId;
        saveBtn.setDisable(true);
        Ui.async(() -> id == null ? ServerApi.createDeck(name, snapshot) : ServerApi.updateDeck(id, name, snapshot),
                saved -> {
                    saveBtn.setDisable(false);
                    savedDecks.removeIf(d -> d.id().equals(saved.id()));
                    savedDecks.add(0, saved);
                    currentDeckId = saved.id();
                    dirty = false;
                    syncPicker();
                    refreshDeckList();
                    notice("Saved \"" + saved.name() + "\".", false);
                },
                err -> {
                    saveBtn.setDisable(false);
                    handleDeckError(err, "Couldn't save the deck: ");
                });
    }

    private void deleteDeck() {
        if (currentDeckId == null || savedDecks == null) return;
        Long id = currentDeckId;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete \"" + deckNameField.getText() + "\"? This can't be undone.", ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.setTitle("Delete deck");
        alert.initOwner(stage);
        if (alert.showAndWait().filter(b -> b == ButtonType.OK).isEmpty()) return;
        Ui.async(() -> { ServerApi.deleteDeck(id); return true; },
                ok -> {
                    savedDecks.removeIf(d -> d.id().equals(id));
                    clearEditor();
                    notice("Deck deleted.", false);
                },
                err -> handleDeckError(err, "Couldn't delete the deck: "));
    }

    private void handleDeckError(Throwable err, String prefix) {
        if (err instanceof ServerApi.ApiError api && api.status() == 401) {
            AccountSession.signOut();
            notice("Your session expired. Sign in again from the main menu to save decks.", true);
        } else {
            notice(prefix + err.getMessage(), true);
        }
    }

    // ── Deck state ────────────────────────────────────────────────────────────

    private int deckTotal() {
        return deckCounts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private boolean canAdd(Long id) {
        return deckCounts.getOrDefault(id, 0) < MAX_COPIES && deckTotal() < MAX_DECK;
    }

    private void addCard(Long id, String name) {
        if (!canAdd(id)) return;
        deckCounts.merge(id, 1, Integer::sum);
        deckNames.put(id, name);
        markDirty();
    }

    private void removeCard(Long id) {
        int current = deckCounts.getOrDefault(id, 0);
        if (current <= 1) {
            deckCounts.remove(id);
            deckNames.remove(id);
        } else {
            deckCounts.put(id, current - 1);
        }
        markDirty();
    }

    private void refreshDeckList() {
        deckListBox.getChildren().clear();

        for (Map.Entry<Long, Integer> entry : deckCounts.entrySet()) {
            Long id    = entry.getKey();
            int  count = entry.getValue();

            Label name = new Label(deckNames.get(id));
            name.setFont(Font.font("Arial", 12));
            name.setTextFill(Color.web(TEXT));
            name.setMinWidth(0);
            name.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(name, Priority.ALWAYS);

            Button minus = stepButton("−");
            minus.setOnAction(e -> removeCard(id));

            Label countLbl = new Label(String.valueOf(count));
            countLbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            countLbl.setTextFill(Color.web(ACCENT));
            countLbl.setMinWidth(18);
            countLbl.setAlignment(Pos.CENTER);

            Button plus = stepButton("+");
            plus.setDisable(!canAdd(id));
            plus.setOnAction(e -> addCard(id, deckNames.get(id)));

            HBox row = new HBox(6, name, minus, countLbl, plus);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(2, 4, 2, 4));
            deckListBox.getChildren().add(row);
        }

        int total = deckTotal();
        deckCountLabel.setText(total + " / " + MAX_DECK + " cards");
        deckCountLabel.setTextFill(Color.web(total >= MAX_DECK ? ACCENT : TEXT));

        int tanks = 0;
        for (Map.Entry<Long, Integer> e : deckCounts.entrySet()) {
            CardDto card = cardsById.get(e.getKey());
            if (card != null && isTank(card)) tanks += e.getValue();
        }
        List<String> problems = new ArrayList<>();
        if (total < DeckRules.MIN_PLAYABLE_DECK_SIZE) {
            problems.add("needs " + (DeckRules.MIN_PLAYABLE_DECK_SIZE - total) + " more card(s) (minimum "
                    + DeckRules.MIN_PLAYABLE_DECK_SIZE + ")");
        }
        if (tanks < DeckRules.MIN_TANKS) {
            problems.add("needs " + (DeckRules.MIN_TANKS - tanks) + " more tank(s) (minimum " + DeckRules.MIN_TANKS + ")");
        }
        problemLabel.setText(total == 0 || problems.isEmpty() ? ""
                : "⚠ Not playable yet: " + String.join(", ", problems) + ".");
        deckStatus.setText(dirty ? "● unsaved changes" : currentDeckId != null ? "✓ saved" : "");
        deckStatus.setTextFill(Color.web(dirty ? ACCENT : Ui.OK));
        deleteBtn.setDisable(currentDeckId == null);
    }

    // ── Favorites ─────────────────────────────────────────────────────────────

    private boolean isFavorite(CardDto c) {
        return favorites != null && favorites.contains(c.id());
    }

    private String defaultHint() {
        return favorites == null
                ? "Guest mode: sign in to save favorites  ·  Use + / − in the deck to adjust copies"
                : "Click ☆ on a card to favorite it  ·  Use + / − in the deck to adjust copies";
    }

    /** Shows a short message in the top bar, then restores the normal hint. */
    private void notice(String message, boolean error) {
        hint.setText(message);
        hint.setTextFill(Color.web(error ? "#ff6b6b" : ACCENT));
        if (noticeReset != null) noticeReset.stop();
        noticeReset = new PauseTransition(javafx.util.Duration.seconds(4));
        noticeReset.setOnFinished(e -> {
            hint.setText(defaultHint());
            hint.setTextFill(Color.web(DIM));
        });
        noticeReset.play();
    }

    private void toggleFavorite(CardDto card) {
        if (favorites == null) {
            notice("Sign in to save favorites (MY ACCOUNT on the main menu).", false);
            return;
        }
        boolean nowFavorite = !favorites.contains(card.id());
        if (nowFavorite) favorites.add(card.id()); else favorites.remove(card.id());
        populateGrid();
        Ui.async(() -> { ServerApi.setFavorite(card.id(), nowFavorite); return true; },
                ok -> { },
                err -> {
                    // undo the optimistic change
                    if (nowFavorite) favorites.remove(card.id()); else favorites.add(card.id());
                    populateGrid();
                    if (err instanceof ServerApi.ApiError api && api.status() == 401) {
                        AccountSession.signOut();
                        notice("Your session expired. Sign in again from the main menu to use favorites.", true);
                    } else {
                        notice("Couldn't save that favorite: " + err.getMessage(), true);
                    }
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Button stepButton(String symbol) {
        Button b = new Button(symbol);
        b.setMinSize(24, 24);
        b.setMaxSize(24, 24);
        String base = "-fx-background-color: #0f3460; -fx-text-fill: " + ACCENT + "; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 4; -fx-padding: 0;";
        String hover = base.replace("#0f3460", "#1a4a80");
        String off = "-fx-background-color: #222233; -fx-text-fill: " + DIM + "; -fx-font-weight: bold; "
                + "-fx-font-size: 13px; -fx-background-radius: 4; -fx-padding: 0;";
        b.setStyle(base);
        b.setOnMouseEntered(e -> { if (!b.isDisabled()) b.setStyle(hover); });
        b.setOnMouseExited(e -> b.setStyle(b.isDisabled() ? off : base));
        b.disabledProperty().addListener((obs, was, is) -> b.setStyle(is ? off : base));
        b.setFocusTraversable(false);
        return b;
    }

    private ToggleButton filterToggle(String label, Filter filter, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(label);
        btn.setUserData(filter);
        btn.setToggleGroup(group);
        String base = "-fx-background-color: #16213e; -fx-text-fill: " + TEXT
                + "; -fx-font-size: 12px; -fx-background-radius: 4; "
                + "-fx-border-color: #444; -fx-border-radius: 4; -fx-border-width: 1;";
        String sel = "-fx-background-color: " + ACCENT + "; -fx-text-fill: #1a1a2e; "
                + "-fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 4; "
                + "-fx-border-color: " + ACCENT + "; -fx-border-radius: 4; -fx-border-width: 1;";
        btn.setStyle(base);
        btn.selectedProperty().addListener((obs, old, v) -> btn.setStyle(v ? sel : base));
        return btn;
    }

    private String btnStyle(boolean hover) {
        String bg = hover ? "#0f3460" : "#16213e";
        return "-fx-background-color: " + bg + "; -fx-text-fill: " + ACCENT
                + "; -fx-font-weight: bold; -fx-background-radius: 4; "
                + "-fx-border-color: " + ACCENT + "; -fx-border-radius: 4; "
                + "-fx-border-width: 1; -fx-padding: 6 14 6 14;";
    }
}
