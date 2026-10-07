package com.example.valtrak.UI;

import com.example.valtrak.Data.GameData.Service.DeckBrowserService;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Resource.FuelCard;
import com.example.valtrak.Gameplay.Cards.Resource.RepairCard;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import com.example.valtrak.UI.components.CardTile;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

public class DeckBuilderScene {

    private static final String BG     = "#1a1a2e";
    private static final String PANEL  = "#16213e";
    private static final String ACCENT = "#e8b84b";
    private static final String TEXT   = "#d4d4d4";
    private static final String DIM    = "#555555";

    private static final int MAX_DECK   = 80;
    private static final int MAX_COPIES = 3;

    private enum Filter { ALL, FAVORITES, VEHICLES, ITEMS }

    private enum Grouping {
        CATEGORY("Category"), RARITY("Rarity"), NATION("Nation"), NONE("None");
        final String label;
        Grouping(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }

    private final Stage stage;
    private final List<Card> cards;
    private final FavoritesStore favorites = new FavoritesStore();

    private final Map<Long, Integer> deckCounts = new LinkedHashMap<>();
    private final Map<Long, String>  deckNames  = new LinkedHashMap<>();

    private Filter currentFilter = Filter.ALL;
    private Grouping currentGrouping = Grouping.CATEGORY;
    private VBox grid;
    private Label deckCountLabel;
    private VBox  deckListBox;

    public DeckBuilderScene(Stage stage) {
        this.stage = stage;
        DeckBrowserService svc = ValtrakFXApp.getContext().getBean(DeckBrowserService.class);
        List<Card> all = new ArrayList<>(svc.getAllCards());
        // vehicles first, then items; within each group by rarity, then name
        all.sort(Comparator
                .comparingInt((Card c) -> c instanceof GroundVehicleCard ? 0 : 1)
                .thenComparingInt(c -> c.getLevel() != null ? c.getLevel().ordinal() : 0)
                .thenComparing(Card::getName));
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
            stage.setResizable(false);
            stage.setScene(new MainMenuScene(stage).build());
            stage.setWidth(640);
            stage.setHeight(480);
            stage.centerOnScreen();
        });

        Label title = new Label("DECK BUILDER");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(Color.web(ACCENT));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label hint = new Label("Click ☆ on a card to favorite it  ·  Use + / − in the deck to adjust copies");
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
        Map<String, List<Card>> groups = new LinkedHashMap<>();
        for (Card c : cards) {
            if (matchesFilter(c)) groups.computeIfAbsent(groupKey(c), k -> new ArrayList<>()).add(c);
        }
        if (groups.isEmpty()) {
            Label empty = new Label(currentFilter == Filter.FAVORITES
                    ? "No favorites yet — click the ☆ on any card to add it here."
                    : "No cards to show.");
            empty.setFont(Font.font("Arial", 13));
            empty.setTextFill(Color.web(DIM));
            grid.getChildren().add(empty);
            return;
        }
        groups.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<String, List<Card>> e) -> groupOrder(e.getValue().get(0)))
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
                    for (Card c : e.getValue()) {
                        row.getChildren().add(new CardTile(
                                c,
                                favorites.isFavorite(c.getName()),
                                () -> addCard(c.getId(), c.getName()),
                                () -> { favorites.toggle(c.getName()); populateGrid(); }
                        ).build());
                    }
                    grid.getChildren().add(row);
                });
    }

    private String groupKey(Card c) {
        return switch (currentGrouping) {
            case NONE     -> "All cards";
            case RARITY   -> c.getLevel() != null ? title(c.getLevel().name()) : "Unknown";
            case NATION   -> c instanceof GroundVehicleCard v && v.getVehicleNation() != null
                    ? v.getVehicleNation() : "Supplies";
            case CATEGORY -> categoryOf(c);
        };
    }

    /** Sort position of a group (all cards in a group share it). */
    private int groupOrder(Card c) {
        return switch (currentGrouping) {
            case RARITY   -> c.getLevel() != null ? c.getLevel().ordinal() : 99;
            case CATEGORY -> c instanceof GroundVehicleCard v && v.getVehicleClass() != null
                    ? java.util.Arrays.asList(
                            com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass.values())
                            .indexOf(com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass
                                    .valueOf(v.getVehicleClass().getClassName()))
                    : 100;
            case NATION   -> c instanceof GroundVehicleCard ? 0 : 1;
            case NONE     -> 0;
        };
    }

    private String categoryOf(Card c) {
        if (c instanceof GroundVehicleCard v) {
            return v.getVehicleClass() != null ? title(v.getVehicleClass().getClassName()) : "Vehicles";
        }
        if (c instanceof AmmunitionCard) return "Ammunition";
        if (c instanceof FuelCard) return "Fuel";
        if (c instanceof RepairCard) return "Repair";
        return "Other Items";
    }

    private static String title(String enumName) {
        StringBuilder sb = new StringBuilder();
        for (String w : enumName.toLowerCase().split("_")) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1));
        }
        return sb.toString();
    }

    private boolean matchesFilter(Card c) {
        return switch (currentFilter) {
            case ALL       -> true;
            case FAVORITES -> favorites.isFavorite(c.getName());
            case VEHICLES  -> c instanceof GroundVehicleCard;
            case ITEMS     -> c instanceof ItemCard;
        };
    }

    // ── Deck panel ────────────────────────────────────────────────────────────

    private VBox buildDeckPanel() {
        Label title = new Label("YOUR DECK");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setTextFill(Color.web(ACCENT));

        deckCountLabel = new Label("0 / " + MAX_DECK + " cards");
        deckCountLabel.setFont(Font.font("Arial", 12));
        deckCountLabel.setTextFill(Color.web(TEXT));

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

        Button clearBtn = new Button("CLEAR DECK");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setStyle(
                "-fx-background-color: #2a0f0f; -fx-text-fill: #ff6b6b; -fx-font-weight: bold; " +
                "-fx-background-radius: 4; -fx-border-color: #ff6b6b; " +
                "-fx-border-radius: 4; -fx-border-width: 1;"
        );
        clearBtn.setOnAction(e -> {
            deckCounts.clear();
            deckNames.clear();
            refreshDeckList();
        });

        VBox panel = new VBox(10, title, deckCountLabel, sep, deckScroll, copiesHint, clearBtn);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + PANEL + ";");
        panel.setMinWidth(240);
        return panel;
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
        refreshDeckList();
    }

    private void removeCard(Long id) {
        int current = deckCounts.getOrDefault(id, 0);
        if (current <= 1) {
            deckCounts.remove(id);
            deckNames.remove(id);
        } else {
            deckCounts.put(id, current - 1);
        }
        refreshDeckList();
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
