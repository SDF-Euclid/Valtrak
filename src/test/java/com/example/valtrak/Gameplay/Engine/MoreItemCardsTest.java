package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_1;
import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Smoke Screen, Jammer, Camouflage, Sabotage, Recycle and Rapid Deployment (rulebook section 7c). */
class MoreItemCardsTest {

    private final TestWorld w = new TestWorld();
    private StrikeGroup mine;
    private StrikeGroup enemy;

    @BeforeEach
    void setUp() {
        w.p(0).deck.addAll(List.of(TANK_COMMON, TANK_COMMON, TANK_COMMON));
        w.p(1).deck.addAll(List.of(TANK_COMMON, TANK_COMMON, TANK_COMMON));
        mine = w.group(0, TANK_COMMON, true);
        enemy = w.group(1, TANK_RARE_MBT, true);
    }

    private PlayItem on(long card, Vehicle... vehicles) {
        return new PlayItem(card, java.util.Arrays.stream(vehicles).map(v -> v.id).toList(), List.of());
    }

    private PlayItem onGroup(long card, StrikeGroup g) {
        return new PlayItem(card, List.of(g.id), List.of());
    }

    private void passTurns() {
        w.act(0, new EndTurn());
        w.act(1, new EndTurn());
    }

    // ── smoke ────────────────────────────────────────────────────────────────

