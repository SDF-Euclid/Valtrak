package com.example.valtrak.UI.components;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CardTile {

    private final Card card;
    private final Runnable onAdd;

    public CardTile(Card card, Runnable onAdd) {
        this.card = card;
        this.onAdd = onAdd;
    }

    public VBox build() {
        Canvas art = buildArt();

        Label nameLbl = new Label(card.getName());
        nameLbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setWrapText(true);
        nameLbl.setMaxWidth(149);

        Label subLbl = new Label(buildSubtitle());
        subLbl.setFont(Font.font("Arial", 10));
        subLbl.setTextFill(Color.web("#888888"));
        subLbl.setWrapText(true);
        subLbl.setMaxWidth(149);

        Label statsLbl = new Label(buildStats());
        statsLbl.setFont(Font.font("Arial", 10));
        statsLbl.setTextFill(Color.web("#aaaaaa"));

        Label rarityLbl = new Label(card.getLevel() != null ? card.getLevel().name() : "");
        rarityLbl.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        rarityLbl.setTextFill(Color.web(rarityColor(card.getLevel())));

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

        VBox tile = new VBox(5, art, nameLbl, subLbl, statsLbl, rarityLbl, addBtn);
        tile.setPadding(new Insets(8));
        tile.setAlignment(Pos.TOP_LEFT);
        tile.setMinWidth(165);
        tile.setMaxWidth(165);
        tile.setStyle(
                "-fx-background-color: #16213e; " +
                "-fx-border-color: " + rarityColor(card.getLevel()) + "; " +
                "-fx-border-width: 1.5; -fx-border-radius: 5; -fx-background-radius: 5;"
        );
        return tile;
    }

    private Canvas buildArt() {
        if (card instanceof GroundVehicleCard v) {
            String vc = v.getVehicleClass() != null ? v.getVehicleClass().getClassName() : "UNKNOWN";
            return CardArtRenderer.createVehicleArt(vc, v.getVehicleNation());
        }
        if (card instanceof AmmunitionCard a && a.getAmmunition() != null) {
            return CardArtRenderer.createAmmoArt(a.getAmmunition().getDamageType());
        }
        return CardArtRenderer.createAmmoArt(null);
    }

    private String buildSubtitle() {
        if (card instanceof GroundVehicleCard v) {
            String nation = v.getVehicleNation() != null ? v.getVehicleNation() : "Unknown";
            String vc = v.getVehicleClass() != null
                    ? v.getVehicleClass().getClassName().replace("_", " ")
                    : "Vehicle";
            return nation + " · " + vc;
        }
        if (card instanceof AmmunitionCard a && a.getAmmunition() != null) {
            return "AMMO · " + a.getAmmunition().name();
        }
        return "Item";
    }

    private String buildStats() {
        if (card instanceof GroundVehicleCard v) {
            int hp    = v.getVehicleHP()    != null ? v.getVehicleHP()    : 0;
            int armor = v.getVehicleArmor() != null ? v.getVehicleArmor() : 0;
            return "HP " + hp + "  ·  Armor " + armor;
        }
        if (card instanceof AmmunitionCard a && a.getCount() != null) {
            return "Resupply ×" + a.getCount();
        }
        return "";
    }

    private String rarityColor(CardLevel level) {
        if (level == null) return "#555555";
        return switch (level) {
            case COMMON    -> "#6c757d";
            case UNCOMMON  -> "#28a745";
            case RARE      -> "#007bff";
            case EPIC      -> "#6f42c1";
            case LEGENDARY -> "#fd7e14";
            case COMMANDER -> "#ffd700";
        };
    }
}
