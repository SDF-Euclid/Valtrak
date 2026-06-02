package com.example.valtrak.UI.components;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

public class CardArtRenderer {

    static final double W = 155;
    static final double H = 90;

    public static Canvas createVehicleArt(String vehicleClass, String vehicleNation) {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        boolean isAir = vehicleClass.equals("AIR_SUPERIORITY") || vehicleClass.equals("CLOSE_AIR_SUPPORT");
        drawSkyBackground(gc, isAir);
        if (!isAir) drawGroundStrip(gc);
        switch (vehicleClass) {
            case "LIGHT_TANK"        -> drawTank(gc, 0.78, false);
            case "MEDIUM_TANK"       -> drawTank(gc, 0.88, false);
            case "HEAVY_TANK"        -> drawTank(gc, 0.95, true);
            case "MAIN_BATTLE_TANK"  -> drawTank(gc, 1.0,  true);
            case "ANTI_AIR"          -> drawAntiAir(gc);
            case "RECON"             -> drawRecon(gc);
            case "AIR_SUPERIORITY"   -> drawJet(gc, true);
            case "CLOSE_AIR_SUPPORT" -> drawJet(gc, false);
            case "SUPPLY"            -> drawSupplyTruck(gc);
            default                  -> drawGenericVehicle(gc);
        }
        drawNationAccent(gc, vehicleNation);
        return canvas;
    }

    public static Canvas createAmmoArt(DamageType damageType) {
        if (damageType == null) damageType = DamageType.KINETIC;
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawAmmoBackground(gc, damageType);
        drawShell(gc, damageType);
        return canvas;
    }

    // ── Backgrounds ─────────────────────────────────────────────────────────