    @Test
    void smokedVehiclesCannotBeAttackedOrHitByArtilleryAndCannotAttack() {
        Vehicle leader = enemy.leader();
        w.hand(1, SMOKE_1);
        w.s.activePlayer = 1;
        w.act(1, on(SMOKE_1, leader));
        assertThat(leader.smoked).isTrue();
        assertThat(w.p(1).discard).containsExactly(SMOKE_1);
        w.act(1, new EndTurn());

        w.pool(mine, NATO_10);
        assertThatThrownBy(() -> w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, leader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("smoke");
        w.hand(0, ARTILLERY_1);
        assertThatThrownBy(() -> w.act(0, new PlayItem(ARTILLERY_1, List.of(leader.id), List.of())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("smoke");
        assertThat(leader.hp).isEqualTo(leader.maxHp);

        // the smoke ends when its owner's next turn starts, and then it can be attacked again
        w.act(0, new EndTurn());
        assertThat(leader.smoked).isFalse();
    }

    @Test
    void aVehicleInItsOwnSmokeCannotAttack() {
        Vehicle shooter = mine.leader();
        w.pool(mine, NATO_10);
        w.hand(0, SMOKE_1);
        w.act(0, on(SMOKE_1, shooter));
        assertThatThrownBy(() -> w.act(0, skirmish(mine, shooter, ATTACK_1, null, enemy.leader())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("own smoke");
    }

    @Test
    void smokeCoversAtMostItsNumberOfVehiclesAndOnlyYourOwn() {
        Vehicle b = w.add(mine, ANTI_AIR, true);
        w.hand(0, SMOKE_1, SMOKE_2);
        assertThatThrownBy(() -> w.act(0, on(SMOKE_1, mine.leader(), b))).isInstanceOf(RuleViolationException.class).hasMessageContaining("at most 1");
        assertThatThrownBy(() -> w.act(0, on(SMOKE_2, enemy.leader()))).isInstanceOf(RuleViolationException.class);
        assertThatThrownBy(() -> w.act(0, new PlayItem(SMOKE_2, List.of(), List.of()))).isInstanceOf(RuleViolationException.class);
        w.act(0, on(SMOKE_2, mine.leader(), b));
        assertThat(b.smoked && mine.leader().smoked).isTrue();
    }

    @Test
    void smokeDoesNotStopAReveal() {
        Vehicle hidden = enemy.leader();
        hidden.faceUp = false;
        hidden.smoked = true;
        Vehicle uav = w.add(mine, UAV, true);
        w.pool(mine, FUEL_5);
        w.act(0, new UseAbility(uav.id, List.of(hidden.id)));
        assertThat(hidden.faceUp).isTrue();
    }

    // ── jammer ───────────────────────────────────────────────────────────────

    @Test
    void aJammedGroupCannotBeRevealedByAnAbility() {
        enemy.leader().faceUp = false;
        Vehicle line = w.add(enemy, ANTI_AIR, false);
        w.hand(1, JAMMER_2);
        w.s.activePlayer = 1;
        w.act(1, onGroup(JAMMER_2, enemy));
        assertThat(enemy.jammerCardId).isEqualTo(JAMMER_2);
        w.act(1, new EndTurn());

        Vehicle uav = w.add(mine, UAV, true);
        w.pool(mine, FUEL_5);
        assertThatThrownBy(() -> w.act(0, new UseAbility(uav.id, List.of(line.id))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("jammed");
        assertThat(line.faceUp).isFalse();
        assertThat(w.fuelIn(mine)).isEqualTo(5);
    }

    @Test
    void theJammerCostsFuelEveryTurnAndFallsOffWhenItCannotBePaid() {
        w.pool(enemy, FUEL_5);
        enemy.jammerCardId = JAMMER_2;                               // upkeep 2
        w.act(0, new EndTurn());                                      // player 1's turn starts: pays 2
        assertThat(w.fuelIn(enemy)).isEqualTo(3);
        w.act(1, new EndTurn());
        w.act(0, new EndTurn());                                      // pays 2 more
        assertThat(w.fuelIn(enemy)).isEqualTo(1);
        assertThat(enemy.jammerCardId).isEqualTo(JAMMER_2);
        w.act(1, new EndTurn());
        ActionResult r = w.act(0, new EndTurn());                     // only 1 left: can't pay 2
        assertThat(enemy.jammerCardId).isZero();
        assertThat(w.p(1).discard).contains(JAMMER_2);
        assertThat(r.log).anyMatch(l -> l.contains("powers down"));
    }

    @Test
    void aNewJammerReplacesTheOldAndAJammerGoesWhenItsGroupIsDestroyed() {
        w.hand(0, JAMMER_2, JAMMER_1, ARTILLERY_1);
        w.act(0, onGroup(JAMMER_2, mine));
        w.act(0, onGroup(JAMMER_1, mine));
        assertThat(mine.jammerCardId).isEqualTo(JAMMER_1);
        assertThat(w.p(0).discard).containsExactly(JAMMER_2);

        mine.leader().hp = 1;
        w.hand(1, ARTILLERY_1);
        w.s.activePlayer = 1;
        w.act(1, new PlayItem(ARTILLERY_1, List.of(mine.leader().id), List.of()));
        assertThat(w.p(0).discard).contains(JAMMER_1);
    }

    // ── camouflage ───────────────────────────────────────────────────────────

    @Test
    void camouflageReducesTheRetreatFuelAndNeverBelowZero() {
        Vehicle v = mine.leader();                                    // Common: retreat costs 1
        w.pool(mine, FUEL_5);
        w.hand(0, CAMO_1);
        w.act(0, on(CAMO_1, v));
        assertThat(v.camoCardId).isEqualTo(CAMO_1);
        w.act(0, new Retreat(v.id));
        assertThat(w.fuelIn(mine)).isEqualTo(5);                      // free

        Vehicle epic = w.group(0, TANK_EPIC, true).leader();          // Epic: retreat costs 2; Thermal Camo takes 3 off
        StrikeGroup g2 = w.p(0).groups.get(1);
        w.pool(g2, FUEL_5);
        w.hand(0, CAMO_3);
        w.act(0, on(CAMO_3, epic));
        w.act(0, new Retreat(epic.id));
        assertThat(w.fuelIn(g2)).isEqualTo(5);
    }

    @Test
    void camouflageOnALeaderMakesAGroupRetreatCheaper() {
        Vehicle legendary = w.group(0, TANK_LEGENDARY, true).leader();   // group retreat = 2 x 3 = 6
        StrikeGroup g = w.p(0).groups.get(1);
        w.pool(g, FUEL_10);
        legendary.camoCardId = CAMO_1;                                   // leader cost 3 - 1 = 2, x2 = 4
        w.act(0, new RetreatGroup(g.id));
        assertThat(w.fuelIn(g)).isEqualTo(6);
    }

    @Test
    void camouflageIsDiscardedWithItsVehicleAndReturnsToHandWithIt() {
        Vehicle recon = w.add(mine, RECON, false);
        recon.camoCardId = CAMO_1;
        recon.hp = 1;
        w.hand(1, ARTILLERY_1);
        w.s.activePlayer = 1;
        recon.faceUp = true;
        w.act(1, new PlayItem(ARTILLERY_1, List.of(recon.id), List.of()));
        assertThat(w.p(0).discard).contains(RECON, CAMO_1);

        Vehicle anti = w.add(mine, ANTI_AIR, true);
        anti.camoCardId = CAMO_3;
        mine.leader().hp = 1;
        w.act(1, new EndTurn());
        w.act(0, new EndTurn());
        w.hand(1, ARTILLERY_1);
        w.act(1, new PlayItem(ARTILLERY_1, List.of(mine.leader().id), List.of()));
        assertThat(w.p(0).hand).contains(ANTI_AIR, CAMO_3);
    }

    // ── sabotage ─────────────────────────────────────────────────────────────

    @Test
    void sabotageMakesTheOpponentDiscardRandomCardsToThePublicPile() {
        w.hand(1, TANK_RARE, FUEL_5, SUPPLY_1, HEAT_5);
        w.hand(0, SABOTAGE_2);
        long seed = w.s.rngSeed;
        ActionResult r = w.act(0, new PlayItem(SABOTAGE_2, List.of(), List.of()));
        assertThat(w.p(1).hand).hasSize(2);
        assertThat(w.p(1).discard).hasSize(2);
        assertThat(w.s.rngSeed).isNotEqualTo(seed);
        assertThat(r.log).anyMatch(l -> l.contains("discards"));
        assertThat(w.s.activePlayer).isZero();
    }

    @Test
    void sabotageTakesWhatThereIsAndNeedsACardInTheirHandAndIsOncePerTurn() {
        w.hand(0, SABOTAGE_1, SABOTAGE_1);
        assertThatThrownBy(() -> w.act(0, new PlayItem(SABOTAGE_1, List.of(), List.of())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("no cards in hand");
        w.hand(1, FUEL_5);
        w.act(0, new PlayItem(SABOTAGE_1, List.of(), List.of()));
        assertThat(w.p(1).hand).isEmpty();
        w.hand(1, FUEL_5);
        assertThatThrownBy(() -> w.act(0, new PlayItem(SABOTAGE_1, List.of(), List.of())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("per turn");
    }

    @Test
    void sabotageDiscardsOnlyAsManyAsTheyHold() {
        w.hand(1, FUEL_5);
        w.hand(0, SABOTAGE_2);
        w.act(0, new PlayItem(SABOTAGE_2, List.of(), List.of()));
        assertThat(w.p(1).hand).isEmpty();
        assertThat(w.p(1).discard).containsExactly(FUEL_5);
    }

    // ── recycle ──────────────────────────────────────────────────────────────

    @Test
    void recycleReturnsResourceCardsFromYourDiscardPile() {
        w.p(0).discard.addAll(List.of(FUEL_5, FUEL_5, TANK_COMMON, HEAT_5));
        w.hand(0, RECYCLE_2);
        w.act(0, new PlayItem(RECYCLE_2, List.of(), List.of(FUEL_5, HEAT_5)));
        assertThat(w.p(0).hand).containsExactlyInAnyOrder(FUEL_5, HEAT_5);
        assertThat(w.p(0).discard).containsExactlyInAnyOrder(FUEL_5, TANK_COMMON, RECYCLE_2);
    }

    @Test
    void recycleChecksWhatItReturns() {
        w.p(0).discard.addAll(List.of(FUEL_5, TANK_COMMON));
        w.hand(0, RECYCLE_2, RECYCLE_1);
        assertThatThrownBy(() -> w.act(0, new PlayItem(RECYCLE_2, List.of(), List.of(TANK_COMMON))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("only returns resource");
        assertThatThrownBy(() -> w.act(0, new PlayItem(RECYCLE_2, List.of(), List.of(FUEL_5, FUEL_5))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("that many copies");
        assertThatThrownBy(() -> w.act(0, new PlayItem(RECYCLE_1, List.of(), List.of(FUEL_5, FUEL_5))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("at most 1");
        assertThat(w.p(0).hand).containsExactlyInAnyOrder(RECYCLE_2, RECYCLE_1);
    }

    // ── rapid deployment ─────────────────────────────────────────────────────

    @Test
    void rapidDeploymentDeploysWithoutTheFormationCostAndFormsTheGroup() {
        w.hand(0, RAPID_2, ANTI_AIR, RECON);
        w.act(0, new PlayItem(RAPID_2, List.of(mine.id), List.of(ANTI_AIR, RECON)));
        assertThat(mine.vehicles).hasSize(3);
        assertThat(mine.formed).isTrue();
        assertThat(w.p(0).hand).isEmpty();
        assertThat(w.p(0).discard).containsExactly(RAPID_2);
        assertThat(mine.vehicles.get(1).faceUp).as("deployed vehicles arrive face down").isFalse();
    }

    @Test
    void withoutItAFormationCostWouldHaveBeenRequired() {
        w.hand(0, ANTI_AIR);
        assertThatThrownBy(() -> w.act(0, new Deploy(ANTI_AIR, mine.id))).isInstanceOf(RuleViolationException.class).hasMessageContaining("Supply");
    }

    @Test
    void rapidDeploymentStillFollowsTheSlotRulesAndTheLimits() {
        Vehicle l1 = w.add(mine, ANTI_AIR, false);
        Vehicle l2 = w.add(mine, RECON, false);                       // both Line slots are full
        w.hand(0, RAPID_1, RAPID_2, AIR, SPECIALIST, SPECIALIST, UAV);
        assertThatThrownBy(() -> w.act(0, new PlayItem(RAPID_1, List.of(mine.id), List.of(AIR))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Line slots");
        assertThatThrownBy(() -> w.act(0, new PlayItem(RAPID_2, List.of(mine.id), List.of(SPECIALIST, SPECIALIST))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Specialist");
        assertThatThrownBy(() -> w.act(0, new PlayItem(RAPID_1, List.of(mine.id), List.of(SPECIALIST, UAV))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("at most 1");
        assertThatThrownBy(() -> w.act(0, new PlayItem(RAPID_1, List.of(mine.id), List.of(TANK_RARE))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("not in your hand");
        assertThat(mine.vehicles).hasSize(3);
        assertThat(w.p(0).hand).hasSize(6);
        w.act(0, new PlayItem(RAPID_1, List.of(mine.id), List.of(SPECIALIST)));
        assertThat(mine.vehicles).hasSize(4);
        assertThat(l1.hp + l2.hp).isPositive();
    }

    @Test
    void aBetterTankDeployedByRapidDeploymentBecomesTheLeader() {
        w.hand(0, RAPID_1, TANK_LEGENDARY);
        w.act(0, new PlayItem(RAPID_1, List.of(mine.id), List.of(TANK_LEGENDARY)));
        assertThat(w.catalog.vehicle(mine.leader().cardId).level().name()).isEqualTo("LEGENDARY");
    }

    // ── the game's own numbers ───────────────────────────────────────────────

    @Test
    void theGameDefaultsAreTheTunedOnes() {
        GameRules rules = GameRules.defaults();
        assertThat(rules.winChips).isEqualTo(3);
        assertThat(rules.damagePercent).isEqualTo(400);
        assertThat(rules.itemLimit(ItemEffect.ARTILLERY)).isEqualTo(1);
        assertThat(rules.itemLimit(ItemEffect.SABOTAGE)).isEqualTo(1);
        assertThat(rules.itemLimit(ItemEffect.DRAW)).isZero();
    }
}
