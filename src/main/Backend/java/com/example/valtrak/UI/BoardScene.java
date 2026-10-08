package com.example.valtrak.UI;

import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto.AttackDto;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.*;
import com.example.valtrak.UI.components.CardTile;
import com.example.valtrak.UI.net.Moves;
import com.example.valtrak.UI.net.ServerApi;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The game board. It shows the game exactly as the server describes it to this player (the opponent's hand and
 * face-down vehicles are never sent) and turns clicks into moves. All rules live on the server: if a move isn't
 * allowed, the server's reason is shown in the message line.
 * <p>
 * How to play with the mouse: click a card in your hand to see what you can do with it; click one of your vehicles
 * or a group heading for its actions; when a card or ability needs targets, the board highlights them and you
 * click them, then press Confirm.
 */
public class BoardScene {

    public static final int WIDTH = 1300;
    public static final int HEIGHT = 840;

    private static final Set<String> TANKS = Set.of("LIGHT_TANK", "MEDIUM_TANK", "HEAVY_TANK", "MAIN_BATTLE_TANK");
    private static final String GOLD = Ui.ACCENT;
    private static final String GREEN = "#6bcf7f";

    private final Stage stage;
    private final long matchId;
    private final String opponentName;
    private final Map<Long, CardDto> cards;
    private final Runnable onLeave;

    private GameView view;
    private Pending pending;
    private boolean busy;
    private int lastLogSeq;
    private Timeline poll;

    private final BorderPane root = new BorderPane();
    private final ListView<String> logList = new ListView<>();
    private final Button endTurn = Ui.button("END TURN", 230);
    private final Label message = new Label();

    public BoardScene(Stage stage, long matchId, String opponentName, Map<Long, CardDto> cards, GameView view, Runnable onLeave) {
        this.stage = stage;
        this.matchId = matchId;
        this.opponentName = opponentName;
        this.cards = cards;
        this.view = view;
        this.onLeave = onLeave;
    }

    // ── building the scene ───────────────────────────────────────────────────

    public Scene build() {
        root.setStyle("-fx-background-color: " + Ui.BG + ";");
        root.setRight(sidePane());
        endTurn.setOnAction(e -> send(Moves.endTurn()));
        render();
        refreshLog();

        poll = new Timeline(new KeyFrame(Duration.seconds(2), e -> pollServer()));
        poll.setCycleCount(Timeline.INDEFINITE);
        poll.play();
        return new Scene(root, WIDTH, HEIGHT);
    }

    private void leave() {
        if (poll != null) poll.stop();
        onLeave.run();
    }

    /** Draws everything from the current view. Cheap enough to do after every change. */
    private void render() {
        root.setTop(topBar());
        root.setCenter(centerArea());
        root.setBottom(handArea());
        endTurn.setDisable(!canAct() || !"PLAYING".equals(view.phase()));
    }

    private boolean canAct() {
        return view.yourTurn() && !"FINISHED".equals(view.phase()) && !busy;
    }

    private boolean finished() {
        return "FINISHED".equals(view.phase());
    }

    // ── top bar ──────────────────────────────────────────────────────────────

