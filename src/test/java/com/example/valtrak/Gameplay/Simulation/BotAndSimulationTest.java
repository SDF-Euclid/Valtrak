package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Gameplay.Engine.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class BotAndSimulationTest {

    private final EnumCardCatalog catalog = new EnumCardCatalog(true);

    private GameEngine engine(int stalemateRounds) {
        GameRules rules = GameRules.defaults();
        rules.stalemateRounds = stalemateRounds;
        return new GameEngine(rules, catalog);
    }

    private List<Long> deck(int size, long seed) {
        return SimDecks.standard(catalog, catalog.all(), size, new Random(seed));
    }

    // ── catalog and decks ────────────────────────────────────────────────────

    @Test
    void theEnumCatalogHasEveryCardKindWithStableIds() {
        assertThat(catalog.all()).hasSize(7 + 3 + 7 + 60 + 4 + 4 + 3 + 41);   // tanks, recon, UAV teams, ammo, fuel, repair, supply, item cards
        assertThat(catalog.find(1)).isInstanceOf(VehicleSpec.class);
        assertThat(new EnumCardCatalog(true).find(1).name()).isEqualTo(catalog.find(1).name());
        assertThat(catalog.all()).anyMatch(c -> c instanceof ResourceSpec r && r.kind() == ResourceKind.SUPPLY);
        // every ammunition type has real cards now, so there is nothing for the synthetic fill to add
        assertThat(catalog.all()).noneMatch(c -> c.name().contains("synthetic"));
        assertThat(catalog.all()).hasSameSizeAs(new EnumCardCatalog(false).all());
    }

    @Test
    void everyTankCanFireSomethingWhenAmmoGapsAreFilled() {
        for (CardSpec c : catalog.all()) {
            if (!(c instanceof VehicleSpec v)) continue;
            for (AttackSpec a : v.attacks()) {
                boolean ammoExists = catalog.all().stream().anyMatch(x -> x instanceof ResourceSpec r
                        && r.kind() == ResourceKind.AMMO && a.weapon().getCompatibleAmmunition().contains(r.ammunition()));
                assertThat(ammoExists).as(v.name() + " " + a.name()).isTrue();
            }
        }
    }

    @Test
    void everyAmmunitionTypeHasCardsInAllFourSizes() {
        var real = new EnumCardCatalog(false);
        for (var ammo : com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition.values()) {
            var sizes = real.all().stream()
                    .filter(c -> c instanceof ResourceSpec r && r.kind() == ResourceKind.AMMO && r.ammunition() == ammo)
                    .map(c -> ((ResourceSpec) c).amount()).sorted().toList();
            assertThat(sizes).as(ammo.name()).containsExactly(1, 5, 10, 20);
        }
        assertThat(real.all()).noneMatch(c -> c.name().contains("synthetic"));
    }

    @Test
    void theNewCardsHaveAbilitiesScaledByRarity() {
        var uavs = catalog.all().stream().filter(c -> c instanceof VehicleSpec v && v.isSpecialist() && v.ability() != null)
                .map(c -> (VehicleSpec) c).toList();
        assertThat(uavs).hasSize(7);
        assertThat(uavs).allMatch(v -> v.attacks().isEmpty());
        for (VehicleSpec v : uavs) {
            int expected = switch (v.level()) {
                case COMMON, UNCOMMON -> 1;
                case RARE, EPIC -> 2;
                default -> 3;
            };
            assertThat(v.ability().power()).as(v.name()).isEqualTo(expected);
        }
        var recon = catalog.all().stream().filter(c -> c instanceof VehicleSpec v && v.vehicleClass().name().equals("RECON"))
                .map(c -> (VehicleSpec) c).toList();
        assertThat(recon).hasSize(3);
        assertThat(recon).allMatch(v -> v.ability() != null && !v.attacks().isEmpty());
    }

    @Test
    void cautiousBotsFightWhenTheDecksHaveRevealCardsEvenWithoutAStalemateRule() {
        int attacks = 0;
        for (int seed = 0; seed < 6; seed++) {
            GameReport r = GameSimulator.play(engine(0), SimDecks.standard(catalog, catalog.all(), 80, new Random(seed), 1),
                    SimDecks.standard(catalog, catalog.all(), 80, new Random(seed + 40), 1),
                    new GreedyBot(false), new GreedyBot(false), seed, 20000);
            attacks += r.attacks()[0] + r.attacks()[1];
        }
        assertThat(attacks).isGreaterThan(50);
    }

    @Test
    void simulationDecksAreLegalAtEverySize() {
        GameEngine engine = engine(0);
        for (int size : new int[]{60, 80, 100}) {
            List<Long> deck = deck(size, size);
            assertThat(deck).hasSize(size);
            assertThat(engine.validateDeck(deck)).as("deck of " + size).isEmpty();
        }
    }

    // ── bots ─────────────────────────────────────────────────────────────────

    @Test
    void botsAlwaysPlayLegalMovesAndGamesFinish() {
        GameEngine engine = engine(3);
        Bot[] bots = {new GreedyBot(true), new GreedyBot(false), new RandomBot()};
        for (Bot a : bots) {
            for (Bot b : bots) {
                for (int seed = 0; seed < 3; seed++) {
                    // GameSimulator throws if a bot ever picks an illegal action
                    GameReport r = GameSimulator.play(engine, deck(60, seed), deck(60, seed + 50), a, b, seed, 20000);
                    assertThat(r.endedBy()).as(a.name() + " vs " + b.name() + " seed " + seed).isNotEqualTo("LIMIT");
                }
            }
        }
    }

    @Test
    void decksWithItemCardsStayLegalAndBotsPlayThemWithoutBreakingTheRules() {
        GameEngine engine = engine(0);
        int itemsPlayed = 0;
        for (int seed = 0; seed < 8; seed++) {
            List<Long> d0 = SimDecks.standard(catalog, catalog.all(), 100, new Random(seed), 1, 2);
            List<Long> d1 = SimDecks.standard(catalog, catalog.all(), 100, new Random(seed + 9), 1, 2);
            assertThat(engine.validateDeck(d0)).isEmpty();
            assertThat(d0).anyMatch(id -> catalog.find(id) instanceof ItemSpec);
            GameReport r = GameSimulator.play(engine, d0, d1, new GreedyBot(true), new GreedyBot(false), seed, 20000);
            assertThat(r.endedBy()).isNotEqualTo("LIMIT");
            itemsPlayed += r.itemsPlayed();
        }
        assertThat(itemsPlayed).as("the bots do play item cards").isGreaterThan(20);
    }

    @Test
    void randomBotsCanPlayItemsToo() {
        GameEngine engine = engine(0);
        for (int seed = 0; seed < 4; seed++) {
            List<Long> d0 = SimDecks.standard(catalog, catalog.all(), 100, new Random(seed), 1, 1);
            List<Long> d1 = SimDecks.standard(catalog, catalog.all(), 100, new Random(seed + 9), 1, 1);
            GameSimulator.play(engine, d0, d1, new RandomBot(), new RandomBot(), seed, 20000);
        }
    }

    @Test
    void aggressiveBotsActuallyFightAndDestroyGroups() {
        GameEngine engine = engine(0);
        int attacks = 0, destroyed = 0;
        for (int seed = 0; seed < 8; seed++) {
            GameReport r = GameSimulator.play(engine, deck(100, seed), deck(100, seed + 9), new GreedyBot(true), new GreedyBot(true), seed, 20000);
            attacks += r.attacks()[0] + r.attacks()[1];
            destroyed += r.groupsDestroyed();
        }
        assertThat(attacks).isGreaterThan(50);       // games are short now (3 chips, damage x4)
        assertThat(destroyed).isGreaterThan(15);
    }

    /**
     * Records a finding about the rules as written: in decks without UAV or Recon cards, if neither player ever has to reveal,
     * nobody can be attacked and the game is decided by deck-out. If this test starts failing, a rule that forces a reveal was added.
     */
    @Test
    void cautiousBotsNeverFightWithoutRevealCardsOrAStalemateRule() {
        for (int seed = 0; seed < 4; seed++) {
            GameReport stalled = GameSimulator.play(engine(0),
                    SimDecks.standard(catalog, catalog.all(), 60, new Random(seed), 0),     // no UAV or Recon cards
                    SimDecks.standard(catalog, catalog.all(), 60, new Random(seed + 7), 0),
                    new GreedyBot(false), new GreedyBot(false), seed, 20000);
            assertThat(stalled.attacks()[0] + stalled.attacks()[1]).isZero();
            assertThat(stalled.endedBy()).isEqualTo("DECK_OUT");
            assertThat(stalled.firstPlayerWon()).as("the first player draws first, so they run out first").isFalse();

            GameReport fought = GameSimulator.play(engine(3),
                    SimDecks.standard(catalog, catalog.all(), 60, new Random(seed), 0),
                    SimDecks.standard(catalog, catalog.all(), 60, new Random(seed + 7), 0),
                    new GreedyBot(false), new GreedyBot(false), seed, 20000);
            assertThat(fought.attacks()[0] + fought.attacks()[1]).isPositive();
        }
    }

    @Test
    void aBotPlacesItsBestTankDuringSetup() {
        GameEngine engine = engine(0);
        GameState s = engine.newGame(deck(60, 1), deck(60, 2), new Random(3));
        Action a = new GreedyBot(false).choose(engine, s, 0, new Random(0));
        assertThat(a).isInstanceOf(Action.PlaceStartingTank.class);
        assertThat(engine.isLegal(s, 0, a)).isTrue();
    }

    @Test
    void simulationStatsAddUp() {
        GameRules rules = GameRules.defaults();
        var stats = SimulationMain.run(rules, new SimulationMain.Matchup(new GreedyBot(true), new GreedyBot(true)), 6, true, 100, 1);
        assertThat(stats.games()).isEqualTo(6);
        assertThat(stats.chipsWins() + stats.deckOuts() + stats.limits()).isEqualTo(6);
        assertThat(stats.describe()).contains("aggressive vs aggressive");
    }
}
