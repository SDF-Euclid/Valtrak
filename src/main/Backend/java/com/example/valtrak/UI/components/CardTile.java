package com.example.valtrak.UI.components;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CardTile {

    private final CardDto card;
    private final boolean favorite;
    private final boolean favoritesEnabled;
    private final Runnable onAdd;
    private final Runnable onToggleFavorite;

    public CardTile(CardDto card, boolean favorite, boolean favoritesEnabled, Runnable onAdd, Runnable onToggleFavorite) {
        this.card = card;
        this.favorite = favorite;
        this.favoritesEnabled = favoritesEnabled;
        this.onAdd = onAdd;
        this.onToggleFavorite = onToggleFavorite;
    }

    public VBox build() {
        StackPane art = new StackPane(artFor(card), buildStar());
        StackPane.setAlignment(art.getChildren().get(1), Pos.TOP_RIGHT);
        art.setMaxSize(CardArtRenderer.W, CardArtRenderer.H);

        Label nameLbl = new Label(card.name());
        nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(155);

        Label subLbl = new Label(buildSubtitle());
        subLbl.setFont(Font.font("Arial", 10));
        subLbl.setTextFill(Color.web("#888888"));
        subLbl.setWrapText(true);
        subLbl.setMaxWidth(155);

        Label statsLbl = new Label(buildStats());
        statsLbl.setFont(Font.font("Arial", 10));
        statsLbl.setTextFill(Color.web("#aaaaaa"));
        statsLbl.setWrapText(true);
        statsLbl.setMaxWidth(155);

        Label abilityLbl = null;
        if (card.ability() != null) {
            abilityLbl = new Label(("ITEM".equals(card.category()) ? "Effect: " : "Ability: ") + card.ability());
            abilityLbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            abilityLbl.setTextFill(Color.web("#7ab8e8"));
            abilityLbl.setWrapText(true);
            abilityLbl.setMaxWidth(155);
        }

        Label rarityLbl = new Label(card.level() != null ? card.level() : "");
        rarityLbl.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        rarityLbl.setTextFill(Color.web(rarityColor(card.level())));
        rarityLbl.setMaxWidth(155);

        Button addBtn = new Button("+ Add to Deck");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        String btnBase = "-fx-background-color: #16213e; -fx-text-fill: #e8b84b; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 3; " +
                "-fx-border-color: #e8b84b; -fx-border-radius: 3; -fx-border-width: 1; -fx-padding: 4 8 4 8;";
        String btnHover = "-fx-background-color: #0f3460; -fx-text-fill: #e8b84b; " +
                "-fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 3; " +
                "-fx-border-color: #e8b84b; -fx-border-radius: 3; -fx-border-width: 1; -fx-padding: 4 8 4 8;";
        addBtn.setStyle(btnBase);
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(btnHover));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(btnBase));
        addBtn.setOnAction(e -> onAdd.run());

        VBox tile = new VBox(5, art, nameLbl, subLbl, statsLbl);
        if (abilityLbl != null) tile.getChildren().add(abilityLbl);
        tile.getChildren().addAll(rarityLbl, addBtn);
        tile.setPadding(new Insets(8));
        tile.setAlignment(Pos.TOP_CENTER);
        tile.setMinWidth(176);
        tile.setMaxWidth(176);
        tile.setStyle(
                "-fx-background-color: #16213e; " +
                "-fx-border-color: " + rarityColor(card.level()) + "; " +
                "-fx-border-width: 1.5; -fx-border-radius: 5; -fx-background-radius: 5;"
        );
        return tile;
    }

    private Button buildStar() {
        Button star = new Button(favorite ? "★" : "☆");
        String color = favorite ? "#ffd700" : favoritesEnabled ? "#cccccc" : "#666666";
        String base = "-fx-background-color: #000000aa; -fx-text-fill: " + color + "; " +
                "-fx-font-size: 14px; -fx-background-radius: 0 0 0 6; -fx-padding: 1 6 1 6; -fx-cursor: hand;";
        star.setStyle(base);
        star.setOnMouseEntered(e -> star.setStyle(base.replace(color, "#ffd700")));
        star.setOnMouseExited(e -> star.setStyle(base));
        star.setOnAction(e -> onToggleFavorite.run());
        star.setFocusTraversable(false);
        return star;
    }

    /** Pictures already drawn, by what they depend on: drawing one is slow (a snapshot and a pixel scan), copying it is fast. */
    private static final java.util.Map<String, javafx.scene.image.WritableImage> ART = new java.util.HashMap<>();

    /** The picture on a card (a vehicle, a shell, a fuel drum...). Call on the JavaFX thread. */
    public static Canvas artFor(CardDto card) {
        Canvas real = realArt(card);
        if (real != null) return real;
        String key = card.category() + "|" + card.vehicleClass() + "|" + card.nation() + "|" + card.damageType()
                + "|" + card.count() + "|" + card.itemType();
        javafx.scene.image.WritableImage image = ART.computeIfAbsent(key, k -> {
            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            return drawArt(card).snapshot(params, null);
        });
        Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
        canvas.getGraphicsContext2D().drawImage(image, 0, 0);
        return canvas;
    }

    // ── real card art ─────────────────────────────────────────────────────────

    /** Real pictures by file name; an empty entry means "looked, there is none" (so the disk is only checked once). */
    private static final java.util.Map<String, java.util.Optional<javafx.scene.image.Image>> REAL = new java.util.HashMap<>();

    /**
     * The file name a card's real picture should have, from its name: lower case, anything that isn't a letter or digit
     * becomes a dash. "Leopard 2A7V" -> "leopard-2a7v", "Sho't Kal" -> "sho-t-kal", "5x 120mm HEAT Crate" -> "5x-120mm-heat-crate".
     */
    public static String artFileName(String cardName) {
        return cardName == null ? "" : cardName.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-+|-+$", "");
    }

    /**
     * A card's real picture, if one has been added: {@code art/cards/<file name>.png} (or .jpg) in the client's resources
     * (src/main/Backend/resources/art/cards/). It is scaled to fill the picture area and centred. Null if there is none, and
     * the generated placeholder is used instead.
     */
    private static Canvas realArt(CardDto card) {
        String file = artFileName(card.name());
        if (file.isEmpty()) return null;
        javafx.scene.image.Image image = REAL.computeIfAbsent(file, f -> {
            for (String ext : new String[]{".png", ".jpg"}) {
                var url = CardTile.class.getResource("/art/cards/" + f + ext);
                if (url != null) return java.util.Optional.of(new javafx.scene.image.Image(url.toExternalForm()));
            }
            return java.util.Optional.empty();
        }).orElse(null);
        if (image == null || image.isError()) return null;
        double w = CardArtRenderer.W, h = CardArtRenderer.H;
        double scale = Math.max(w / image.getWidth(), h / image.getHeight());     // fill the area, crop what sticks out
        double sw = w / scale, sh = h / scale;
        Canvas canvas = new Canvas(w, h);
        canvas.getGraphicsContext2D().drawImage(image, (image.getWidth() - sw) / 2, (image.getHeight() - sh) / 2, sw, sh, 0, 0, w, h);
        return canvas;
    }

    private static Canvas drawArt(CardDto card) {
        return switch (card.category()) {
            case "VEHICLE"    -> CardArtRenderer.createVehicleArt(
                    card.vehicleClass() != null ? card.vehicleClass() : "UNKNOWN", card.nation());
            case "AMMUNITION" -> CardArtRenderer.createAmmoArt(
                    card.damageType() != null ? DamageType.valueOf(card.damageType()) : null);
            case "FUEL"       -> CardArtRenderer.createFuelArt(card.count() != null ? card.count() : 1);
            case "SUPPLY"     -> CardArtRenderer.createSupplyArt(card.count() != null ? card.count() : 1);
            case "REPAIR"     -> CardArtRenderer.createRepairArt();
            case "ITEM"       -> CardArtRenderer.createItemArt(card.itemType());
            default           -> CardArtRenderer.createAmmoArt(null);
        };
    }

    private String buildSubtitle() {
        return switch (card.category()) {
            case "VEHICLE" -> (card.nation() != null ? card.nation() : "Unknown") + " · "
                    + (card.vehicleClass() != null ? card.vehicleClass().replace("_", " ") : "Vehicle");
            case "AMMUNITION" -> card.ammunition() != null ? "AMMO · " + card.ammunition() : "Ammunition";
            case "ITEM" -> "ITEM · " + itemKind(card.itemType()).toUpperCase();
            default -> card.itemType() != null ? "ITEM · " + card.itemType() : "Item";
        };
    }

    private String buildStats() {
        return switch (card.category()) {
            case "VEHICLE" -> "HP " + orZero(card.hp()) + "  ·  Armor " + orZero(card.armor());
            case "AMMUNITION" -> card.count() != null ? "Resupply ×" + card.count() : "";
            case "FUEL" -> card.count() != null ? "Fuel +" + card.count() : "";
            case "SUPPLY" -> card.count() != null ? "Supply +" + card.count() : "";
            case "REPAIR" -> card.repairAmount() == null ? ""
                    : card.repairAmount() >= 999 ? "Repairs: full HP" : "Repairs " + card.repairAmount() + " HP";
            case "ITEM" -> "";                                  // the effect is shown on the ability line
            default -> card.description() != null ? card.description() : "";
        };
    }

    /** ERA, Artillery, Search or Draw, from an item card's effect name. */
    public static String itemKind(String effect) {
        if (effect == null) return "Item";
        if (effect.startsWith("ERA")) return "ERA";
        if (effect.startsWith("ARTILLERY")) return "Artillery";
        if (effect.startsWith("SEARCH")) return "Search";
        if (effect.startsWith("DRAW")) return "Draw";
        if (effect.startsWith("SMOKE")) return "Smoke";
        if (effect.startsWith("JAMMER")) return "Jammer";
        if (effect.startsWith("CAMO")) return "Camo";
        if (effect.startsWith("SABOTAGE")) return "Sabotage";
        if (effect.startsWith("RECYCLE")) return "Recycle";
        if (effect.startsWith("RAPID")) return "Rapid Deploy";
        if (effect.startsWith("AIRDROP")) return "Airdrop";
        return "Item";
    }

    private static int orZero(Integer v) { return v != null ? v : 0; }

    public static String rarityColor(String levelName) {
        if (levelName == null) return "#555555";
        return switch (CardLevel.valueOf(levelName)) {
            case COMMON    -> "#6c757d";
            case UNCOMMON  -> "#28a745";
            case RARE      -> "#007bff";
            case EPIC      -> "#6f42c1";
            case LEGENDARY -> "#fd7e14";
            case COMMANDER -> "#ffd700";
        };
    }
}
