package com.example.valtrak.UI;

import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import com.example.valtrak.UI.net.ServerApi;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

public class MainMenuScene {

    private static final int WINDOW_WIDTH  = 640;
    private static final int WINDOW_HEIGHT = 480;

    private static final String BG_COLOR      = "#1a1a2e";
    private static final String ACCENT_COLOR  = "#e8b84b";
    private static final String BTN_COLOR     = "#16213e";
    private static final String BTN_HOVER     = "#0f3460";
    private static final String BTN_DISABLED  = "#2d2d2d";
    private static final String TEXT_COLOR    = "#d4d4d4";
    private static final String TEXT_DISABLED = "#555555";

    private final Stage stage;

    public MainMenuScene(Stage stage) {
        this.stage = stage;
    }

    public Scene build() {
        Label title = new Label("VALTRAK");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        title.setTextFill(Color.web(ACCENT_COLOR));

        Label subtitle = new Label("Modern Military Card Combat");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        subtitle.setTextFill(Color.web(TEXT_COLOR));

        Button playBtn      = createButton("PLAY GAME",    false);
        Button deckBtn      = createButton("DECK BUILDER", false);
        Button settingsBtn  = createButton("SETTINGS",     true);
        Button quitBtn      = createButton("QUIT",         false);

        playBtn.setOnAction(e -> onPlay());
        Label status = new Label();
        status.setFont(Font.font("Arial", 12));
        status.setTextFill(Color.web("#ff6b6b"));
        status.setWrapText(true);
        status.setMaxWidth(420);
        status.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        status.setAlignment(Pos.CENTER);

        deckBtn.setOnAction(e -> openDeckBuilder(deckBtn, status));
        quitBtn.setOnAction(e -> stage.close());

        VBox root = new VBox(14, title, subtitle, spacer(8), playBtn, deckBtn, settingsBtn, spacer(10), quitBtn, status);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        return new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    private Button createButton(String text, boolean disabled) {
        Button btn = new Button(text);
        btn.setDisable(disabled);
        btn.setMinWidth(260);
        btn.setMinHeight(46);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        String base = disabled
                ? "-fx-background-color: " + BTN_DISABLED + "; -fx-text-fill: " + TEXT_DISABLED + "; -fx-background-radius: 4;"
                : "-fx-background-color: " + BTN_COLOR + "; -fx-text-fill: " + ACCENT_COLOR + "; -fx-background-radius: 4; -fx-border-color: " + ACCENT_COLOR + "; -fx-border-radius: 4; -fx-border-width: 1;";

        btn.setStyle(base);

        if (!disabled) {
            String hover = "-fx-background-color: " + BTN_HOVER + "; -fx-text-fill: " + ACCENT_COLOR + "; -fx-background-radius: 4; -fx-border-color: " + ACCENT_COLOR + "; -fx-border-radius: 4; -fx-border-width: 1;";
            btn.setOnMouseEntered(e -> btn.setStyle(hover));
            btn.setOnMouseExited(e -> btn.setStyle(base));
        }

        return btn;
    }

    private javafx.scene.layout.Region spacer(double height) {
        javafx.scene.layout.Region r = new javafx.scene.layout.Region();
        r.setMinHeight(height);
        return r;
    }

    /** Loads the card catalog from the server off the UI thread, then opens the deck builder. */
    private void openDeckBuilder(Button deckBtn, Label status) {
        deckBtn.setDisable(true);
        status.setTextFill(Color.web(TEXT_COLOR));
        status.setText("Loading cards from " + ServerApi.BASE_URL + " ...");
        Task<List<CardDto>> load = new Task<>() {
            @Override protected List<CardDto> call() throws Exception { return ServerApi.fetchCards(); }
        };
        load.setOnSucceeded(ev -> {
            deckBtn.setDisable(false);
            status.setText("");
            stage.setResizable(true);
            stage.setScene(new DeckBuilderScene(stage, load.getValue()).build());
            stage.centerOnScreen();
        });
        load.setOnFailed(ev -> {
            deckBtn.setDisable(false);
            status.setTextFill(Color.web("#ff6b6b"));
            status.setText("Can't reach the server at " + ServerApi.BASE_URL + ". Is it running?");
        });
        Thread t = new Thread(load, "load-cards");
        t.setDaemon(true);
        t.start();
    }

    private void onPlay() {
        // Placeholder — will open game lobby/setup screen
        System.out.println("PLAY GAME clicked");
    }
}
