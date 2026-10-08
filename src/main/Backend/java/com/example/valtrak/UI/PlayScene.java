package com.example.valtrak.UI;

import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import com.example.valtrak.Data.GameData.DataTransfer.DeckData.DeckDtos.DeckDto;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.MatchSummary;
import com.example.valtrak.UI.net.AccountSession;
import com.example.valtrak.UI.net.ServerApi;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Map;

/**
 * The lobby: practise against the computer, challenge another player, answer challenges, and open your games.
 * Everything here goes through the server; the lobby holds no game rules.
 */
public class PlayScene {

    public static final int WIDTH = 940;
    public static final int HEIGHT = 640;

    private final Stage stage;
    private final Map<Long, CardDto> cards;
    private final List<DeckDto> decks;

    private final VBox gamesBox = new VBox(8);
    private final Label status = new Label();
    private final ComboBox<DeckDto> myDeck = new ComboBox<>();
    private Timeline refresher;

    public PlayScene(Stage stage, Map<Long, CardDto> cards, List<DeckDto> decks) {
        this.stage = stage;
        this.cards = cards;
        this.decks = decks;
    }

    public Scene build() {
        Label title = Ui.heading("PLAY", 30);
        Button back = Ui.link("← Main menu");
        back.setOnAction(e -> leave());
        HBox top = new HBox(16, back, title);
        top.setAlignment(Pos.CENTER_LEFT);

        myDeck.getItems().setAll(decks);
        myDeck.setConverter(deckConverter());
        myDeck.setMinWidth(300);
        Ui.styleCombo(myDeck);
        myDeck.setPromptText(decks.isEmpty() ? "Save a deck in the Deck Builder first" : "Choose your deck");
        decks.stream().filter(DeckDto::playable).findFirst().ifPresent(myDeck::setValue);
        VBox deckBox = section("Your deck", Ui.body("The deck you take into a game. It must be playable (60–100 cards, at least 12 tanks)."), myDeck);

        VBox left = new VBox(14, deckBox, botSection(), challengeSection());
        left.setPrefWidth(380);
        left.setMinWidth(380);

        Button refresh = Ui.button("REFRESH", 110);
        refresh.setOnAction(e -> refreshGames());
        Label gamesTitle = Ui.heading("Your games", 18);
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox gamesHead = new HBox(gamesTitle, grow, refresh);
        gamesHead.setAlignment(Pos.CENTER_LEFT);
        ScrollPane scroll = new ScrollPane(gamesBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + Ui.BG + "; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox right = new VBox(10, gamesHead, scroll);
        HBox.setHgrow(right, Priority.ALWAYS);

        status.setWrapText(true);
        status.setFont(Font.font("Arial", 12));
        HBox center = new HBox(24, left, right);
        VBox.setVgrow(center, Priority.ALWAYS);
        VBox root = new VBox(14, top, center, status);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + Ui.BG + ";");

        refreshGames();
        refresher = new Timeline(new KeyFrame(Duration.seconds(4), e -> refreshGames()));
        refresher.setCycleCount(Timeline.INDEFINITE);
        refresher.play();
        return new Scene(root, WIDTH, HEIGHT);
    }

    // ── left column ──────────────────────────────────────────────────────────

    private VBox botSection() {
        ComboBox<String> botDeck = new ComboBox<>();
        botDeck.getItems().add("Standard deck (built by the server)");
        botDeck.getItems().add("Mirror (a copy of my deck)");
        for (DeckDto d : decks) botDeck.getItems().add("My deck: " + d.name());
        botDeck.setValue(botDeck.getItems().get(0));
        botDeck.setMinWidth(300);
        Ui.styleCombo(botDeck);

        ComboBox<String> style = new ComboBox<>();
        style.getItems().addAll("Aggressive", "Cautious");
        style.setValue("Aggressive");
        style.setMinWidth(300);
        Ui.styleCombo(style);

        Button start = Ui.button("START PRACTICE GAME", 300);
        start.setOnAction(e -> {
            DeckDto mine = myDeck.getValue();
            if (mine == null) { say("Choose your deck first.", true); return; }
            int i = botDeck.getSelectionModel().getSelectedIndex();
            String choice = i == 0 ? "STANDARD" : i == 1 ? "MIRROR" : String.valueOf(decks.get(i - 2).id());
            start.setDisable(true);
            say("Starting a practice game...", false);
            Ui.async(() -> ServerApi.startBotMatch(mine.id(), choice, style.getValue().toUpperCase()),
                    m -> { start.setDisable(false); open(m.id(), m.opponentName()); },
                    err -> { start.setDisable(false); say(err.getMessage(), true); });
        });
        return section("Practice against the computer",
                Ui.body("Test a deck without another player. The computer sees the whole game, so it is a sparring partner, not a fair fight."),
                label("The computer's deck"), botDeck, label("Its style"), style, start);
    }

    private VBox challengeSection() {
        TextField opponent = Ui.field("Opponent's display name");
        Button send = Ui.button("SEND CHALLENGE", 300);
        send.setOnAction(e -> {
            DeckDto mine = myDeck.getValue();
            if (mine == null) { say("Choose your deck first.", true); return; }
            String name = opponent.getText().trim();
            if (name.isEmpty()) { say("Type the display name of the player to challenge.", true); return; }
            send.setDisable(true);
            Ui.async(() -> ServerApi.challenge(name, mine.id()),
                    m -> { send.setDisable(false); opponent.clear(); say("Challenge sent to " + m.opponentName() + ".", false); refreshGames(); },
                    err -> { send.setDisable(false); say(err.getMessage(), true); });
        });
        return section("Challenge a player", opponent, send);
    }