    private Node topBar() {
        Button back = Ui.link("← Lobby");
        back.setOnAction(e -> leave());
        Label vs = Ui.heading("vs " + opponentName, 18);

        String turn;
        String color = GOLD;
        if (finished()) {
            boolean won = view.winner() == view.youIndex();
            turn = (won ? "VICTORY" : "DEFEAT") + (view.endReason() == null ? "" : " - " + view.endReason());
            color = won ? GREEN : Ui.ERROR;
        } else if ("SETUP".equals(view.phase())) {
            turn = view.yourTurn() ? "SETUP: click a tank in your hand to place it as your starting tank" : "SETUP: waiting for your opponent to place a tank";
        } else {
            turn = view.yourTurn() ? "YOUR TURN" : "Opponent's turn...";
            if (view.yourTurn()) color = GREEN;
        }
        Label turnLbl = new Label(turn);
        turnLbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        turnLbl.setTextFill(Color.web(color));

        Label chips = new Label("Territory chips  You " + view.you().chips() + " / " + view.winChips()
                + "   ·   Opponent " + view.opponent().chips() + " / " + view.winChips());
        chips.setTextFill(Color.web(Ui.TEXT));

        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        Button resign = Ui.button("RESIGN", 100);
        resign.setMinHeight(30);
        resign.setDisable(finished());
        resign.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Give up this game?", ButtonType.YES, ButtonType.NO);
            a.initOwner(stage);
            a.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> resignNow());
        });

        HBox bar = new HBox(18, back, vs, turnLbl, grow, chips, resign);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 14, 8, 14));
        bar.setStyle("-fx-background-color: #101830; -fx-border-color: #2c3a5e; -fx-border-width: 0 0 1 0;");
        return bar;
    }

    // ── centre: both sides ───────────────────────────────────────────────────

    private Node centerArea() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10));
        if (pending != null) box.getChildren().add(pendingBanner());
        box.getChildren().add(playerArea(view.opponent(), false));
        Separator sep = new Separator();
        box.getChildren().add(sep);
        box.getChildren().add(playerArea(view.you(), true));
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + Ui.BG + "; -fx-background-color: transparent;");
        return scroll;
    }

    private Node playerArea(PlayerView p, boolean mine) {
        Label name = new Label((mine ? "YOU: " : "OPPONENT: ") + p.displayName() + (p.nation() == null ? "" : "  [" + p.nation() + "]"));
        name.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        name.setTextFill(Color.web(mine ? GREEN : "#e07a7a"));
        Label counts = dim("Groups " + p.groups().size() + "/" + p.groupLimit() + "   ·   Deck " + p.deckSize() + "   ·   Hand " + p.handSize()
                + (mine && "PLAYING".equals(view.phase()) ? "   ·   Resource cards you may still play this turn: " + p.designationsLeft() : ""));
        Label depot = new Label("Depot (safe): " + resources(p.depot()));
        depot.setTextFill(Color.web("#cdd6f4"));
        depot.setFont(Font.font("Arial", 12));
        VBox head = new VBox(2, new HBox(14, name, counts), depot);

        HBox groups = new HBox(12);
        groups.setAlignment(Pos.TOP_LEFT);
        int n = 1;
        for (GroupView g : p.groups()) groups.getChildren().add(groupPanel(g, n++, mine));
        if (p.groups().isEmpty()) groups.getChildren().add(dim("(no strike groups)"));
        ScrollPane scroll = new ScrollPane(groups);
        scroll.setFitToHeight(true);
        scroll.setMinHeight(236);
        scroll.setPrefHeight(236);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + Ui.BG + "; -fx-background-color: transparent;");
        return new VBox(4, head, scroll);
    }

    private Node groupPanel(GroupView g, int number, boolean mine) {
        String jam = g.jammer() == null ? "" : "   · Jammer " + (g.jammer().on() ? "ON" : "off") + " " + g.jammer().hp() + "/" + g.jammer().maxHp();
        Label head = new Label("Group " + number + (g.formed() ? "" : " (lone)") + jam);
        head.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        head.setTextFill(Color.web(GOLD));
        head.setMaxWidth(Double.MAX_VALUE);
        head.setPadding(new Insets(2, 6, 2, 6));
        boolean pickable = pending != null && pending.accepts.test(new Target(mine, true, g.id(), null, null));
        boolean picked = pending != null && pending.groupPicked(g.id());
        head.setStyle("-fx-background-color: " + (picked ? "#5a4a10" : pickable ? "#1f4d2a" : "#1d2a4a") + "; -fx-background-radius: 3; -fx-cursor: hand;");
        head.setOnMouseClicked(e -> onGroupClick(g, number, mine, head));

        HBox vehicles = new HBox(6);
        int i = 0;
        for (VehicleView v : g.vehicles()) vehicles.getChildren().add(vehicleTile(v, mine, mine && i++ == 0));
        Label pool = new Label("Pool (at risk): " + resources(g.pool()));
        pool.setTextFill(Color.web("#cdd6f4"));
        pool.setFont(Font.font("Arial", 11));
        pool.setWrapText(true);
        pool.setMaxWidth(Math.max(240, g.vehicles().size() * 124));
        VBox box = new VBox(4, head, vehicles, pool);
        box.setPadding(new Insets(6));
        box.setStyle("-fx-background-color: #131b33; -fx-background-radius: 6; -fx-border-color: " + (pickable ? GREEN : "#2c3a5e") + "; -fx-border-radius: 6;");
        return box;
    }

    private Node vehicleTile(VehicleView v, boolean mine, boolean leader) {
        CardDto c = v.cardId() == null ? null : cards.get(v.cardId());
        VBox tile = new VBox(2);
        tile.setPrefWidth(118);
        tile.setMinWidth(118);
        tile.setMaxWidth(118);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.setPadding(new Insets(4));
        String border = "#3a4a70";
        if (c == null) {
            Label q = new Label("?");
            q.setFont(Font.font("Arial", FontWeight.BOLD, 40));
            q.setTextFill(Color.web("#3a4a70"));
            q.setMinHeight(66);
            Label down = new Label("FACE DOWN");
            down.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            down.setTextFill(Color.web(Ui.DIM));
            tile.getChildren().addAll(q, down);
            if (v.smoked()) tile.getChildren().add(mark("SMOKE", "#9aa7b8"));
            tile.setMinHeight(150);
        } else {
            border = CardTile.rarityColor(c.level());
            tile.getChildren().add(scaled(CardTile.artFor(c), 0.72));
            Label name = new Label((leader ? "★ " : "") + c.name());
            name.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            name.setTextFill(Color.WHITE);
            name.setWrapText(true);
            name.setMaxWidth(110);
            name.setMinHeight(26);
            tile.getChildren().add(name);
            if (v.hp() != null && v.maxHp() != null) {
                ProgressBar hp = new ProgressBar(Math.max(0, (double) v.hp() / v.maxHp()));
                hp.setPrefWidth(104);
                hp.setPrefHeight(8);
                double ratio = (double) v.hp() / v.maxHp();
                hp.setStyle("-fx-accent: " + (ratio > 0.6 ? GREEN : ratio > 0.3 ? "#e8b84b" : "#e05a5a") + ";");
                Label hpText = new Label("HP " + v.hp() + "/" + v.maxHp() + "  ·  Armor " + (c.armor() == null ? 0 : c.armor()));
                hpText.setFont(Font.font("Arial", 9));
                hpText.setTextFill(Color.web("#aaaaaa"));
                tile.getChildren().addAll(hp, hpText);
            }
            Label facing = new Label(v.faceUp() ? "▲ FACE UP" : "▼ face down");
            facing.setFont(Font.font("Arial", FontWeight.BOLD, 9));
            facing.setTextFill(Color.web(v.faceUp() ? GREEN : Ui.DIM));
            tile.getChildren().add(facing);
            FlowPane marks = new FlowPane(3, 2);
            marks.setPrefWrapLength(108);
            if (Boolean.TRUE.equals(v.stunned())) marks.getChildren().add(mark("STUN", "#e05a5a"));
            if (Boolean.TRUE.equals(v.suppressed())) marks.getChildren().add(mark("SUPPRESSED", "#e8b84b"));
            if (Boolean.TRUE.equals(v.disabled())) marks.getChildren().add(mark("DISABLED", "#e05a5a"));
            if (v.breachStacks() != null && v.breachStacks() > 0) marks.getChildren().add(mark("BREACH " + v.breachStacks(), "#e8b84b"));
            if (v.smoked()) marks.getChildren().add(mark("SMOKE", "#9aa7b8"));
            if (v.eraCardId() != null) marks.getChildren().add(mark("ERA", "#7ab8e8"));
            if (v.camoCardId() != null) marks.getChildren().add(mark("CAMO", "#7ab8e8"));
            if (Boolean.TRUE.equals(v.abilityUsed())) marks.getChildren().add(mark("ability used", Ui.DIM));
            tile.getChildren().add(marks);
            if (mine && !v.faceUp()) tile.setOpacity(0.8);
            Tooltip.install(tile, new Tooltip(c.name() + "\n" + vehicleBlurb(c, v)));
        }
        Target t = new Target(mine, false, v.id(), v, c);
        boolean pickable = pending != null && pending.accepts.test(t);
        boolean picked = pending != null && pending.vehiclePicked(v.id());
        tile.setStyle("-fx-background-color: " + (picked ? "#3a2f0c" : "#16213e") + "; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-width: "
                + (picked || pickable ? 3 : 1.5) + "; -fx-border-color: " + (picked ? GOLD : pickable ? GREEN : border) + "; -fx-cursor: hand;");
        tile.setOnMouseClicked(e -> onVehicleClick(v, c, mine, tile));
        return tile;
    }

    private String vehicleBlurb(CardDto c, VehicleView v) {
        StringBuilder sb = new StringBuilder();
        if (c.vehicleClass() != null) sb.append(c.vehicleClass().replace('_', ' ')).append("  ·  ").append(c.level()).append('\n');
        if (c.attacks() != null) for (AttackDto a : c.attacks()) {
            sb.append(a.slot()).append(": ").append(a.name()).append("  dmg ").append(a.baseDamage())
                    .append("  ammo ").append(a.ammoCost()).append("  fuel ").append(a.fuelCost()).append('\n');
        }
        if (c.ability() != null) sb.append("Ability: ").append(c.ability()).append('\n');
        if (v.eraCardId() != null && cards.get(v.eraCardId()) != null) sb.append(cards.get(v.eraCardId()).ability()).append('\n');
        return sb.toString().trim();
    }

    // ── hand ─────────────────────────────────────────────────────────────────

    private Node handArea() {
        HBox hand = new HBox(8);
        hand.setPadding(new Insets(6, 10, 6, 10));
        hand.setAlignment(Pos.CENTER_LEFT);
        List<Long> ids = view.you().hand();
        for (long id : ids) hand.getChildren().add(handTile(id));
        if (ids.isEmpty()) hand.getChildren().add(dim("Your hand is empty."));
        ScrollPane scroll = new ScrollPane(hand);
        scroll.setFitToHeight(true);
        scroll.setPrefHeight(176);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: #101830; -fx-background-color: #101830; -fx-border-color: #2c3a5e; -fx-border-width: 1 0 0 0;");
        return scroll;
    }

    private Node handTile(long cardId) {
        CardDto c = cards.get(cardId);
        VBox tile = new VBox(2);
        tile.setPrefWidth(124);
        tile.setMinWidth(124);
        tile.setMaxWidth(124);
        tile.setAlignment(Pos.TOP_CENTER);
        tile.setPadding(new Insets(4));
        if (c == null) {
            tile.getChildren().add(new Label("Card " + cardId));
            return tile;
        }
        tile.getChildren().add(scaled(CardTile.artFor(c), 0.76));
        Label name = new Label(c.name());
        name.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        name.setTextFill(Color.WHITE);
        name.setWrapText(true);
        name.setMaxWidth(116);
        name.setMinHeight(26);
        Label info = new Label(handInfo(c));
        info.setFont(Font.font("Arial", 9));
        info.setTextFill(Color.web("#aaaaaa"));
        info.setWrapText(true);
        info.setMaxWidth(116);
        tile.getChildren().addAll(name, info);
        tile.setStyle("-fx-background-color: #16213e; -fx-background-radius: 5; -fx-border-radius: 5; -fx-border-width: 1.5; -fx-cursor: hand; -fx-border-color: "
                + CardTile.rarityColor(c.level()) + ";");
        Tooltip.install(tile, new Tooltip(c.name() + "  (" + c.level() + ")\n" + (c.description() == null ? "" : c.description())
                + ("VEHICLE".equals(c.category()) ? "\n" + vehicleBlurb(c, new VehicleView(0, false, null, null, null, null, null, null, null, null, null, null, false)) : "")));
        tile.setOnMouseClicked(e -> onHandClick(c, tile));
        return tile;
    }

    private static String handInfo(CardDto c) {
        return switch (c.category()) {
            case "VEHICLE" -> (c.vehicleClass() == null ? "" : c.vehicleClass().replace('_', ' ')) + "\nHP " + c.hp() + " · Armor " + c.armor();
            case "AMMUNITION" -> "Ammo ×" + c.count() + "\n" + (c.ammunition() == null ? "" : c.ammunition().replace('_', ' '));
            case "FUEL" -> "Fuel +" + c.count();
            case "SUPPLY" -> "Supply +" + c.count();
            case "REPAIR" -> c.repairAmount() != null && c.repairAmount() >= 999 ? "Full repairs" : "Repairs " + c.repairAmount() + " HP";
            case "ITEM" -> c.ability() == null ? "" : c.ability();
            default -> "";
        };
    }

    // ── side panel ───────────────────────────────────────────────────────────

    private Node sidePane() {
        Label title = Ui.heading("Game log", 14);
        logList.setStyle("-fx-control-inner-background: #0d1326; -fx-text-fill: #cdd6f4;");
        logList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setWrapText(true);
                setPrefWidth(0);
                setTextFill(Color.web("#cdd6f4"));
                setStyle("-fx-background-color: #0d1326; -fx-font-size: 11px;");
            }
        });
        VBox.setVgrow(logList, Priority.ALWAYS);
        message.setWrapText(true);
        message.setMaxWidth(250);
        message.setMinHeight(54);
        message.setAlignment(Pos.TOP_LEFT);
        Label help = new Label("Click a card in your hand, one of your vehicles, or a group heading to see what you can do. Attacking ends your turn.");
        help.setWrapText(true);
        help.setMaxWidth(250);
        help.setFont(Font.font("Arial", 10));
        help.setTextFill(Color.web(Ui.DIM));
        VBox side = new VBox(8, title, logList, message, endTurn, help);
        side.setPadding(new Insets(10));
        side.setPrefWidth(272);
        side.setStyle("-fx-background-color: #101830; -fx-border-color: #2c3a5e; -fx-border-width: 0 0 0 1;");
        return side;
    }

    private void say(String text, boolean error) {
        message.setText(text);
        message.setTextFill(Color.web(error ? Ui.ERROR : Ui.OK));
    }

    // ── clicking ─────────────────────────────────────────────────────────────

    private void onHandClick(CardDto c, Node anchor) {
        if (pending != null) { say("Finish or cancel the selection first.", true); return; }
        if (!canAct()) { say(finished() ? "The game is over." : view.yourTurn() ? "Please wait..." : "It's not your turn.", true); return; }
        ContextMenu menu = new ContextMenu();
        boolean tank = "VEHICLE".equals(c.category()) && TANKS.contains(c.vehicleClass());
        if ("SETUP".equals(view.phase())) {
            if (tank) menu.getItems().add(item("Place as my starting tank", () -> send(Moves.placeStartingTank(c.id()))));
            else { say("Choose a tank for your starting vehicle.", true); return; }
        } else {
            switch (c.category()) {
                case "AMMUNITION", "FUEL" -> {
                    menu.getItems().add(item("Put in my Depot (safe)", () -> send(Moves.designate(c.id(), null))));
                    int n = 1;
                    for (GroupView g : view.you().groups()) {
                        long gid = g.id();
                        menu.getItems().add(item("Put in Group " + n++ + "'s pool (at risk)", () -> send(Moves.designate(c.id(), gid))));
                    }
                }
                case "SUPPLY", "REPAIR" -> menu.getItems().add(item("Put in my Depot", () -> send(Moves.designate(c.id(), null))));
                case "VEHICLE" -> {
                    if (tank) menu.getItems().add(item("Deploy as a new group (face down)", () -> send(Moves.deploy(c.id(), null))));
                    int n = 1;
                    for (GroupView g : view.you().groups()) {
                        long gid = g.id();
                        menu.getItems().add(item("Deploy into Group " + n++, () -> send(Moves.deploy(c.id(), gid))));
                    }
                }
                case "ITEM" -> itemMenu(c, menu);
                default -> { return; }
            }
        }
        if (menu.getItems().isEmpty()) return;
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(item("Cancel", () -> {}));
        menu.show(anchor, Side.TOP, 0, 0);
    }

    private void itemMenu(CardDto c, ContextMenu menu) {
        String effect = c.itemType() == null ? "" : c.itemType();
        int first = c.effectPrimary() == null ? 1 : c.effectPrimary();
        int second = c.effectSecondary() == null ? 1 : c.effectSecondary();
        switch (effect) {
            case "DRAW_CARDS", "SABOTAGE" -> menu.getItems().add(item("Play " + c.name(), () -> send(Moves.playItem(c.id(), List.of(), null, List.of()))));
            case "ERA_PROTECTION", "CAMOUFLAGE" -> menu.getItems().add(item("Attach to one of my vehicles...", () ->
                    startPicking("Choose one of YOUR vehicles for " + c.name(), 1, false, t -> t.mine && !t.isGroup,
                            ids -> send(Moves.playItem(c.id(), ids, null, List.of())))));
            case "SMOKE_SCREEN" -> menu.getItems().add(item("Cover up to " + first + " of my vehicles...", () ->
                    startPicking("Choose up to " + first + " of YOUR vehicles to hide in smoke", first, false, t -> t.mine && !t.isGroup,
                            ids -> send(Moves.playItem(c.id(), ids, null, List.of())))));
            case "ARTILLERY_STRIKE" -> {
                boolean blind = "LEGENDARY".equals(c.level()) || "COMMANDER".equals(c.level());
                menu.getItems().add(item("Fire at up to " + second + " enemy vehicle(s)...", () ->
                        startPicking("Choose up to " + second + " enemy vehicles" + (blind ? " (this card can hit face-down ones too)" : " (face-up only)")
                                        + ". Aircraft can't be hit.",
                                second, false, t -> !t.mine && !t.isGroup && (t.vehicle.faceUp() ? !(t.card != null && Boolean.TRUE.equals(t.card.air())) : blind),
                                ids -> send(Moves.playItem(c.id(), ids, null, List.of())))));
            }
            case "JAMMER" -> menu.getItems().add(item("Attach to one of my strike groups...", () ->
                    startPicking("Click the heading of one of YOUR groups for " + c.name(), 1, false, t -> t.mine && t.isGroup,
                            ids -> send(Moves.playItem(c.id(), null, ids.get(0), List.of())))));
            case "SEARCH_RESOURCES", "SEARCH_TANKS", "SEARCH_SUPPORT" -> menu.getItems().add(item("Search my deck...", () -> {
                List<Option> options = new ArrayList<>();
                for (long id : view.you().deckCards()) {
                    CardDto d = cards.get(id);
                    if (d != null && searchMatches(effect, d)) options.add(new Option(id, d.name()));
                }
                pickOptions(c.name(), "Take up to " + first + " card(s) from your deck. Your opponent will see them.", options, first,
                        ids -> send(Moves.playItem(c.id(), List.of(), null, ids)));
            }));
            case "RECYCLE" -> menu.getItems().add(item("Return resource cards from my discard pile...", () -> {
                List<Option> options = new ArrayList<>();
                for (long id : view.you().discard()) {
                    CardDto d = cards.get(id);
                    if (d != null && isResourceCard(d)) options.add(new Option(id, d.name()));
                }
                pickOptions(c.name(), "Take up to " + first + " resource card(s) back into your hand.", options, first,
                        ids -> send(Moves.playItem(c.id(), List.of(), null, ids)));
            }));
            case "AIRDROP" -> menu.getItems().add(item("Drop Depot cards into a group's pool...", () -> {
                List<Option> options = new ArrayList<>();
                for (ResourceView r : view.you().depot()) {
                    if (!"AMMO".equals(r.kind()) && !"FUEL".equals(r.kind())) continue;
                    CardDto rc = cards.get(r.cardId());
                    options.add(new Option(r.id(), (rc == null ? "Card" : rc.name()) + "  (" + r.remaining() + " left)"));
                }
                pickOptions(c.name(), "Choose up to " + first + " Ammo or Fuel card(s) from your Depot, then the group that gets them.", options, first,
                        ids -> {
                            if (ids.isEmpty()) return;
                            startPicking("Click the heading of the group that receives the cards", 1, false, t -> t.mine && t.isGroup,
                                    groups -> send(Moves.playItem(c.id(), null, groups.get(0), ids)));
                        });
            }));
            case "RAPID_DEPLOYMENT" -> menu.getItems().add(item("Deploy up to " + first + " vehicle(s) with no formation cost...", () -> rapidDeployDialog(c, first)));
            default -> menu.getItems().add(item("Play " + c.name(), () -> send(Moves.playItem(c.id(), List.of(), null, List.of()))));
        }
    }

    private void onVehicleClick(VehicleView v, CardDto c, boolean mine, Node anchor) {
        Target t = new Target(mine, false, v.id(), v, c);
        if (pending != null) {
            if (pending.accepts.test(t)) { pending.toggleVehicle(v.id()); render(); }
            else say("That isn't a valid choice right now.", true);
            return;
        }
        if (!mine || c == null) return;
        if (!canAct() || !"PLAYING".equals(view.phase())) return;
        ContextMenu menu = new ContextMenu();
        boolean resupply = "SUPPLY".equals(c.vehicleClass());
        if (!v.faceUp() && !resupply) menu.getItems().add(item("Reveal (turn face up)", () -> send(Moves.reveal(v.id()))));
        if (v.faceUp()) menu.getItems().add(item("Retreat (turn face down, costs Fuel)", () -> send(Moves.retreat(v.id()))));
        if (c.abilityPower() != null && v.faceUp()) {
            int power = c.abilityPower();
            menu.getItems().add(item("Use ability: reveal up to " + power + " enemy vehicle(s) (" + c.abilityFuelCost() + " Fuel)...", () ->
                    startPicking("Choose up to " + power + " FACE-DOWN enemy vehicles to reveal", power, false,
                            tt -> !tt.mine && !tt.isGroup && !tt.vehicle.faceUp(), ids -> send(Moves.useAbility(v.id(), ids)))));
        }
        Menu move = new Menu("Move to another group (costs Fuel)");
        int n = 1;
        for (GroupView g : view.you().groups()) {
            long gid = g.id();
            boolean here = g.vehicles().stream().anyMatch(x -> x.id() == v.id());
            if (!here) move.getItems().add(item("Group " + n, () -> send(Moves.move(v.id(), gid))));
            n++;
        }
        if (TANKS.contains(c.vehicleClass())) move.getItems().add(item("Out to its own new group", () -> send(Moves.move(v.id(), null))));
        if (!move.getItems().isEmpty()) menu.getItems().add(move);
        Menu repair = new Menu("Repair with a card from my Depot");
        for (ResourceView r : view.you().depot()) {
            if (!"REPAIR".equals(r.kind())) continue;
            CardDto rc = cards.get(r.cardId());
            repair.getItems().add(item(rc == null ? "Repair card" : rc.name(), () -> send(Moves.repair(r.id(), v.id()))));
        }
        if (!repair.getItems().isEmpty()) menu.getItems().add(repair);
        if (menu.getItems().isEmpty()) return;
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }

    private void onGroupClick(GroupView g, int number, boolean mine, Node anchor) {
        Target t = new Target(mine, true, g.id(), null, null);
        if (pending != null) {
            if (pending.accepts.test(t)) { pending.toggleGroup(g.id()); render(); }
            else say("That isn't a valid choice right now.", true);
            return;
        }
        if (!mine || !canAct() || !"PLAYING".equals(view.phase())) return;
        ContextMenu menu = new ContextMenu();
        menu.getItems().add(item("Reveal the whole group", () -> send(Moves.revealGroup(g.id()))));
        menu.getItems().add(item("Retreat the whole group (costs Fuel)", () -> send(Moves.retreatGroup(g.id()))));
        menu.getItems().add(item("Convoy: move Depot cards into the pool...", () -> {
            List<Option> options = new ArrayList<>();
            for (ResourceView r : view.you().depot()) {
                if (!"AMMO".equals(r.kind()) && !"FUEL".equals(r.kind())) continue;
                CardDto rc = cards.get(r.cardId());
                options.add(new Option(r.id(), (rc == null ? "Card" : rc.name()) + "  (" + r.remaining() + " left)"));
            }
            pickOptions("Convoy into Group " + number, "Needs a Resupply vehicle in the group. It can move only a few cards per turn.",
                    options, Integer.MAX_VALUE, ids -> send(Moves.convoy(g.id(), ids)));
        }));
        if (g.jammer() != null) {
            boolean on = g.jammer().on();
            menu.getItems().add(item(on ? "Switch the Jammer off" : "Switch the Jammer on (costs Fuel each turn; it can be shot)", () -> send(Moves.jammer(g.id(), !on))));
        }
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(item("ATTACK with this group...", () -> attackDialog(g, number)));
        menu.show(anchor, Side.BOTTOM, 0, 0);
    }

    private MenuItem item(String text, Runnable action) {
        MenuItem i = new MenuItem(text);
        i.setOnAction(e -> action.run());
        return i;
    }

    // ── picking targets on the board ─────────────────────────────────────────

    private record Target(boolean mine, boolean isGroup, long id, VehicleView vehicle, CardDto card) {}

    private static final class Pending {
        final String prompt;
        final int max;
        final boolean allowNone;
        final Predicate<Target> accepts;
        final Consumer<List<Long>> onConfirm;
        final List<Long> vehicles = new ArrayList<>();
        final List<Long> groups = new ArrayList<>();

        Pending(String prompt, int max, boolean allowNone, Predicate<Target> accepts, Consumer<List<Long>> onConfirm) {
            this.prompt = prompt;
            this.max = max;
            this.allowNone = allowNone;
            this.accepts = accepts;
            this.onConfirm = onConfirm;
        }

        boolean vehiclePicked(long id) { return vehicles.contains(id); }
        boolean groupPicked(long id) { return groups.contains(id); }
        int count() { return vehicles.size() + groups.size(); }

        void toggleVehicle(long id) { toggle(vehicles, id); }
        void toggleGroup(long id) { toggle(groups, id); }

        private void toggle(List<Long> list, long id) {
            if (list.remove(id)) return;
            if (max == 1) { vehicles.clear(); groups.clear(); }
            if (count() < max) list.add(id);
        }

        List<Long> picked() {
            List<Long> all = new ArrayList<>(vehicles);
            all.addAll(groups);
            return all;
        }
    }

    private void startPicking(String prompt, int max, boolean allowNone, Predicate<Target> accepts, Consumer<List<Long>> onConfirm) {
        pending = new Pending(prompt, max, allowNone, accepts, onConfirm);
        say("", false);
        render();
    }

    private Node pendingBanner() {
        Label text = new Label(pending.prompt + "   (" + pending.count() + "/" + pending.max + " chosen)");
        text.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        text.setTextFill(Color.web(GOLD));
        text.setWrapText(true);
        Button confirm = Ui.button("CONFIRM", 110);
        confirm.setMinHeight(30);
        confirm.setDisable(pending.count() == 0 && !pending.allowNone);
        confirm.setOnAction(e -> {
            Pending p = pending;
            pending = null;
            p.onConfirm.accept(p.picked());
        });
        Button cancel = Ui.button("CANCEL", 100);
        cancel.setMinHeight(30);
        cancel.setOnAction(e -> { pending = null; render(); });
        Region grow = new Region();
        HBox.setHgrow(grow, Priority.ALWAYS);
        HBox box = new HBox(12, text, grow, confirm, cancel);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(8, 12, 8, 12));
        box.setStyle("-fx-background-color: #2a2410; -fx-border-color: " + GOLD + "; -fx-border-radius: 5; -fx-background-radius: 5;");
        return box;
    }

    // ── dialogs ──────────────────────────────────────────────────────────────

    private record Option(long key, String label) {}

    /** A list of check boxes; the chosen keys come back (a key appears once per ticked box). */
    private void pickOptions(String title, String prompt, List<Option> options, int max, Consumer<List<Long>> onOk) {
        Dialog<ButtonType> d = dialog(title);
        VBox box = new VBox(6);
        box.setPadding(new Insets(10));
        Label p = new Label(prompt);
        p.setTextFill(Color.web(Ui.TEXT));
        p.setWrapText(true);
        p.setMaxWidth(380);
        box.getChildren().add(p);
        List<CheckBox> boxes = new ArrayList<>();
        VBox list = new VBox(4);
        for (Option o : options) {
            CheckBox cb = new CheckBox(o.label());
            cb.setTextFill(Color.WHITE);
            boxes.add(cb);
            list.getChildren().add(cb);
        }
        if (options.isEmpty()) list.getChildren().add(dim("Nothing to choose from."));
        Runnable limit = () -> {
            long on = boxes.stream().filter(CheckBox::isSelected).count();
            boxes.forEach(cb -> cb.setDisable(!cb.isSelected() && on >= max));
        };
        boxes.forEach(cb -> cb.selectedProperty().addListener((o, a, b) -> limit.run()));
        ScrollPane sp = new ScrollPane(list);
        sp.setPrefHeight(Math.min(320, 40 + options.size() * 26));
        sp.setPrefWidth(400);
        sp.setStyle("-fx-background: " + Ui.PANEL + "; -fx-background-color: " + Ui.PANEL + ";");
        box.getChildren().add(sp);
        d.getDialogPane().setContent(box);
        d.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            List<Long> keys = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) if (boxes.get(i).isSelected()) keys.add(options.get(i).key());
            onOk.accept(keys);
        });
    }

    private void rapidDeployDialog(CardDto item, int max) {
        Dialog<ButtonType> d = dialog(item.name());
        ComboBox<Integer> group = new ComboBox<>();
        for (int i = 1; i <= view.you().groups().size(); i++) group.getItems().add(i);
        group.setValue(1);
        Ui.styleCombo(group);
        List<Option> options = new ArrayList<>();
        for (long id : view.you().hand()) {
            CardDto c = cards.get(id);
            if (c != null && "VEHICLE".equals(c.category())) options.add(new Option(id, c.name()));
        }
        if (view.you().groups().isEmpty() || options.isEmpty()) { say("You need a strike group and a vehicle in your hand.", true); return; }
        List<CheckBox> boxes = new ArrayList<>();
        VBox list = new VBox(4);
        for (Option o : options) {
            CheckBox cb = new CheckBox(o.label());
            cb.setTextFill(Color.WHITE);
            boxes.add(cb);
            list.getChildren().add(cb);
        }
        boxes.forEach(cb -> cb.selectedProperty().addListener((o, a, b) -> {
            long on = boxes.stream().filter(CheckBox::isSelected).count();
            boxes.forEach(x -> x.setDisable(!x.isSelected() && on >= max));
        }));
        Label l = new Label("Deploy into group:");
        l.setTextFill(Color.web(Ui.TEXT));
        VBox box = new VBox(8, l, group, new Label(""), list);
        box.setPadding(new Insets(10));
        d.getDialogPane().setContent(box);
        d.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) if (boxes.get(i).isSelected()) ids.add(options.get(i).key());
            long gid = view.you().groups().get(group.getValue() - 1).id();
            send(Moves.playItem(item.id(), null, gid, ids));
        });
    }

    private record TargetOpt(long id, String label) {}

    private static final class AttackRow {
        final CheckBox use;
        final ComboBox<AttackDto> attack = new ComboBox<>();
        final ComboBox<String> ammo = new ComboBox<>();
        final ComboBox<TargetOpt> target = new ComboBox<>();
        final long vehicleId;

        AttackRow(long vehicleId, String name) {
            this.vehicleId = vehicleId;
            this.use = new CheckBox(name);
            use.setTextFill(Color.WHITE);
            use.setMinWidth(170);
        }
    }

    /** Pick which of a group's face-up vehicles attack, with what, at which target. One vehicle = Skirmish; two or more = Combined Assault. */
    private void attackDialog(GroupView g, int number) {
        List<TargetOpt> targets = new ArrayList<>();
        for (GroupView eg : view.opponent().groups()) {
            for (VehicleView ev : eg.vehicles()) {
                if (!ev.faceUp() || ev.cardId() == null) continue;
                CardDto ec = cards.get(ev.cardId());
                targets.add(new TargetOpt(ev.id(), (ec == null ? "Vehicle" : ec.name()) + "  (" + ev.hp() + "/" + ev.maxHp() + " HP)"));
            }
            if (eg.jammer() != null && eg.jammer().on()) {
                targets.add(new TargetOpt(eg.jammer().id(), "Jammer  (" + eg.jammer().hp() + "/" + eg.jammer().maxHp() + " HP)"));
            }
        }
        if (targets.isEmpty()) { say("There is nothing to attack: no enemy vehicle is face up (use a UAV or Recon to reveal some).", true); return; }

        List<AttackRow> rows = new ArrayList<>();
        VBox list = new VBox(8);
        for (VehicleView v : g.vehicles()) {
            CardDto c = v.cardId() == null ? null : cards.get(v.cardId());
            if (c == null || !v.faceUp() || c.attacks() == null || c.attacks().isEmpty()) continue;
            AttackRow row = new AttackRow(v.id(), c.name());
            row.attack.getItems().setAll(c.attacks());
            row.attack.setConverter(new StringConverter<>() {
                @Override public String toString(AttackDto a) {
                    return a == null ? "" : a.slot().replace("ATTACK_", "#") + " " + a.name() + " · dmg " + a.baseDamage() + " · ammo " + a.ammoCost() + " · fuel " + a.fuelCost();
                }
                @Override public AttackDto fromString(String s) { return null; }
            });
            row.attack.setValue(c.attacks().get(0));
            Runnable fillAmmo = () -> {
                AttackDto a = row.attack.getValue();
                row.ammo.getItems().setAll(a == null || a.ammo() == null ? List.<String>of() : a.ammo());
                if (row.ammo.getItems().isEmpty()) return;
                // start with an ammunition type the pool actually has enough of
                String pick = row.ammo.getItems().get(0);
                for (String type : row.ammo.getItems()) {
                    int have = g.pool().stream().filter(r -> "AMMO".equals(r.kind()) && type.equals(r.ammunition())).mapToInt(ResourceView::remaining).sum();
                    if (a != null && have >= a.ammoCost()) { pick = type; break; }
                }
                row.ammo.setValue(pick);
            };
            fillAmmo.run();
            row.attack.valueProperty().addListener((o, a, b) -> fillAmmo.run());
            row.target.getItems().setAll(targets);
            row.target.setConverter(new StringConverter<>() {
                @Override public String toString(TargetOpt t) { return t == null ? "" : t.label(); }
                @Override public TargetOpt fromString(String s) { return null; }
            });
            row.target.setValue(targets.get(0));
            for (ComboBox<?> cb : List.of(row.attack, row.ammo, row.target)) Ui.styleCombo(cb);
            row.attack.setPrefWidth(300);
            row.ammo.setPrefWidth(190);
            row.target.setPrefWidth(250);
            rows.add(row);
            list.getChildren().add(new HBox(8, row.use, row.attack, row.ammo, new Label("→"), row.target));
        }
        if (rows.isEmpty()) { say("No face-up vehicle in Group " + number + " has an attack. Reveal some first.", true); return; }
        rows.get(0).use.setSelected(true);

        Label cost = new Label();
        cost.setTextFill(Color.web(Ui.TEXT));
        Label error = new Label();
        error.setTextFill(Color.web(Ui.ERROR));
        error.setWrapText(true);
        error.setMaxWidth(900);
        Runnable updateCost = () -> {
            Map<String, Integer> ammo = new LinkedHashMap<>();
            int fuel = 0, n = 0;
            for (AttackRow r : rows) {
                if (!r.use.isSelected() || r.attack.getValue() == null) continue;
                n++;
                if (r.ammo.getValue() != null) ammo.merge(r.ammo.getValue().replace('_', ' '), r.attack.getValue().ammoCost(), Integer::sum);
                fuel += r.attack.getValue().fuelCost();
            }
            StringBuilder sb = new StringBuilder(n >= 2 ? "Combined Assault (the Leader must be face up).  " : n == 1 ? "Skirmish.  " : "");
            sb.append("Cost: ");
            ammo.forEach((k, v) -> sb.append(v).append(' ').append(k).append(", "));
            sb.append(fuel).append(" Fuel.   The pool has: ").append(resources(g.pool()));
            cost.setText(sb.toString());
        };
        for (AttackRow r : rows) {
            r.use.selectedProperty().addListener((o, a, b) -> updateCost.run());
            r.attack.valueProperty().addListener((o, a, b) -> updateCost.run());
            r.ammo.valueProperty().addListener((o, a, b) -> updateCost.run());
        }
        updateCost.run();

        Dialog<ButtonType> d = dialog("Attack with Group " + number);
        Label hint = new Label("Tick the vehicles that attack. Attacking ends your turn. The cost is paid from this group's pool.");
        hint.setTextFill(Color.web(Ui.DIM));
        VBox box = new VBox(10, hint, list, cost, error);
        box.setPadding(new Insets(10));
        d.getDialogPane().setContent(box);
        d.getDialogPane().getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        ((Button) d.getDialogPane().lookupButton(ButtonType.OK)).setText("ATTACK");
        d.getDialogPane().lookupButton(ButtonType.OK).addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            ev.consume();
            List<AttackChoiceRequest> choices = new ArrayList<>();
            for (AttackRow r : rows) {
                if (!r.use.isSelected()) continue;
                if (r.attack.getValue() == null || r.target.getValue() == null) { error.setText("Choose an attack and a target."); return; }
                choices.add(new AttackChoiceRequest(r.vehicleId, r.attack.getValue().slot(), r.ammo.getValue(), r.target.getValue().id()));
            }
            if (choices.isEmpty()) { error.setText("Tick at least one vehicle."); return; }
            error.setText("");
            Ui.async(() -> ServerApi.act(matchId, Moves.attack(g.id(), choices)),
                    resp -> { d.close(); applyView(resp.view()); },
                    err -> error.setText(err.getMessage()));
        });
        d.show();
    }

    private Dialog<ButtonType> dialog(String title) {
        Dialog<ButtonType> d = new Dialog<>();
        d.initOwner(stage);
        d.setTitle(title);
        d.setHeaderText(title);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        d.getDialogPane().setStyle("-fx-background-color: " + Ui.BG + ";");
        d.getDialogPane().getStylesheets().add("data:text/css," + java.net.URLEncoder.encode(
                ".dialog-pane .header-panel { -fx-background-color: " + Ui.PANEL + "; } .dialog-pane .header-panel .label { -fx-text-fill: " + Ui.ACCENT + "; -fx-font-weight: bold; }"
                        + " .dialog-pane .label { -fx-text-fill: " + Ui.TEXT + "; }", java.nio.charset.StandardCharsets.UTF_8).replace("+", "%20"));
        return d;
    }

    // ── talking to the server ────────────────────────────────────────────────

    private void send(ActionRequest request) {
        if (busy) return;
        busy = true;
        pending = null;
        say("", false);
        render();
        Ui.async(() -> ServerApi.act(matchId, request),
                resp -> { busy = false; applyView(resp.view()); },
                err -> { busy = false; say(err.getMessage(), true); render(); });
    }

    private void resignNow() {
        busy = true;
        Ui.async(() -> ServerApi.resign(matchId),
                resp -> { busy = false; applyView(resp.view()); },
                err -> { busy = false; say(err.getMessage(), true); render(); });
    }

    private void applyView(GameView fresh) {
        view = fresh;
        pending = null;
        render();
        refreshLog();
    }

    private void pollServer() {
        if (busy || finished()) return;
        Ui.async(() -> ServerApi.fetchMatch(matchId), fresh -> {
            if (fresh.version() != view.version() && !busy) applyView(fresh);
        }, err -> { /* the next poll will try again */ });
    }

    private void refreshLog() {
        Ui.async(() -> ServerApi.fetchLog(matchId, lastLogSeq), lines -> {
            for (LogLine l : lines) {
                logList.getItems().add(relative(l.text()));
                lastLogSeq = Math.max(lastLogSeq, l.seq());
            }
            if (!lines.isEmpty()) logList.scrollTo(logList.getItems().size() - 1);
        }, err -> { });
    }

    // ── small helpers ────────────────────────────────────────────────────────

    /** The log is written with "Player 1" and "Player 2"; show "You" and "Opponent" instead. */
    private String relative(String line) {
        String you = "Player " + (view.youIndex() + 1);
        String them = "Player " + (2 - view.youIndex());
        String out = line.replace(them, "Opponent").replace(you, "You");
        // "You places" -> "You place"
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\bYou (\\w+)").matcher(out);
        StringBuilder sb = new StringBuilder();
        while (m.find()) m.appendReplacement(sb, "You " + java.util.regex.Matcher.quoteReplacement(plain(m.group(1))));
        m.appendTail(sb);
        return sb.toString();
    }

    /** The verb for "you" from the verb for "he/she/it" ("places" -> "place", "launches" -> "launch"). */
    private static String plain(String verb) {
        if (verb.equals("has")) return "have";
        if (verb.endsWith("ches") || verb.endsWith("shes") || verb.endsWith("sses") || verb.endsWith("xes")) return verb.substring(0, verb.length() - 2);
        if (verb.endsWith("ies")) return verb.substring(0, verb.length() - 3) + "y";
        if (verb.endsWith("s") && !verb.endsWith("ss")) return verb.substring(0, verb.length() - 1);
        return verb;
    }

    private String resources(List<ResourceView> list) {
        if (list == null || list.isEmpty()) return "empty";
        Map<String, Integer> totals = new LinkedHashMap<>();
        for (ResourceView r : list) {
            String key = switch (r.kind()) {
                case "AMMO" -> r.ammunition() == null ? "Ammo" : r.ammunition().replace('_', ' ');
                case "FUEL" -> "Fuel";
                case "SUPPLY" -> "Supply";
                case "REPAIR" -> "Repair cards";
                default -> r.kind();
            };
            totals.merge(key, "REPAIR".equals(r.kind()) ? 1 : r.remaining(), Integer::sum);
        }
        StringBuilder sb = new StringBuilder();
        totals.forEach((k, v) -> sb.append(sb.length() > 0 ? "  ·  " : "").append(k).append(' ').append(v));
        return sb.toString();
    }

    private static boolean isResourceCard(CardDto c) {
        return Set.of("AMMUNITION", "FUEL", "SUPPLY", "REPAIR").contains(c.category());
    }

    private static boolean searchMatches(String effect, CardDto c) {
        boolean vehicle = "VEHICLE".equals(c.category());
        boolean tank = vehicle && TANKS.contains(c.vehicleClass());
        return switch (effect) {
            case "SEARCH_RESOURCES" -> isResourceCard(c);
            case "SEARCH_TANKS" -> tank;
            case "SEARCH_SUPPORT" -> vehicle && !tank;
            default -> false;
        };
    }

    private static Node scaled(Canvas canvas, double scale) {
        canvas.setScaleX(scale);
        canvas.setScaleY(scale);
        Group g = new Group(canvas);
        StackPane box = new StackPane(g);
        box.setMinSize(canvas.getWidth() * scale, canvas.getHeight() * scale);
        box.setPrefSize(canvas.getWidth() * scale, canvas.getHeight() * scale);
        box.setMaxSize(canvas.getWidth() * scale, canvas.getHeight() * scale);
        return box;
    }

    private static Label mark(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 8));
        l.setTextFill(Color.web(color));
        l.setStyle("-fx-border-color: " + color + "; -fx-border-radius: 3; -fx-padding: 0 3 0 3;");
        return l;
    }

    private static Label dim(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 11));
        l.setTextFill(Color.web(Ui.DIM));
        return l;
    }
}
