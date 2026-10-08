package com.example.valtrak.Gameplay.Engine;

import org.junit.jupiter.api.Test;

import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_1;
import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;

/** The optional experiment rules are off by default; these tests check that they work when switched on. */
class ExperimentRulesTest {

    @Test
    void theExperimentRulesAreOffByDefault() {
        GameRules rules = GameRules.defaults();
        assertThat(rules.stalemateRounds).isZero();
        assertThat(rules.damagePercent).isEqualTo(100);
    }

    @Test
    void afterEnoughPassiveRoundsEveryVehicleExceptResupplyIsRevealed() {
        TestWorld w = new TestWorld();
        w.rules.stalemateRounds = 1;                                 // 1 round = both players pass once
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        Vehicle supply = w.add(mine, RESUPPLY, false);
        StrikeGroup theirs = w.group(1, TANK_RARE, false);
        w.p(0).deck.add(TANK_COMMON);
        w.p(1).deck.add(TANK_COMMON);

        w.act(0, new Action.EndTurn());
        assertThat(mine.leader().faceUp).isFalse();                  // only one pass so far
        w.act(1, new Action.EndTurn());

        assertThat(mine.leader().faceUp).isTrue();
        assertThat(theirs.leader().faceUp).isTrue();
        assertThat(supply.faceUp).isFalse();
        assertThat(w.s.passesInARow).isZero();
    }

    @Test
    void anAttackResetsTheStalemateCounter() {
        TestWorld w = new TestWorld();
        w.rules.stalemateRounds = 1;
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        StrikeGroup theirs = w.group(1, TANK_RARE, true);
        w.pool(mine, NATO_10);
        w.p(0).deck.add(TANK_COMMON);
        w.p(1).deck.add(TANK_COMMON);
        w.s.passesInARow = 1;
        w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, theirs.leader()));
        assertThat(w.s.passesInARow).isZero();
    }

    @Test
    void damagePercentScalesDamage() {
        int normal = hpLostWith(100);
        int doubled = hpLostWith(200);
        assertThat(doubled).isEqualTo(normal * 2);
    }

    private int hpLostWith(int percent) {
        TestWorld w = new TestWorld();
        w.rules.damagePercent = percent;
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        StrikeGroup theirs = w.group(1, TANK_RARE_MBT, true);
        w.pool(mine, NATO_10);
        w.p(0).deck.add(TANK_COMMON);
        w.p(1).deck.add(TANK_COMMON);
        w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, theirs.leader()));
        return theirs.leader().maxHp - theirs.leader().hp;
    }
}