    // ── games list ───────────────────────────────────────────────────────────

    private void refreshGames() {
        Ui.async(ServerApi::fetchMatches, this::showGames, err -> say(err.getMessage(), true));
    }

    private void showGames(List<MatchSummary> matches) {
        gamesBox.getChildren().clear();
        if (matches.isEmpty()) {
            gamesBox.getChildren().add(dim("No games yet. Start a practice game or challenge a player."));
            return;
        }
        for (MatchSummary m : matches) gamesBox.getChildren().add(row(m));
    }

    private HBox row(MatchSummary m) {
        String text = m.opponentName() + (m.vsBot() ? "  (practice)" : "");
        Label name = new Label(text);
        name.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        name.setTextFill(Color.WHITE);
        Label state = new Label(describe(m));
        state.setFont(Font.font("Arial", 11));
        state.setTextFill(Color.web(m.yourTurn() && !"FINISHED".equals(m.status()) ? Ui.OK : Ui.DIM));
        VBox info = new VBox(2, name, state);
        HBox.setHgrow(info, Priority.ALWAYS);
        info.setMaxWidth(Double.MAX_VALUE);

        HBox row = new HBox(8, info);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color: " + Ui.PANEL + "; -fx-background-radius: 5; -fx-border-color: #2c3a5e; -fx-border-radius: 5;");
        switch (m.status()) {
            case "PENDING" -> {
                if (m.youChallenged()) {
                    Button cancel = small("Cancel");
                    cancel.setOnAction(e -> act(() -> ServerApi.cancelChallenge(m.id())));
                    row.getChildren().add(cancel);
                } else {
                    Button accept = small("Accept");
                    accept.setOnAction(e -> {
                        DeckDto mine = myDeck.getValue();
                        if (mine == null) { say("Choose your deck (left) before accepting.", true); return; }
                        act(() -> ServerApi.acceptChallenge(m.id(), mine.id()));
                    });
                    Button decline = small("Decline");
                    decline.setOnAction(e -> act(() -> ServerApi.declineChallenge(m.id())));
                    row.getChildren().addAll(accept, decline);
                }
            }
            default -> {
                Button open = small("Open");
                open.setOnAction(e -> open(m.id(), m.opponentName()));
                row.getChildren().add(open);
            }
        }
        return row;
    }

    private static String describe(MatchSummary m) {
        return switch (m.status()) {
            case "PENDING" -> m.youChallenged() ? "Waiting for them to accept" : "Challenged you: choose a deck and accept";
            case "ACTIVE" -> m.yourTurn() ? "Your turn" : "Waiting for " + (m.vsBot() ? "the computer" : "their move");
            case "FINISHED" -> "Finished: you " + (m.result() == null ? "" : m.result().toLowerCase());
            default -> m.status();
        };
    }

    private void act(java.util.concurrent.Callable<MatchSummary> call) {
        Ui.async(call, m -> { say("", false); refreshGames(); }, err -> say(err.getMessage(), true));
    }

    // ── opening a game ───────────────────────────────────────────────────────

    private void open(long matchId, String opponent) {
        say("Opening the game...", false);
        Ui.async(() -> ServerApi.fetchMatch(matchId), view -> {
            if (refresher != null) refresher.stop();
            stage.setResizable(true);
            BoardScene board = new BoardScene(stage, matchId, opponent, cards, view, this::backToLobby);
            stage.setScene(board.build());
            stage.setWidth(BoardScene.WIDTH);
            stage.setHeight(BoardScene.HEIGHT);
            stage.centerOnScreen();
        }, err -> say(err.getMessage(), true));
    }

    private void backToLobby() {
        stage.setScene(new PlayScene(stage, cards, decks).build());
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.centerOnScreen();
    }

    private void leave() {
        if (refresher != null) refresher.stop();
        stage.setResizable(false);
        stage.setScene(new MainMenuScene(stage).build());
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    // ── small helpers ────────────────────────────────────────────────────────

    private StringConverter<DeckDto> deckConverter() {
        return new StringConverter<>() {
            @Override public String toString(DeckDto d) {
                return d == null ? "" : d.name() + "  (" + d.totalCards() + " cards" + (d.playable() ? "" : ", not playable") + ")";
            }
            @Override public DeckDto fromString(String s) { return null; }
        };
    }

    private void say(String text, boolean error) {
        status.setText(text);
        status.setTextFill(Color.web(error ? Ui.ERROR : Ui.OK));
    }

    private static VBox section(String title, javafx.scene.Node... content) {
        Label t = Ui.heading(title, 16);
        VBox box = new VBox(6, t);
        box.getChildren().addAll(content);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #131b33; -fx-background-radius: 6; -fx-border-color: #2c3a5e; -fx-border-radius: 6;");
        return box;
    }

    private static Label label(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 11));
        l.setTextFill(Color.web(Ui.DIM));
        return l;
    }

    private static Label dim(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.web(Ui.DIM));
        l.setWrapText(true);
        return l;
    }

    private static Button small(String text) {
        Button b = Ui.button(text, 80);
        b.setMinHeight(30);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        return b;
    }
}
