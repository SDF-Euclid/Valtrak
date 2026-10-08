package com.example.valtrak.UI.components;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

import java.util.function.Consumer;

public class CardArtRenderer {

    static final double W = 155;
    static final double H = 90;

    public static Canvas createVehicleArt(String vehicleClass, String vehicleNation) {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        boolean isAir = vehicleClass.equals("AIR_SUPERIORITY") || vehicleClass.equals("CLOSE_AIR_SUPPORT")
                || vehicleClass.equals("SPECIALIST");           // specialists are UAV teams for now
        drawSkyBackground(gc, isAir);
        drawCentered(gc, !isAir, sprite -> {
            switch (vehicleClass) {
                case "LIGHT_TANK"        -> drawTank(sprite, 0.78, false);
                case "MEDIUM_TANK"       -> drawTank(sprite, 0.88, false);
                case "HEAVY_TANK"        -> drawTank(sprite, 0.95, true);
                case "MAIN_BATTLE_TANK"  -> drawTank(sprite, 1.0,  true);
                case "ANTI_AIR"          -> drawAntiAir(sprite);
                case "RECON"             -> drawRecon(sprite);
                case "AIR_SUPERIORITY"   -> drawJet(sprite, true);
                case "CLOSE_AIR_SUPPORT" -> drawJet(sprite, false);
                case "SPECIALIST"        -> drawDrone(sprite);
                case "SUPPLY"            -> drawSupplyTruck(sprite);
                default                  -> drawGenericVehicle(sprite);
            }
        });
        drawNationAccent(gc, vehicleNation);
        return canvas;
    }

    public static Canvas createAmmoArt(DamageType damageType) {
        if (damageType == null) damageType = DamageType.KINETIC;
        final DamageType dt = damageType;
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawAmmoBackground(gc, dt);
        drawCentered(gc, false, sprite -> drawShell(sprite, dt));
        return canvas;
    }

    public static Canvas createFuelArt(int count) {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#14100a"));
        gc.fillRect(0, 0, W, H);
        drawCrateGrid(gc);
        drawCentered(gc, false, sprite -> drawFuel(sprite, count));
        return canvas;
    }

    public static Canvas createSupplyArt(int count) {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#100f0a"));
        gc.fillRect(0, 0, W, H);
        drawCrateGrid(gc);
        drawCentered(gc, false, sprite -> drawSupplyCrates(sprite, count));
        return canvas;
    }

