package com.example.valtrak.UI;

import com.example.valtrak.Data.GameData.Service.DeckBrowserService;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
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

    private static final int MAX_DECK  = 80;
    private static final int MAX_COPIES = 3;

    private final Stage stage;
    private final List<GroundVehicleCard> vehicleCards;
    private final List<AmmunitionCard>   ammoCards;

    private final Map<Long, Integer> deckCounts = new LinkedHashMap<>();
    private final Map<Long, String>  deckNames  = new LinkedHashMap<>();

    private Label deckCountLabel;
    private VBox  deckListBox;

    public DeckBuilderScene(Stage stage) {
        this.stage = stage;
        DeckBrowserService svc = ValtrakFXApp.getContext().getBean(DeckBrowserService.class);
        this.vehicleCards = svc.getAllVehicleCards();
        this.ammoCards    = svc.getAllAmmunitionCards();
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

        Label hint = new Label("Click a card to add it  ·  Click a deck entry to remove one copy");
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
        ToggleButton allBtn     = filterToggle("ALL",      filterGroup);
        ToggleButton vehBtn     = filterToggle("VEHICLES", filterGroup);
        ToggleButton itemBtn    = filterToggle("ITEMS",    filterGroup);
        allBtn.setSelected(true);

        HBox filters = new HBox(8, allBtn, vehBtn, itemBtn);
        filters.setPadding(new Insets(10, 16, 10, 16));
        filters.setStyle("-fx-background-color: " + BG + ";");

        FlowPane grid = new FlowPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(6, 16, 16, 16));
        grid.setStyle("-fx-background-color: " + BG + ";");
        populateGrid(grid, "ALL");

        filterGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal == null) { filterGroup.selectToggle(allBtn); return; }
            populateGrid(grid, ((ToggleButton) newVal).getText());
        });

        VBox content = new VBox(filters, grid);
        content.setStyle("-fx-background-color: " + BG + ";");

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BG + "; -fx-background-color: " + BG + ";");
        return scroll;
    }

    private void populateGrid(FlowPane grid, String filter) {
        grid.getChildren().clear();
        if (!filter.equals("ITEMS")) {
            for (GroundVehicleCard c : vehicleCards) {
                grid.getChildren().add(new CardTile(c, () -> addCard(c.getId(), c.getName())).build());
            }
        }
        if (!filter.equals("VEHICLES")) {
            for (AmmunitionCard c : ammoCards) {
                grid.getChildren().add(new CardTile(c, () -> addCard(c.getId(), c.getName())).build());
            }
        }
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

        Label removeHint = new Label("Click an entry to remove one copy");
        removeHint.setFont(Font.font("Arial", 9));
        removeHint.setTextFill(Color.web(DIM));

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

        VBox panel = new VBox(10, title, deckCountLabel, sep, deckScroll, removeHint, clearBtn);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: " + PANEL + ";");
        panel.setMinWidth(220);
        return panel;
    }

    // ── Deck state ────────────────────────────────────────────────────────────

    private void addCard(Long id, String name) {
        int current = deckCounts.getOrDefault(id, 0);
        if (current < MAX_COPIES) {
            deckCounts.put(id, current + 1);
            deckNames.put(id, name);
        }
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
        int total = deckCounts.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<Long, Integer> entry : deckCounts.entrySet()) {
            Long id    = entry.getKey();
            int  count = entry.getValue();
            String name = deckNames.get(id);

            Label row = new Label(count + "×  " + name);
            row.setFont(Font.font("Arial", 12));
            row.setTextFill(Color.web(TEXT));
            row.setMaxWidth(Double.MAX_VALUE);
            row.setStyle("-fx-cursor: hand; -fx-padding: 2 4 2 4;");
            row.setOnMouseClicked(e -> removeCard(id));
            row.setOnMouseEntered(e -> {
                row.setTextFill(Color.web("#ff8888"));
                row.setStyle("-fx-cursor: hand; -fx-background-color: #2a1010; -fx-padding: 2 4 2 4;");
            });
            row.setOnMouseExited(e -> {
                row.setTextFill(Color.web(TEXT));
                row.setStyle("-fx-cursor: hand; -fx-padding: 2 4 2 4;");
            });
            deckListBox.getChildren().add(row);
        }

        boolean over = total > MAX_DECK;
        deckCountLabel.setText(total + " / " + MAX_DECK + " cards" + (over ? " ⚠" : ""));
        deckCountLabel.setTextFill(Color.web(over ? "#ff6b6b" : TEXT));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ToggleButton filterToggle(String label, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(label);
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
