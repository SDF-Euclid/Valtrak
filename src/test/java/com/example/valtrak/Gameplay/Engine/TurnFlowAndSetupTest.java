package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnFlowAndSetupTest {

    private final TestWorld w = new TestWorld();

    /** 60 cards: 8 tanks x3, 5 other vehicles x3, 7 resources x3. */
    private List<Long> standardDeck() {
        List<Long> deck = new ArrayList<>();
        long[] ids = {TANK_COMMON, TANK_UNCOMMON, TANK_RARE, TANK_RARE_MBT, TANK_EPIC, TANK_LEGENDARY, TANK_LEGENDARY_HEAVY,
                TANK_COMMANDER, ANTI_AIR, RECON, SPECIALIST, RESUPPLY, AIR, APFSDS_5, HEAT_5, NATO_10, FUEL_5, FUEL_10, SUPPLY_1, SUPPLY_3};
        for (long id : ids) for (int i = 0; i < 3; i++) deck.add(id);
        return deck;
    }

    private boolean isTank(long cardId) {
        return w.catalog.find(cardId) instanceof VehicleSpec v && v.isTank();
    }

    // ── decks ────────────────────────────────────────────────────────────────

    @Test
    void aStandardDeckIsAllowed() {
        assertThat(w.engine.validateDeck(standardDeck())).isEmpty();
    }

    @Test
    void deckSizeLimitsAreSixtyToOneHundred() {
        List<Long> small = standardDeck().subList(0, 59);
        assertThat(w.engine.validateDeck(small)).anyMatch(p -> p.contains("at least 60"));

        List<Long> big = new ArrayList<>(standardDeck());
        for (int i = 0; i < 41; i++) big.add(TANK_COMMON);
        assertThat(w.engine.validateDeck(big)).anyMatch(p -> p.contains("at most 100"));
    }

    @Test
    void atMostThreeCopiesOfACard() {
        List<Long> deck = new ArrayList<>(standardDeck());
        deck.add(TANK_COMMON);
        assertThat(w.engine.validateDeck(deck)).anyMatch(p -> p.contains("at most 3 copies"));
    }

    @Test
    void aDeckNeedsTwelveTanks() {
        List<Long> deck = new ArrayList<>(standardDeck());
        deck.removeIf(id -> id == TANK_COMMANDER);                  // 21 tanks -> still fine
        deck.removeIf(id -> id == TANK_LEGENDARY_HEAVY);
        deck.removeIf(id -> id == TANK_LEGENDARY);
        deck.removeIf(id -> id == TANK_EPIC);                       // 12 tanks left
        assertThat(w.engine.validateDeck(deck)).noneMatch(p -> p.contains("tanks"));
        deck.removeIf(id -> id == TANK_RARE_MBT);
        assertThat(w.engine.validateDeck(deck)).anyMatch(p -> p.contains("at least 12 tanks"));
    }

    @Test
    void unknownCardsAreRejected() {
        List<Long> deck = new ArrayList<>(standardDeck());
        deck.set(0, 9999L);
        assertThat(w.engine.validateDeck(deck)).anyMatch(p -> p.contains("unknown card"));
    }

    // ── setup ────────────────────────────────────────────────────────────────

    @Test
    void newGameDealsSevenAndGuaranteesATankInEveryHand() {
        for (int seed = 0; seed < 50; seed++) {
            GameState s = w.engine.newGame(standardDeck(), standardDeck(), new Random(seed));
            for (int i = 0; i < 2; i++) {
                PlayerState p = s.player(i);
                int expected = 7 + Math.min(3, s.player(1 - i).mulligans);
                assertThat(p.hand).hasSize(expected);
                assertThat(p.hand.stream().anyMatch(this::isTank)).isTrue();
            }
        }
    }

    @Test
    void aMulliganGivesTheOpponentAnExtraCardUpToThree() {
        // a deck that is mostly non-tanks forces mulligans
        GameRules rules = GameRules.defaults();
        rules.minDeckSize = 10;
        rules.minTanksInDeck = 1;
        rules.maxCopies.replaceAll((level, n) -> 100);
        GameEngine engine = new GameEngine(rules, w.catalog);
        List<Long> weak = new ArrayList<>();
        for (int i = 0; i < 29; i++) weak.add(i % 2 == 0 ? FUEL_5 : ANTI_AIR);
        weak.add(TANK_COMMON);
        boolean sawMulligan = false;
        for (int seed = 0; seed < 40; seed++) {
            GameState s = engine.newGame(weak, standardDeck(), new Random(seed));
            assertThat(s.player(0).hand).anyMatch(this::isTank);
            int expectedOpponent = 7 + Math.min(3, s.player(0).mulligans);
            assertThat(s.player(1).hand).hasSize(expectedOpponent);
            sawMulligan |= s.player(0).mulligans > 0;
        }
        assertThat(sawMulligan).isTrue();
    }

    @Test
    void setupWaitsForBothStartingTanksThenTheFirstPlayerDraws() {
        GameState s = w.engine.newGame(standardDeck(), standardDeck(), new Random(7));
        assertThat(s.phase).isEqualTo(GameState.Phase.SETUP);
        int first = s.firstPlayer;
        int second = 1 - first;

        // nothing but placing a tank is allowed during setup
        assertThatThrownBy(() -> w.engine.apply(s, first, new EndTurn())).isInstanceOf(RuleViolationException.class);

        long tank0 = s.player(first).hand.stream().filter(this::isTank).findFirst().orElseThrow();
        long tank1 = s.player(second).hand.stream().filter(this::isTank).findFirst().orElseThrow();
        int handBefore = s.player(first).hand.size();
        w.engine.apply(s, first, new PlaceStartingTank(tank0));
        assertThat(s.phase).isEqualTo(GameState.Phase.SETUP);
        assertThatThrownBy(() -> w.engine.apply(s, first, new PlaceStartingTank(tank0)))
                .isInstanceOf(RuleViolationException.class);
        w.engine.apply(s, second, new PlaceStartingTank(tank1));

        assertThat(s.phase).isEqualTo(GameState.Phase.PLAYING);
        assertThat(s.activePlayer).isEqualTo(first);
        assertThat(s.player(first).hand).hasSize(handBefore - 1 + 1);   // placed one, drew one on turn 1
        assertThat(s.player(first).groups).hasSize(1);
        assertThat(s.player(first).groups.get(0).leader().faceUp).isFalse();
        assertThat(s.player(first).designationsLeft).isEqualTo(1);
    }

    @Test
    void theStartingVehicleMustBeATank() {
        GameState s = w.engine.newGame(standardDeck(), standardDeck(), new Random(3));
        PlayerState p = s.player(0);
        p.hand.add(ANTI_AIR);
        assertThatThrownBy(() -> w.engine.apply(s, 0, new PlaceStartingTank(ANTI_AIR)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("must be a tank");
    }

    // ── turns ────────────────────────────────────────────────────────────────

    @Test
    void endingYourTurnPassesPlayAndTheNextPlayerDrawsAndGetsTheirDesignationBack() {
        w.p(0).deck.add(TANK_COMMON);
        w.p(1).deck.addAll(List.of(FUEL_5, FUEL_1));
        w.p(1).designationsLeft = 0;
        w.act(0, new EndTurn());
        assertThat(w.s.activePlayer).isEqualTo(1);
        assertThat(w.p(1).hand).containsExactly(FUEL_5);
        assertThat(w.p(1).deck).containsExactly(FUEL_1);
        assertThat(w.p(1).designationsLeft).isEqualTo(1);
    }

    @Test
    void drawingFromAnEmptyDeckLosesTheGame() {
        w.p(0).deck.add(TANK_COMMON);
        w.act(0, new EndTurn());                                    // player 1 has no card to draw
        assertThat(w.s.phase).isEqualTo(GameState.Phase.FINISHED);
        assertThat(w.s.winner).isZero();
        assertThat(w.s.endReason).contains("no card to draw");
    }

    @Test
    void aRejectedActionLeavesTheStateUntouched() {
        StrikeGroup g = w.group(0, TANK_RARE, true);
        w.add(g, ANTI_AIR, true);
        w.hand(0, RECON, FUEL_5, TANK_COMMON);
        w.depot(0, SUPPLY_1);
        w.pool(g, FUEL_1);
        GameState before = w.s.copy();

        List<Action> bad = List.of(
                new Retreat(g.leader().id),                         // 2 Fuel needed, 1 available
                new RetreatGroup(g.id),
                new Move(g.vehicles.get(1).id, null),               // not a tank
                new Designate(TANK_COMMON, null),
                new Reveal(List.of(g.leader().id)));                // already face up
        for (Action a : bad) {
            assertThat(w.engine.isLegal(w.s, 0, a)).as(a.toString()).isFalse();
            assertThatThrownBy(() -> w.engine.apply(w.s, 0, a)).isInstanceOf(RuleViolationException.class);
            assertThat(snapshot(w.s)).isEqualTo(snapshot(before));
        }
    }

    @Test
    void legalActionsAreExactlyTheOnesTheEngineAccepts() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.hand(0, FUEL_5, TANK_UNCOMMON, ANTI_AIR);
        List<Action> legal = w.engine.legalActions(w.s, 0);
        assertThat(legal).contains(new EndTurn(), new Designate(FUEL_5, null), new Designate(FUEL_5, g.id),
                new Deploy(TANK_UNCOMMON, null));
        assertThat(legal).doesNotContain(new Deploy(ANTI_AIR, null));
        for (Action a : legal) assertThat(w.engine.isLegal(w.s, 0, a)).isTrue();
        assertThat(w.engine.legalActions(w.s, 1)).isEmpty();        // not player 1's turn
    }

    @Test
    void resigningGivesTheGameToTheOpponentEvenOutOfTurn() {
        w.engine.resign(w.s, 1);                                    // it is player 0's turn
        assertThat(w.s.phase).isEqualTo(GameState.Phase.FINISHED);
        assertThat(w.s.winner).isZero();
        assertThat(w.s.endReason).contains("resigned");
        assertThatThrownBy(() -> w.engine.resign(w.s, 0)).isInstanceOf(RuleViolationException.class);
    }

    private static String snapshot(GameState s) {
        StringBuilder sb = new StringBuilder();
        for (PlayerState p : s.players) {
            sb.append(p.hand).append(p.deck).append(p.discard).append(p.chips).append(p.designationsLeft);
            p.depot.forEach(r -> sb.append('d').append(r.id).append(':').append(r.remaining));
            for (StrikeGroup g : p.groups) {
                sb.append('g').append(g.id).append(g.formed).append(g.convoyMoved);
                g.pool.forEach(r -> sb.append('p').append(r.id).append(':').append(r.remaining));
                g.vehicles.forEach(v -> sb.append('v').append(v.id).append(v.hp).append(v.faceUp).append(v.breachStacks));
            }
        }
        return sb.append(s.activePlayer).append(s.phase).toString();
    }
}