    private static void drawSkyBackground(GraphicsContext gc, boolean isAir) {
        Color c1 = isAir ? Color.web("#04101e") : Color.web("#091209");
        Color c2 = isAir ? Color.web("#0d2035") : Color.web("#121e0a");
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, c1), new Stop(1, c2)));
        gc.fillRect(0, 0, W, H);
    }

    private static void drawGroundStrip(GraphicsContext gc) {
        gc.setFill(Color.web("#162510"));
        gc.fillRect(0, 70, W, H - 70);
        gc.setFill(Color.web("#1f3318"));
        gc.fillRect(0, 70, W, 3);
    }

    private static void drawAmmoBackground(GraphicsContext gc, DamageType dt) {
        Color bg = switch (dt) {
            case KINETIC   -> Color.web("#0e0e14");
            case CHEMICAL  -> Color.web("#0a140a");
            case EXPLOSIVE -> Color.web("#14100a");
            case ELECTRIC  -> Color.web("#08090f");
        };
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);
        gc.setStroke(Color.web("#1a1a22", 0.6));
        gc.setLineWidth(1);
        for (int x = 0; x < W; x += 20) gc.strokeLine(x, 0, x, H);
        for (int y = 0; y < H; y += 20) gc.strokeLine(0, y, W, y);
    }

    // ── Vehicles ─────────────────────────────────────────────────────────────

    private static void drawTank(GraphicsContext gc, double scale, boolean heavy) {
        double cx = W / 2.0 - 8;

        double tW = 128 * scale;
        double tH = heavy ? 15 : 12;
        double tX = cx - tW / 2;
        double tY = 55;
        gc.setFill(Color.web("#252525"));
        gc.fillRoundRect(tX, tY, tW, tH, 6, 6);
        gc.setFill(Color.web("#3a3a3a"));
        int wheels = heavy ? 5 : 4;
        for (int i = 1; i <= wheels; i++) {
            double wx = tX + i * (tW / (wheels + 1)) - 4;
            gc.fillOval(wx, tY + 3, 8, 8);
        }

        double bW = 108 * scale;
        double bH = heavy ? 19 : 15;
        double bX = cx - bW / 2;
        double bY = tY - bH + 4;
        gc.setFill(Color.web(heavy ? "#495533" : "#586540"));
        gc.fillRoundRect(bX, bY, bW, bH, 4, 4);
        gc.setFill(Color.web(heavy ? "#5a6840" : "#697748"));
        gc.fillRoundRect(bX + 3, bY + 2, bW - 6, 4, 2, 2);

        double turW = (heavy ? 56 : 45) * scale;
        double turH = heavy ? 17 : 13;
        double turX = cx - turW / 2 + 4;
        double turY = bY - turH + 5;
        gc.setFill(Color.web(heavy ? "#3e4b2c" : "#4d5c35"));
        gc.fillRoundRect(turX, turY, turW, turH, 4, 4);

        double barLen = heavy ? 46 : 35;
        gc.setFill(Color.web("#4a4a4a"));
        gc.fillRect(turX + turW, turY + turH / 2 - 2.5, barLen, heavy ? 5 : 4);
        if (heavy) {
            gc.setFill(Color.web("#606060"));
            gc.fillRect(turX + turW + barLen - 3, turY + turH / 2 - 4, 7, 9);
        }
        gc.setFill(Color.web("#2e3820"));
        gc.fillOval(turX + 6, turY + 2, 10, 7);
    }

    private static void drawAntiAir(GraphicsContext gc) {
        gc.setFill(Color.web("#252525"));
        gc.fillRoundRect(10, 58, 125, 12, 6, 6);
        gc.setFill(Color.web("#586540"));
        gc.fillRoundRect(18, 47, 110, 14, 3, 3);
        gc.setFill(Color.web("#4a5635"));
        gc.fillRoundRect(43, 37, 65, 13, 3, 3);
        gc.setFill(Color.web("#4a4a4a"));
        gc.save();
        gc.translate(62, 37);
        gc.rotate(-42);
        gc.fillRect(-2, -3, 30, 4);
        gc.restore();
        gc.save();
        gc.translate(84, 37);
        gc.rotate(-42);
        gc.fillRect(-2, -3, 30, 4);
        gc.restore();
        gc.setFill(Color.web("#363d28"));
        gc.fillOval(67, 28, 16, 12);
    }

    private static void drawRecon(GraphicsContext gc) {
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillOval(16, 55, 20, 20);
        gc.fillOval(50, 56, 18, 18);
        gc.fillOval(88, 56, 18, 18);
        gc.fillOval(120, 55, 20, 20);
        gc.setFill(Color.web("#333333"));
        gc.fillOval(19, 58, 14, 14);
        gc.fillOval(53, 59, 12, 12);
        gc.fillOval(91, 59, 12, 12);
        gc.fillOval(123, 58, 14, 14);

        gc.setFill(Color.web("#4a7060"));
        gc.fillRoundRect(18, 47, 122, 14, 5, 5);
        gc.setFill(Color.web("#3d5e50"));
        gc.fillRoundRect(58, 36, 42, 13, 6, 6);
        gc.setFill(Color.web("#4a4a4a"));
        gc.fillRect(93, 40, 20, 3);
        gc.setStroke(Color.web("#606060"));
        gc.setLineWidth(1.5);
        gc.strokeLine(83, 36, 87, 18);
    }

    private static void drawJet(GraphicsContext gc, boolean fighter) {
        double cx = W / 2.0;
        double cy = H / 2.0;

        gc.setFill(new LinearGradient(cx - 8, cy + 22, cx + 8, cy + 36, false, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ff4400", 0.5)), new Stop(1, Color.TRANSPARENT)));
        gc.fillOval(cx - 10, cy + 20, 20, 16);

        gc.setFill(Color.web("#4a5568"));
        double[] fx = {cx, cx - 7, cx - 5, cx + 5, cx + 7};
        double[] fy = {cy - 38, cy - 8, cy + 26, cy + 26, cy - 8};
        gc.fillPolygon(fx, fy, 5);

        gc.setFill(Color.web("#3a4558"));
        if (fighter) {
            double[] lwX = {cx - 4, cx - 44, cx - 5};
            double[] lwY = {cy - 4,  cy + 16,  cy + 24};
            gc.fillPolygon(lwX, lwY, 3);
            double[] rwX = {cx + 4, cx + 44, cx + 5};
            double[] rwY = {cy - 4,  cy + 16,  cy + 24};
            gc.fillPolygon(rwX, rwY, 3);
            gc.setFill(Color.web("#2a3548"));
            double[] ltX = {cx - 4, cx - 18, cx - 8};
            double[] ltY = {cy + 20, cy + 26, cy + 32};
            gc.fillPolygon(ltX, ltY, 3);
            double[] rtX = {cx + 4, cx + 18, cx + 8};
            double[] rtY = {cy + 20, cy + 26, cy + 32};
            gc.fillPolygon(rtX, rtY, 3);
        } else {
            double[] lwX = {cx - 4, cx - 42, cx - 30, cx - 5};
            double[] lwY = {cy,      cy + 13,  cy + 24,  cy + 16};
            gc.fillPolygon(lwX, lwY, 4);
            double[] rwX = {cx + 4, cx + 42, cx + 30, cx + 5};
            double[] rwY = {cy,      cy + 13,  cy + 24,  cy + 16};
            gc.fillPolygon(rwX, rwY, 4);
            gc.setFill(Color.web("#666070"));
            gc.fillRect(cx - 36, cy + 10, 10, 3);
            gc.fillRect(cx + 26, cy + 10, 10, 3);
        }

        gc.setFill(Color.web("#7ab8e8"));
        double[] cX = {cx, cx - 4, cx + 4};
        double[] cY = {cy - 34, cy - 22, cy - 22};
        gc.fillPolygon(cX, cY, 3);

        gc.setFill(Color.web(fighter ? "#ff4400" : "#ff8800", 0.8));
        gc.fillOval(cx - 5, cy + 23, 10, 7);
    }

    private static void drawSupplyTruck(GraphicsContext gc) {
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillOval(14, 56, 22, 22);
        gc.fillOval(105, 56, 22, 22);
        gc.setFill(Color.web("#333333"));
        gc.fillOval(18, 60, 14, 14);
        gc.fillOval(109, 60, 14, 14);

        gc.setFill(Color.web("#7a6040"));
        gc.fillRect(16, 35, 100, 26);
        gc.setFill(Color.web("#6a5030"));
        gc.fillRect(16, 35, 100, 4);
        gc.fillRect(16, 35, 3, 26);
        gc.setStroke(Color.web("#8a7050"));
        gc.setLineWidth(1);
        gc.strokeLine(66, 35, 66, 61);
        gc.strokeLine(16, 48, 116, 48);

        gc.setFill(Color.web("#5a4828"));
        gc.fillRoundRect(116, 43, 28, 18, 3, 3);
        gc.setFill(Color.web("#3a5a6a"));
        gc.fillRect(119, 46, 22, 10);
        gc.setFill(Color.web("#444428"));
        gc.fillRect(119, 57, 22, 4);
    }

    private static void drawGenericVehicle(GraphicsContext gc) {
        gc.setFill(Color.web("#252525"));
        gc.fillRoundRect(12, 57, 128, 12, 5, 5);
        gc.setFill(Color.web("#5a5a70"));
        gc.fillRoundRect(22, 44, 108, 16, 4, 4);
        gc.setFill(Color.web("#4a4a60"));
        gc.fillOval(55, 28, 42, 20);
        gc.setFill(Color.web("#6a6a80"));
        gc.fillOval(60, 31, 32, 14);
        gc.setFill(Color.web("#3a3a50"));
        gc.fillRect(75, 44, 4, 6);
    }

    // ── Ammo ─────────────────────────────────────────────────────────────────

    private static void drawShell(GraphicsContext gc, DamageType dt) {
        String tip  = switch (dt) {
            case KINETIC   -> "#c0c0c0";
            case CHEMICAL  -> "#40a040";
            case EXPLOSIVE -> "#e0a020";
            case ELECTRIC  -> "#4060e0";
        };
        String body = switch (dt) {
            case KINETIC   -> "#707070";
            case CHEMICAL  -> "#206020";
            case EXPLOSIVE -> "#806010";
            case ELECTRIC  -> "#203080";
        };
        String glow = switch (dt) {
            case KINETIC   -> "#c0c0c022";
            case CHEMICAL  -> "#40a04022";
            case EXPLOSIVE -> "#e0a02022";
            case ELECTRIC  -> "#4060e033";
        };

        double cx = W / 2.0;
        double cy = H / 2.0 + 10;

        gc.setFill(Color.web(glow));
        gc.fillOval(cx - 22, cy - 48, 44, 44);

        gc.setFill(Color.web("#9a7a30"));
        gc.fillRoundRect(cx - 8, cy - 5, 16, 32, 3, 3);

        gc.setFill(Color.web(body));
        gc.fillRoundRect(cx - 7, cy - 28, 14, 28, 2, 2);

        double[] tipX = {cx, cx - 7, cx + 7};
        double[] tipY = {cy - 42, cy - 28, cy - 28};
        gc.setFill(Color.web(tip));
        gc.fillPolygon(tipX, tipY, 3);

        gc.setFill(Color.web("#cc8800"));
        gc.fillRect(cx - 8, cy - 7, 16, 4);
    }

    // ── Nation accent ─────────────────────────────────────────────────────────

    private static void drawNationAccent(GraphicsContext gc, String nation) {
        if (nation == null) return;
        String color = switch (nation) {
            case "United States" -> "#003087";
            case "Russia"        -> "#CC2020";
            case "Germany"       -> "#CCAA00";
            default              -> "#404040";
        };
        gc.setFill(Color.web(color, 0.85));
        gc.fillRect(0, 0, 4, H);
    }
}
