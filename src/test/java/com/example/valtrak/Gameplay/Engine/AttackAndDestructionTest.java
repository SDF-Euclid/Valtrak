package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_1;
import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_2;
import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttackAndDestructionTest {

    private final TestWorld w = new TestWorld();
    /** Player 1's group: a face-up tank with a Line vehicle. */
    private StrikeGroup enemy;
    private Vehicle enemyLeader;

    @BeforeEach
    void enemyDeck() {
        // both players need cards to draw when turns pass
        w.p(0).deck.add(TANK_COMMON);
        w.p(1).deck.add(TANK_COMMON);
        enemy = w.group(1, TANK_RARE_MBT, true);
        enemyLeader = enemy.leader();
    }

    // ── skirmish and assault ─────────────────────────────────────────────────

    @Test
    void aSkirmishCanBeMadeWithTheLeaderFaceDown() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        Vehicle aa = w.add(mine, ANTI_AIR, true);
        w.pool(mine, NATO_10);

        w.act(0, skirmish(mine, aa, ATTACK_1, null, enemyLeader));

        assertThat(enemyLeader.hp).isLessThan(200);
        assertThat(w.ammoIn(mine, Ammunition.NATO_127x99MM)).isEqualTo(9);   // 1 ammo spent
        assertThat(w.s.activePlayer).isEqualTo(1);                           // attacking ends the turn
    }

    @Test
    void aCombinedAssaultNeedsTheLeaderFaceUp() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        Vehicle aa = w.add(mine, ANTI_AIR, true);
        Vehicle recon = w.add(mine, RECON, true);
        w.pool(mine, NATO_10);
        Attack assault = new Attack(mine.id, List.of(
                new AttackChoice(aa.id, ATTACK_1, null, enemyLeader.id),
                new AttackChoice(recon.id, ATTACK_1, null, enemyLeader.id)));

        assertThatThrownBy(() -> w.act(0, assault))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Leader to be face up");
        assertThat(w.ammoIn(mine, Ammunition.NATO_127x99MM)).isEqualTo(10);  // nothing was spent

        mine.leader().faceUp = true;
        w.act(0, assault);
        assertThat(w.ammoIn(mine, Ammunition.NATO_127x99MM)).isEqualTo(8);
    }

    @Test
    void anAssaultPaysTheSumOfEveryAttackAndKeepsTheRest() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        Vehicle second = w.add(mine, TANK_UNCOMMON, true);
        w.pool(mine, APFSDS_5);
        w.pool(mine, FUEL_1);
        Attack both = new Attack(mine.id, List.of(
                new AttackChoice(mine.leader().id, ATTACK_2, Ammunition.APFSDS_120MM, enemyLeader.id),
                new AttackChoice(second.id, ATTACK_2, Ammunition.APFSDS_120MM, enemyLeader.id)));

        // needs 4 ammo and 2 fuel in total: only 1 fuel in the pool
        assertThatThrownBy(() -> w.act(0, both))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Fuel");
        w.pool(mine, FUEL_5);
        w.act(0, both);
        assertThat(w.ammoIn(mine, Ammunition.APFSDS_120MM)).isEqualTo(1);
        assertThat(w.fuelIn(mine)).isEqualTo(4);                              // 6 - 2
    }

    @Test
    void notEnoughOfTheRightAmmoIsRejectedWithoutSpendingAnything() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, HEAT_5);
        w.pool(mine, FUEL_5);
        assertThatThrownBy(() -> w.act(0, skirmish(mine, mine.leader(), ATTACK_2, Ammunition.APFSDS_120MM, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("APFSDS_120MM");
        assertThat(w.ammoIn(mine, Ammunition.HEAT_120MM)).isEqualTo(5);
        assertThat(w.fuelIn(mine)).isEqualTo(5);
        assertThat(w.s.activePlayer).isZero();
    }

    @Test
    void ammunitionMustFitTheWeaponAndCanBeLeftOutOnlyWhenThereIsOneChoice() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, APFSDS_5);
        w.pool(mine, NATO_10);
        w.pool(mine, FUEL_5);
        assertThatThrownBy(() -> w.act(0, skirmish(mine, mine.leader(), ATTACK_2, Ammunition.NATO_127x99MM, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("doesn't fit");
        assertThatThrownBy(() -> w.act(0, skirmish(mine, mine.leader(), ATTACK_2, null, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Choose which ammunition");
        w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, enemyLeader));   // the .50 only takes one type
        assertThat(w.ammoIn(mine, Ammunition.NATO_127x99MM)).isEqualTo(9);
    }

    // ── who can attack and who can be attacked ───────────────────────────────

    @Test
    void onlyFaceUpVehiclesCanBeTargetedAndResupplyNever() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        Vehicle hidden = w.add(enemy, ANTI_AIR, false);
        Vehicle supply = w.add(enemy, RESUPPLY, true);
        assertThatThrownBy(() -> w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, hidden)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("face-up");
        assertThatThrownBy(() -> w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, supply)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Resupply");
    }

    @Test
    void faceDownResupplyAndUnarmedVehiclesCannotAttack() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        Vehicle supply = w.add(mine, RESUPPLY, true);
        Vehicle specialist = w.add(mine, SPECIALIST, true);
        w.pool(mine, NATO_10);
        assertThatThrownBy(() -> w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("face down");
        assertThatThrownBy(() -> w.act(0, skirmish(mine, supply, ATTACK_1, null, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Resupply");
        assertThatThrownBy(() -> w.act(0, skirmish(mine, specialist, ATTACK_1, null, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("no ATTACK_1");
    }

    @Test
    void stunnedDisabledAndSuppressedVehiclesAreRestricted() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        w.pool(mine, APFSDS_5);
        w.pool(mine, FUEL_5);
        Vehicle v = mine.leader();

        v.stunned = true;
        assertThatThrownBy(() -> w.act(0, skirmish(mine, v, ATTACK_1, null, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("stunned");
        v.stunned = false;
        v.disabled = true;
        assertThatThrownBy(() -> w.act(0, skirmish(mine, v, ATTACK_1, null, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("disabled");
        v.disabled = false;
        v.suppressed = true;
        assertThatThrownBy(() -> w.act(0, skirmish(mine, v, ATTACK_1, null, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("suppressed");
        w.act(0, skirmish(mine, v, ATTACK_2, Ammunition.APFSDS_120MM, enemyLeader));  // ATTACK_2 is still allowed
    }

    @Test
    void effectsLastThroughTheVictimsNextTurnThenWearOff() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        enemyLeader.stunned = true;
        enemyLeader.suppressed = true;
        enemyLeader.disabled = true;
        w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, enemyLeader));   // player 1's turn begins, still affected
        assertThat(enemyLeader.stunned).isTrue();
        w.act(1, new EndTurn());
        assertThat(enemyLeader.stunned).isFalse();
        assertThat(enemyLeader.suppressed).isFalse();
        assertThat(enemyLeader.disabled).isFalse();
    }

    @Test
    void theFirstPlayerCannotAttackOnTheirFirstTurnButTheSecondPlayerCan() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        w.p(0).turnsTaken = 1;
        assertThatThrownBy(() -> w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("first turn");

        w.s.activePlayer = 1;
        w.p(1).turnsTaken = 1;
        StrikeGroup theirs = enemy;
        w.pool(theirs, NATO_10);
        Vehicle target = mine.leader();
        w.act(1, skirmish(theirs, enemyLeader, ATTACK_1, null, target));
        assertThat(target.hp).isLessThan(100);
    }

    @Test
    void youCannotActOutOfTurn() {
        w.hand(1, FUEL_5);
        assertThatThrownBy(() -> w.act(1, new Designate(FUEL_5, null)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("not your turn");
    }

    // ── destruction ──────────────────────────────────────────────────────────

    private Attack finishingBlow(StrikeGroup mine, Vehicle target) {
        return skirmish(mine, mine.leader(), ATTACK_1, null, target);
    }

    @Test
    void destroyingANonLeaderGivesNoChipAndTheGroupKeepsGoing() {
        Vehicle aa = w.add(enemy, ANTI_AIR, true);
        aa.hp = 1;
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        w.act(0, finishingBlow(mine, aa));
        assertThat(w.p(0).chips).isZero();
        assertThat(enemy.vehicles).hasSize(1);
        assertThat(w.p(1).discard).contains(ANTI_AIR);
        assertThat(w.p(1).groups).containsExactly(enemy);
    }

    @Test
    void destroyingALeaderTakesAChipAndBreaksUpTheGroup() {
        Vehicle tank = w.add(enemy, TANK_UNCOMMON, false);
        Vehicle aa = w.add(enemy, ANTI_AIR, true);
        Vehicle supply = w.add(enemy, RESUPPLY, false);
        enemyLeader.hp = 1;
        w.pool(enemy, FUEL_5);
        w.pool(enemy, FUEL_1);
        w.pool(enemy, APFSDS_5);              // 3 cards: lose 2 (rounded up), keep the fullest
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);

        w.act(0, finishingBlow(mine, enemyLeader));

        assertThat(w.p(0).chips).isEqualTo(2);                       // 4 vehicles: 1 chip + 1 bonus
        assertThat(w.p(1).groups).doesNotContain(enemy);
        assertThat(w.p(1).groups).hasSize(1);                        // the surviving tank is a group of one...
        assertThat(w.p(1).groups.get(0).leader()).isSameAs(tank);
        assertThat(tank.faceUp).isFalse();                           // ...face down
        assertThat(w.p(1).hand).contains(ANTI_AIR, RESUPPLY);        // non-tanks go back to hand
        assertThat(w.p(1).hand).doesNotContain(TANK_UNCOMMON);
        assertThat(w.p(1).discard).contains(TANK_RARE_MBT);
        // pool: 3 cards, ceil(3/2) = 2 discarded (smallest first), 1 card (a 5x) goes to the Depot
        assertThat(w.p(1).depot).hasSize(1);
        assertThat(w.p(1).discard).contains(FUEL_1);
        assertThat(w.p(1).discard).hasSize(1 + 2);                   // the leader + 2 pool cards
        assertThat(aa).isNotNull();
        assertThat(supply).isNotNull();
    }

    @Test
    void aGroupOfFourOrMoreIsWorthTwoChips() {
        w.add(enemy, TANK_UNCOMMON, false);
        w.add(enemy, ANTI_AIR, false);
        w.add(enemy, SPECIALIST, false);
        enemyLeader.hp = 1;
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        w.act(0, finishingBlow(mine, enemyLeader));
        assertThat(w.p(0).chips).isEqualTo(2);
    }

    @Test
    void aLoneTankIsItsOwnLeaderSoItCostsAChipAndItsWholePool() {
        enemyLeader.hp = 1;
        w.pool(enemy, FUEL_5);
        w.pool(enemy, FUEL_1);
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        w.act(0, finishingBlow(mine, enemyLeader));
        assertThat(w.p(0).chips).isEqualTo(1);
        assertThat(w.p(1).depot).isEmpty();
        assertThat(w.p(1).discard).containsExactlyInAnyOrder(TANK_RARE_MBT, FUEL_5, FUEL_1);
        assertThat(w.p(1).groups).isEmpty();
    }

    @Test
    void takingEnoughChipsWinsTheGame() {
        w.p(0).chips = 4;
        enemyLeader.hp = 1;
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        w.act(0, finishingBlow(mine, enemyLeader));
        assertThat(w.s.phase).isEqualTo(GameState.Phase.FINISHED);
        assertThat(w.s.winner).isZero();
        assertThatThrownBy(() -> w.act(1, new EndTurn()))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("over");
    }

    @Test
    void aTargetAlreadyDestroyedByAnEarlierAttackWastesTheLaterOne() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        Vehicle second = w.add(mine, TANK_UNCOMMON, true);
        Vehicle aa = w.add(enemy, ANTI_AIR, true);
        aa.hp = 1;
        w.pool(mine, NATO_10);
        ActionResult result = w.act(0, new Attack(mine.id, List.of(
                new AttackChoice(mine.leader().id, ATTACK_1, null, aa.id),
                new AttackChoice(second.id, ATTACK_1, null, aa.id))));
        assertThat(result.log).anyMatch(line -> line.contains("already destroyed"));
        assertThat(w.ammoIn(mine, Ammunition.NATO_127x99MM)).isEqualTo(8);    // both attacks were paid for
    }

    @Test
    void anAttackFromAnotherPlayersGroupIsRejected() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        assertThatThrownBy(() -> w.act(0, skirmish(enemy, enemyLeader, ATTACK_1, null, mine.leader())))
                .isInstanceOf(RuleViolationException.class);
    }
}
