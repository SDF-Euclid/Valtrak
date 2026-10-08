package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Gameplay.Engine.Action.Attack;
import com.example.valtrak.Gameplay.Engine.Action.EndTurn;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plays whole games with bots that pick random legal moves, and checks the rules'
 * invariants after every single action. This finds the bugs nobody thought to write a test for.
 */
class RandomPlayTest {

    private final TestWorld w = new TestWorld();

    private List<Long> deck() {
        List<Long> deck = new ArrayList<>();
        long[] ids = {TANK_COMMON, TANK_UNCOMMON, TANK_RARE, TANK_RARE_MBT, TANK_EPIC, TANK_LEGENDARY, TANK_LEGENDARY_HEAVY,
                TANK_COMMANDER, ANTI_AIR, SCOUT, UAV, RESUPPLY, AIR, APFSDS_5, HEAT_5, NATO_10, FUEL_5, FUEL_10, SUPPLY_1, SUPPLY_3};
        for (long id : ids) for (int i = 0; i < 3; i++) deck.add(id);
        return deck;
    }

    @Test
    void randomGamesNeverBreakTheRules() {
        int finished = 0, byChips = 0, byDeckOut = 0, unfinished = 0;
        for (int seed = 0; seed < 30; seed++) {
            Random rnd = new Random(seed);
            GameState s = w.engine.newGame(deck(), deck(), rnd);
            int steps = 0;
            while (s.phase != GameState.Phase.FINISHED && steps++ < 4000) {
                int player = pickPlayer(s);
                Action action = pickAction(s, player, rnd);
                w.engine.apply(s, player, action);
                checkInvariants(s, seed, steps);
            }
            if (s.phase == GameState.Phase.FINISHED) {
                finished++;
                if (s.player(s.winner).chips >= w.rules.winChips) byChips++; else byDeckOut++;
                assertThat(s.winner).isBetween(0, 1);
            } else {
                unfinished++;
            }
        }
        System.out.println("Random games: finished=" + finished + " (chips=" + byChips + ", deck-out=" + byDeckOut
                + "), still running after the step limit=" + unfinished);
        assertThat(finished).isGreaterThan(0);
    }

    private int pickPlayer(GameState s) {
        if (s.phase == GameState.Phase.SETUP) return s.player(0).placedStartingTank ? 1 : 0;
        return s.activePlayer;
    }

    private Action pickAction(GameState s, int player, Random rnd) {
        List<Action> legal = w.engine.legalActions(s, player);
        assertThat(legal).as("a player always has a legal move").isNotEmpty();
        List<Action> attacks = legal.stream().filter(a -> a instanceof Attack).toList();
        List<Action> others = legal.stream().filter(a -> !(a instanceof Attack) && !(a instanceof EndTurn)).toList();
        if (!attacks.isEmpty() && rnd.nextDouble() < 0.5) return attacks.get(rnd.nextInt(attacks.size()));
        if (!others.isEmpty() && rnd.nextDouble() < 0.85) return others.get(rnd.nextInt(others.size()));
        return legal.contains(new EndTurn()) ? new EndTurn() : legal.get(rnd.nextInt(legal.size()));
    }

    private void checkInvariants(GameState s, int seed, int step) {
        String where = " (seed " + seed + ", step " + step + ")";
        for (PlayerState p : s.players) {
            int cards = p.deck.size() + p.hand.size() + p.discard.size() + p.depot.size();
            for (StrikeGroup g : p.groups) cards += g.vehicles.size() + g.pool.size();
            assertThat(cards).as("every card is somewhere exactly once" + where).isEqualTo(60);

            assertThat(p.chips).isGreaterThanOrEqualTo(0);
            p.depot.forEach(r -> assertThat(r.remaining).as("depot card has something left" + where).isPositive());
            for (StrikeGroup g : p.groups) {
                assertThat(g.vehicles).as("a group is never empty" + where).isNotEmpty();
                assertThat(g.vehicles.size()).as("group size" + where).isLessThanOrEqualTo(w.rules.maxGroupSize);
                assertThat(((VehicleSpec) w.catalog.find(g.leader().cardId)).isTank()).as("leaders are tanks" + where).isTrue();
                int leaderLevel = ((VehicleSpec) w.catalog.find(g.leader().cardId)).level().ordinal();
                for (Vehicle v : g.vehicles) {
                    VehicleSpec spec = (VehicleSpec) w.catalog.find(v.cardId);
                    if (spec.isTank()) {
                        assertThat(spec.level().ordinal()).as("the Leader is the highest-rarity tank" + where).isLessThanOrEqualTo(leaderLevel);
                    }
                }
                int specialists = 0, resupply = 0, line = 0;
                for (int i = 1; i < g.vehicles.size(); i++) {
                    VehicleSpec spec = (VehicleSpec) w.catalog.find(g.vehicles.get(i).cardId);
                    if (spec.isSpecialist()) specialists++; else if (spec.isResupply()) resupply++; else line++;
                }
                assertThat(specialists).as("specialists" + where).isLessThanOrEqualTo(1);
                assertThat(resupply).as("resupply vehicles" + where).isLessThanOrEqualTo(1);
                assertThat(line).as("line vehicles" + where).isLessThanOrEqualTo(w.rules.maxLineVehicles);
                for (Vehicle v : g.vehicles) {
                    assertThat(v.hp).as("living vehicles have HP" + where).isPositive().isLessThanOrEqualTo(v.maxHp);
                    assertThat(v.breachStacks).isBetween(0, 3);
                }
                for (ResourceStack r : g.pool) {
                    assertThat(r.kind).as("pools hold only ammo and fuel" + where).isIn(ResourceKind.AMMO, ResourceKind.FUEL);
                    assertThat(r.remaining).as("pool card has something left" + where).isPositive();
                }
            }
        }
        if (s.phase == GameState.Phase.FINISHED) assertThat(s.winner).isBetween(0, 1);
    }
}