    public static Canvas createRepairArt() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#0a1210"));
        gc.fillRect(0, 0, W, H);
        drawCrateGrid(gc);
        drawCentered(gc, false, CardArtRenderer::drawRepairKit);
        return canvas;
    }

    /** Item cards: an icon for the kind of effect (ERA, Artillery, Search or Draw). */
    public static Canvas createItemArt(String effect) {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#0d0f1a"));
        gc.fillRect(0, 0, W, H);
        drawCrateGrid(gc);
        String kind = CardTile.itemKind(effect);
        drawCentered(gc, false, sprite -> {
            switch (kind) {
                case "ERA"       -> drawEra(sprite);
                case "Artillery" -> drawBlast(sprite);
                case "Search"    -> drawMagnifier(sprite);
                case "Smoke"     -> drawSmoke(sprite);
                case "Jammer"    -> drawJammer(sprite);
                case "Camo"      -> drawCamo(sprite);
                case "Sabotage"  -> drawSabotage(sprite);
                case "Recycle"   -> drawRecycle(sprite);
                case "Rapid Deploy" -> drawRapid(sprite);
                case "Airdrop"   -> drawAirdrop(sprite);
                default          -> drawCardStack(sprite);
            }
        });
        return canvas;
    }

    /**
     * Paints the artwork onto a transparent sprite, finds its visible bounding box,
     * and draws it so that box is centred on the card. Ground vehicles also get a
     * ground strip placed at the base of the (centred) vehicle.
     */
    private static void drawCentered(GraphicsContext gc, boolean withGround, Consumer<GraphicsContext> painter) {
        Canvas spriteCanvas = new Canvas(W, H);
        painter.accept(spriteCanvas.getGraphicsContext2D());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        WritableImage sprite = spriteCanvas.snapshot(params, null);

        int w = (int) W, h = (int) H;
        int minX = w, minY = h, maxX = -1, maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if ((sprite.getPixelReader().getArgb(x, y) >>> 24) > 100) {
                    minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                }
            }
        }
        double dx = 0, dy = 0;
        if (maxX >= 0) {
            dx = Math.round(W / 2.0 - (minX + maxX + 1) / 2.0);
            dy = Math.round(H / 2.0 - (minY + maxY + 1) / 2.0);
        }
        if (withGround && maxX >= 0) {
            double groundTop = maxY + 1 + dy - 3;
            gc.setFill(Color.web("#162510"));
            gc.fillRect(0, groundTop, W, H - groundTop);
            gc.setFill(Color.web("#1f3318"));
            gc.fillRect(0, groundTop, W, 3);
        }
        gc.drawImage(sprite, dx, dy);
    }

    // ── Backgrounds ─────────────────────────────────────────────────────────

    private static void drawSkyBackground(GraphicsContext gc, boolean isAir) {
        Color c1 = isAir ? Color.web("#04101e") : Color.web("#091209");
        Color c2 = isAir ? Color.web("#0d2035") : Color.web("#121e0a");
        gc.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, c1), new Stop(1, c2)));
        gc.fillRect(0, 0, W, H);
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
        drawCrateGrid(gc);
    }

    private static void drawCrateGrid(GraphicsContext gc) {
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

    /** A small fixed-wing UAV seen from above: long straight wings, V tail, a sensor ball in the nose. */
    private static void drawDrone(GraphicsContext gc) {
        double cx = W / 2.0;
        double cy = H / 2.0;
        gc.setFill(Color.web("#6a7a8a"));
        double[] wx = {cx - 54, cx - 6, cx + 6, cx + 54, cx + 6, cx - 6};
        double[] wy = {cy - 2, cy - 7, cy - 7, cy - 2, cy + 3, cy + 3};
        gc.fillPolygon(wx, wy, 6);
        gc.setFill(Color.web("#8a98a8"));
        gc.fillOval(cx - 8, cy - 30, 16, 60);
        gc.setFill(Color.web("#5a6878"));
        gc.fillPolygon(new double[]{cx - 2, cx - 22, cx - 19, cx - 1}, new double[]{cy + 22, cy + 32, cy + 36, cy + 28}, 4);
        gc.fillPolygon(new double[]{cx + 2, cx + 22, cx + 19, cx + 1}, new double[]{cy + 22, cy + 32, cy + 36, cy + 28}, 4);
        gc.setFill(Color.web("#2e3a48"));
        gc.fillOval(cx - 5, cy - 31, 10, 10);
        gc.setFill(Color.web("#7ab8e8"));
        gc.fillOval(cx - 2, cy - 28, 4, 4);
        gc.setStroke(Color.web("#d0d8e0"));
        gc.setLineWidth(1.5);
        gc.strokeOval(cx - 9, cy + 28, 18, 5);
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

    // ── Fuel / repair ────────────────────────────────────────────────────────

    private static void drawFuel(GraphicsContext gc, int count) {
        if (count >= 20) {
            drawTanker(gc);
            return;
        }
        int drums = count <= 1 ? 1 : count <= 5 ? 2 : 3;
        double spacing = 34;
        double startX = W / 2.0 - (drums - 1) * spacing / 2.0 - 13;
        for (int i = 0; i < drums; i++) {
            drawDrum(gc, startX + i * spacing, 32);
        }
    }

    private static void drawDrum(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.web("#8f2f1a"));
        gc.fillRoundRect(x, y, 26, 36, 5, 5);
        gc.setFill(Color.web("#a8402a"));
        gc.fillRoundRect(x + 3, y + 2, 5, 32, 3, 3);
        gc.setFill(Color.web("#5e1d0f"));
        gc.fillRect(x, y + 9, 26, 3);
        gc.fillRect(x, y + 24, 26, 3);
        gc.setFill(Color.web("#b3513a"));
        gc.fillOval(x, y - 3, 26, 8);
        gc.setFill(Color.web("#3a1208"));
        gc.fillOval(x + 9, y - 1, 8, 4);
    }

    private static void drawTanker(GraphicsContext gc) {
        gc.setFill(Color.web("#1a1a1a"));
        gc.fillOval(26, 58, 20, 20);
        gc.fillOval(52, 58, 20, 20);
        gc.fillOval(104, 58, 20, 20);
        gc.setFill(Color.web("#333333"));
        gc.fillOval(30, 62, 12, 12);
        gc.fillOval(56, 62, 12, 12);
        gc.fillOval(108, 62, 12, 12);
        gc.setFill(Color.web("#8a8f95"));
        gc.fillRoundRect(18, 34, 88, 30, 15, 15);
        gc.setFill(Color.web("#b0b5ba"));
        gc.fillRoundRect(24, 38, 76, 6, 3, 3);
        gc.setFill(Color.web("#8f2f1a"));
        gc.fillRect(18, 52, 88, 4);
        gc.setFill(Color.web("#5a4828"));
        gc.fillRoundRect(106, 42, 30, 22, 3, 3);
        gc.setFill(Color.web("#3a5a6a"));
        gc.fillRect(110, 46, 22, 10);
    }

    private static void drawSupplyCrates(GraphicsContext gc, int count) {
        int crates = count <= 1 ? 1 : count <= 3 ? 2 : 3;
        double size = 34;
        double startX = W / 2.0 - (crates * size + (crates - 1) * 4) / 2.0;
        for (int i = 0; i < crates; i++) {
            double x = startX + i * (size + 4);
            gc.setFill(Color.web("#8a6a3a"));
            gc.fillRect(x, 40, size, size);
            gc.setStroke(Color.web("#5c4524"));
            gc.setLineWidth(2);
            gc.strokeRect(x + 1, 41, size - 2, size - 2);
            gc.strokeLine(x, 40, x + size, 40 + size);
            gc.strokeLine(x + size, 40, x, 40 + size);
        }
    }

    private static void drawRepairKit(GraphicsContext gc) {
        double cx = W / 2.0;
        gc.setStroke(Color.web("#707070"));
        gc.setLineWidth(3);
        gc.strokeArc(cx - 16, 24, 32, 22, 0, 180, javafx.scene.shape.ArcType.OPEN);
        gc.setFill(Color.web("#3f5a3a"));
        gc.fillRoundRect(cx - 38, 36, 76, 40, 6, 6);
        gc.setFill(Color.web("#2e4429"));
        gc.fillRect(cx - 38, 50, 76, 3);
        gc.setFill(Color.web("#506f49"));
        gc.fillRoundRect(cx - 35, 38, 70, 5, 3, 3);
        gc.setFill(Color.web("#a0a0a0"));
        gc.fillRect(cx - 6, 47, 12, 6);
        gc.setFill(Color.web("#e8e8e8"));
        gc.fillRect(cx - 5, 56, 10, 16);
        gc.fillRect(cx - 12, 59, 24, 10);
    }

    // ── Nation accent ─────────────────────────────────────────────────────────

    private static void drawEra(GraphicsContext gc) {
        gc.setFill(Color.web("#2f3b2a"));
        gc.fillRoundRect(40, 22, 76, 46, 4, 4);
        gc.setFill(Color.web("#566b49"));
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) gc.fillRect(44 + col * 18, 26 + row * 20, 14, 16);
        }
        gc.setStroke(Color.web("#e8b84b"));
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(40, 22, 76, 46, 4, 4);
    }

    private static void drawBlast(GraphicsContext gc) {
        gc.setFill(Color.web("#ff8c1a"));
        double cx = 78, cy = 45;
        double[] xs = new double[16], ys = new double[16];
        for (int i = 0; i < 16; i++) {
            double r = i % 2 == 0 ? 30 : 15, a = Math.PI * 2 * i / 16;
            xs[i] = cx + Math.cos(a) * r;
            ys[i] = cy + Math.sin(a) * r;
        }
        gc.fillPolygon(xs, ys, 16);
        gc.setFill(Color.web("#ffe066"));
        gc.fillOval(cx - 10, cy - 10, 20, 20);
    }

    private static void drawMagnifier(GraphicsContext gc) {
        gc.setStroke(Color.web("#9fb4d6"));
        gc.setLineWidth(5);
        gc.strokeOval(52, 20, 38, 38);
        gc.setLineWidth(7);
        gc.strokeLine(86, 54, 104, 72);
        gc.setFill(Color.web("#9fb4d633"));
        gc.fillOval(54, 22, 34, 34);
    }

    private static void drawSmoke(GraphicsContext gc) {
        gc.setFill(Color.web("#aab2bd"));
        gc.fillOval(40, 32, 44, 34);
        gc.fillOval(62, 22, 44, 38);
        gc.fillOval(74, 38, 40, 30);
        gc.setFill(Color.web("#cfd5dd"));
        gc.fillOval(58, 30, 36, 26);
    }

    private static void drawJammer(GraphicsContext gc) {
        gc.setStroke(Color.web("#7ab8e8"));
        gc.setLineWidth(3);
        for (int i = 1; i <= 3; i++) gc.strokeArc(78 - i * 13, 45 - i * 13, i * 26, i * 26, 30, 120, javafx.scene.shape.ArcType.OPEN);
        gc.setFill(Color.web("#e8b84b"));
        gc.fillRect(75, 44, 6, 28);
        gc.fillOval(72, 38, 12, 12);
    }

    private static void drawCamo(GraphicsContext gc) {
        gc.setFill(Color.web("#3d4a2c"));
        gc.fillRoundRect(36, 24, 84, 44, 8, 8);
        gc.setFill(Color.web("#6a7a45"));
        gc.fillOval(46, 30, 28, 18);
        gc.fillOval(84, 42, 28, 18);
        gc.setFill(Color.web("#2a3320"));
        gc.fillOval(70, 28, 22, 14);
        gc.fillOval(42, 48, 24, 14);
    }

    private static void drawSabotage(GraphicsContext gc) {
        gc.setFill(Color.web("#3a4666"));
        gc.fillRoundRect(44, 24, 36, 48, 5, 5);
        gc.setFill(Color.web("#4a5678"));
        gc.fillRoundRect(76, 24, 36, 48, 5, 5);
        gc.setStroke(Color.web("#ff4d4d"));
        gc.setLineWidth(5);
        gc.strokeLine(40, 28, 116, 68);
        gc.strokeLine(116, 28, 40, 68);
    }

    private static void drawRecycle(GraphicsContext gc) {
        gc.setStroke(Color.web("#5ec27a"));
        gc.setLineWidth(6);
        gc.strokeArc(46, 20, 64, 52, 40, 250, javafx.scene.shape.ArcType.OPEN);
        gc.setFill(Color.web("#5ec27a"));
        gc.fillPolygon(new double[]{100, 118, 114}, new double[]{22, 40, 20}, 3);
    }

    private static void drawAirdrop(GraphicsContext gc) {
        gc.setFill(Color.web("#e8e8e8"));
        gc.fillArc(46, 14, 64, 44, 0, 180, javafx.scene.shape.ArcType.ROUND);
        gc.setStroke(Color.web("#bbbbbb"));
        gc.setLineWidth(1.2);
        gc.strokeLine(48, 36, 76, 62);
        gc.strokeLine(108, 36, 80, 62);
        gc.strokeLine(78, 36, 78, 62);
        gc.setFill(Color.web("#8b6b3a"));
        gc.fillRect(66, 62, 24, 18);
        gc.setStroke(Color.web("#5a4524"));
        gc.strokeRect(66, 62, 24, 18);
    }

    private static void drawRapid(GraphicsContext gc) {
        gc.setFill(Color.web("#e8b84b"));
        for (int i = 0; i < 3; i++) {
            double x = 38 + i * 26;
            gc.fillPolygon(new double[]{x, x + 22, x, x + 8}, new double[]{24, 46, 68, 46}, 4);
        }
    }

    private static void drawCardStack(GraphicsContext gc) {
        for (int i = 0; i < 3; i++) {
            gc.setFill(i == 2 ? Color.web("#e8b84b") : Color.web("#3a4666"));
            gc.fillRoundRect(48 + i * 12, 20 + i * 6, 40, 54, 5, 5);
            gc.setStroke(Color.web("#0d0f1a"));
            gc.setLineWidth(1.5);
            gc.strokeRoundRect(48 + i * 12, 20 + i * 6, 40, 54, 5, 5);
        }
    }

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
