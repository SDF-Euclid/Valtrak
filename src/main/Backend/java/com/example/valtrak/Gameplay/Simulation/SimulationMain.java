package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Gameplay.Engine.GameEngine;
import com.example.valtrak.Gameplay.Engine.GameRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Runs many bot-vs-bot games and prints what happened. Run it from your IDE. Options are {@code key=value}
 * program arguments:
 * <pre>
 *   games=200  chips=5  deckSize=60  damage=100  designations=1  scouts=1  stalemate=0  fillAmmo=false  matchups=all|aggressive
 * </pre>
 * With no options it plays 200 games per matchup using the rulebook as written.
 */
public final class SimulationMain {

    public static void main(String[] args) {
        Map<String, String> opt = new HashMap<>();
        for (String a : args) {
            String[] kv = a.replaceFirst("^--", "").split("=", 2);
            opt.put(kv[0], kv.length > 1 ? kv[1] : "true");
        }
        int games = Integer.parseInt(opt.getOrDefault("games", "200"));
        boolean fillAmmo = Boolean.parseBoolean(opt.getOrDefault("fillAmmo", "false"));
        int deckSize = Integer.parseInt(opt.getOrDefault("deckSize", "60"));
        String which = opt.getOrDefault("matchups", "all");
        int scouts = Integer.parseInt(opt.getOrDefault("scouts", "1"));

        GameRules rules = GameRules.defaults();
        rules.stalemateRounds = Integer.parseInt(opt.getOrDefault("stalemate", "0"));
        rules.winChips = Integer.parseInt(opt.getOrDefault("chips", "5"));
        rules.damagePercent = Integer.parseInt(opt.getOrDefault("damage", "100"));
        rules.designationsPerTurn = Integer.parseInt(opt.getOrDefault("designations", "1"));
        System.out.println("Rules: win at " + rules.winChips + " chips, " + deckSize + "-card decks, damage "
                + rules.damagePercent + "%, stalemate rule "
                + (rules.stalemateRounds == 0 ? "off" : "after " + rules.stalemateRounds + " passive rounds")
                + (fillAmmo ? ", synthetic Ammo cards fill the gaps" : ", real cards only"));

        List<Matchup> matchups = new ArrayList<>(List.of(
                new Matchup(new GreedyBot(true), new GreedyBot(true)),
                new Matchup(new GreedyBot(false), new GreedyBot(false)),
                new Matchup(new GreedyBot(true), new GreedyBot(false)),
                new Matchup(new RandomBot(), new RandomBot())));
        if (which.equals("aggressive")) matchups = matchups.subList(0, 1);
        for (Matchup m : matchups) System.out.println(run(rules, m, games, fillAmmo, deckSize, scouts).describe());
    }

    record Matchup(Bot bot0, Bot bot1) {}

    /** Plays {@code games} games of the matchup and sums them up. */
    public static SimStats run(GameRules rules, Matchup m, int games, boolean fillAmmo, int deckSize, int scouts) {
        EnumCardCatalog catalog = new EnumCardCatalog(fillAmmo);
        GameEngine engine = new GameEngine(rules, catalog);
        SimStats stats = new SimStats(m.bot0().name() + " vs " + m.bot1().name());
        for (int i = 0; i < games; i++) {
            Random deckRng = new Random(1000 + i);
            List<Long> deck0 = SimDecks.standard(catalog, catalog.all(), deckSize, deckRng, scouts);
            List<Long> deck1 = SimDecks.standard(catalog, catalog.all(), deckSize, deckRng, scouts);
            stats.add(GameSimulator.play(engine, deck0, deck1, m.bot0(), m.bot1(), i, 20000));
        }
        return stats;
    }

    /** Totals over many games. */
    public static final class SimStats {
        private final String title;
        private int games, chipsWins, deckOuts, limits, firstPlayerWins, bot0Wins, bot1Wins;
        private long turns, attacks, destroyed, firstAttackTurn, withAttack;

        SimStats(String title) {
            this.title = title;
        }

        void add(GameReport r) {
            games++;
            switch (r.endedBy()) {
                case "CHIPS" -> chipsWins++;
                case "DECK_OUT" -> deckOuts++;
                default -> limits++;
            }
            if (r.winner() == 0) bot0Wins++;
            if (r.winner() == 1) bot1Wins++;
            if (r.winner() >= 0 && r.firstPlayerWon()) firstPlayerWins++;
            turns += r.turns();
            attacks += r.attacks()[0] + r.attacks()[1];
            destroyed += r.groupsDestroyed();
            if (r.firstAttackTurn() >= 0) {
                firstAttackTurn += r.firstAttackTurn();
                withAttack++;
            }
        }

        public int games() { return games; }
        public int chipsWins() { return chipsWins; }
        public int deckOuts() { return deckOuts; }
        public int limits() { return limits; }
        public int firstPlayerWins() { return firstPlayerWins; }
        public int bot0Wins() { return bot0Wins; }
        public double avgTurns() { return (double) turns / games; }
        public double avgAttacks() { return (double) attacks / games; }

        public String describe() {
            return String.format(Locale.ROOT,
                    "%-24s %d games | won by chips %3.0f%%, by deck-out %3.0f%%, unfinished %3.0f%% | "
                            + "first player won %3.0f%% | left bot won %3.0f%%, right bot %3.0f%% | "
                            + "avg turns %5.1f, attacks %5.1f, groups destroyed %4.1f, first attack on turn %s",
                    title, games, pct(chipsWins), pct(deckOuts), pct(limits), pct(firstPlayerWins),
                    pct(bot0Wins), pct(bot1Wins), (double) turns / games, (double) attacks / games,
                    (double) destroyed / games,
                    withAttack == 0 ? "never" : String.format(Locale.ROOT, "%.1f", (double) firstAttackTurn / withAttack));
        }

        private double pct(int n) {
            return 100.0 * n / games;
        }
    }
}
