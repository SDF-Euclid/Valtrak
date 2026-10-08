package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_1;
import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** UAV teams and Recon vehicles: a face-up vehicle spends Fuel to turn face-down enemy vehicles face up. */
class AbilityTest {

    private final TestWorld w = new TestWorld();
    private StrikeGroup mine;
    private Vehicle uav;
    private StrikeGroup enemy;
    private Vehicle enemyLeader, enemyLine, enemyResupply;

    @BeforeEach
    void setUp() {
        w.p(0).deck.add(TANK_COMMON);
        w.p(1).deck.add(TANK_COMMON);
        mine = w.group(0, TANK_COMMON, false);
        uav = w.add(mine, UAV, true);                  // reveals up to 2, costs 1 Fuel
        w.pool(mine, FUEL_5);
        enemy = w.group(1, TANK_RARE_MBT, false);
        enemyLine = w.add(enemy, ANTI_AIR, false);
        enemyResupply = w.add(enemy, RESUPPLY, false);
        enemyLeader = enemy.leader();
    }

    private UseAbility reveal(Vehicle by, Vehicle... targets) {
        return new UseAbility(by.id, java.util.Arrays.stream(targets).map(t -> t.id).toList());
    }

    @Test
    void revealsTheChosenVehiclesAndCostsFuel() {
        ActionResult result = w.act(0, reveal(uav, enemyLeader, enemyLine));
        assertThat(enemyLeader.faceUp).isTrue();
        assertThat(enemyLine.faceUp).isTrue();
        assertThat(enemyResupply.faceUp).isFalse();
        assertThat(w.fuelIn(mine)).isEqualTo(4);
        assertThat(uav.abilityUsed).isTrue();
        assertThat(result.log).anyMatch(l -> l.contains("reveals 2"));
        assertThat(w.s.activePlayer).as("using an ability does not end your turn").isZero();
    }

    @Test
    void youCanScoutAndThenAttackInTheSameTurn() {
        Vehicle shooter = mine.leader();
        shooter.faceUp = true;
        w.pool(mine, NATO_10);
        w.act(0, reveal(uav, enemyLeader));
        w.act(0, skirmish(mine, shooter, ATTACK_1, null, enemyLeader));
        assertThat(enemyLeader.hp).isLessThan(enemyLeader.maxHp);
    }

    @Test
    void itCannotRevealMoreThanItsPower() {
        assertThatThrownBy(() -> w.act(0, reveal(uav, enemyLeader, enemyLine, enemyResupply)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("at most 2");
        assertThat(enemyLeader.faceUp).isFalse();
        assertThat(w.fuelIn(mine)).isEqualTo(5);
    }

    @Test
    void aReaperLevelVehicleWithMorePowerCanRevealMore() {
        Vehicle scout = w.add(mine, SCOUT, true);        // reveals 1
        assertThatThrownBy(() -> w.act(0, reveal(scout, enemyLeader, enemyLine)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("at most 1");
        w.act(0, reveal(scout, enemyLeader));
        assertThat(enemyLeader.faceUp).isTrue();
    }

    @Test
    void theVehicleMustBeFaceUpAndAbleAndHaveAnAbility() {
        uav.faceUp = false;
        assertThatThrownBy(() -> w.act(0, reveal(uav, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("face up");
        uav.faceUp = true;
        uav.stunned = true;
        assertThatThrownBy(() -> w.act(0, reveal(uav, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("can't use");
        uav.stunned = false;
        assertThatThrownBy(() -> w.act(0, reveal(mine.leader(), enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("no ability");
    }

    @Test
    void onlyFaceDownEnemyVehiclesCanBeChosen() {
        enemyLeader.faceUp = true;
        assertThatThrownBy(() -> w.act(0, reveal(uav, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("already face up");
        assertThatThrownBy(() -> w.act(0, reveal(uav, mine.leader())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("not found");
        assertThatThrownBy(() -> w.act(0, new UseAbility(uav.id, List.of())))
                .isInstanceOf(RuleViolationException.class);
        assertThatThrownBy(() -> w.act(0, new UseAbility(uav.id, List.of(enemyLine.id, enemyLine.id))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("only be chosen once");
    }

    @Test
    void oncePerTurnThenAvailableAgainNextTurn() {
        w.act(0, reveal(uav, enemyLeader));
        assertThatThrownBy(() -> w.act(0, reveal(uav, enemyLine)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("already used");
        w.act(0, new EndTurn());
        w.act(1, new EndTurn());
        assertThat(uav.abilityUsed).isFalse();
        w.act(0, reveal(uav, enemyLine));
        assertThat(enemyLine.faceUp).isTrue();
    }

    @Test
    void itNeedsTheFuelAndLeavesEverythingAloneWithoutIt() {
        mine.pool.clear();
        assertThatThrownBy(() -> w.act(0, reveal(uav, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Fuel");
        assertThat(enemyLeader.faceUp).isFalse();
        assertThat(uav.abilityUsed).isFalse();
    }

    @Test
    void aRevealedResupplyVehicleCanBeAttackedBecauseAnEffectExposedIt() {
        w.act(0, reveal(uav, enemyResupply));
        assertThat(enemyResupply.faceUp).isTrue();
        Vehicle shooter = mine.leader();
        shooter.faceUp = true;
        w.pool(mine, NATO_10);
        w.act(0, skirmish(mine, shooter, ATTACK_1, null, enemyResupply));
        assertThat(enemyResupply.hp).isLessThan(90);
    }

    @Test
    void revealedVehiclesStayFaceUpUntilTheirOwnerRetreatsThem() {
        w.act(0, reveal(uav, enemyLine));
        w.act(0, new EndTurn());
        assertThat(enemyLine.faceUp).isTrue();
        w.pool(enemy, FUEL_5);
        w.act(1, new Retreat(enemyLine.id));
        assertThat(enemyLine.faceUp).isFalse();
    }
}
