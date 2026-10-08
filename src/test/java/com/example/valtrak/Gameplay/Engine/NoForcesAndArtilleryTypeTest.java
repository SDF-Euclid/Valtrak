package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_1;
import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;

/** Losing with no strike group and no tank to start one, and Artillery doing explosive damage. */
class NoForcesAndArtilleryTypeTest {

    private final TestWorld w = new TestWorld();

    // ── no forces ────────────────────────────────────────────────────────────

    @Test
    void youLoseAtTheStartOfYourTurnWithNoGroupAndNoTankInHandAfterDrawing() {
        w.group(0, TANK_COMMON, false);
        w.hand(1, UAV, SPECIALIST);                // no groups, and nothing that can stand alone
        w.p(1).deck.add(FUEL_5);                   // the draw doesn't help
        w.p(0).deck.add(TANK_COMMON);
        ActionResult r = w.act(0, new EndTurn());
        assertThat(w.s.phase).isEqualTo(GameState.Phase.FINISHED);
        assertThat(w.s.winner).isZero();
        assertThat(w.s.endReason).contains("no strike group").contains("Player 2");
        assertThat(r.log).anyMatch(l -> l.contains("Game over"));
    }

    @Test
    void aTankYouDrawSavesYou() {
        w.group(0, TANK_COMMON, false);
        w.p(1).deck.add(TANK_RARE);
        ActionResult r = w.act(0, new EndTurn());
        assertThat(w.s.phase).isEqualTo(GameState.Phase.PLAYING);
        assertThat(w.p(1).hand).containsExactly(TANK_RARE);
    }

    @Test
    void aTankAlreadyInHandIsEnough() {
        w.group(0, TANK_COMMON, false);
        w.hand(1, TANK_UNCOMMON);
        w.p(1).deck.add(FUEL_5);
        w.act(0, new EndTurn());
        assertThat(w.s.phase).isEqualTo(GameState.Phase.PLAYING);
    }

    @Test
    void aGroupOnTheFieldIsEnoughEvenWithAnEmptyHand() {
        w.group(0, TANK_COMMON, false);
        w.group(1, TANK_COMMON, false);
        w.p(1).deck.add(FUEL_5);
        w.act(0, new EndTurn());
        assertThat(w.s.phase).isEqualTo(GameState.Phase.PLAYING);
    }

    @Test
    void losingYourLastGroupWithNothingToReplaceItEndsTheGameOnYourTurn() {
        StrikeGroup mine = w.group(0, TANK_RARE, true);
        w.pool(mine, NATO_10);
        StrikeGroup theirs = w.group(1, TANK_COMMON, true);
        theirs.leader().hp = 1;
        w.hand(1, UAV);
        w.p(1).deck.add(FUEL_5);
        w.p(0).deck.add(TANK_COMMON);
        w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, theirs.leader()));
        assertThat(w.p(0).chips).isEqualTo(1);
        assertThat(w.s.phase).isEqualTo(GameState.Phase.FINISHED);
        assertThat(w.s.winner).isZero();
        assertThat(w.s.endReason).contains("no strike group");
    }

    @Test
    void theRuleCanBeSwitchedOff() {
        w.rules.loseWithNoForces = false;
        w.group(0, TANK_COMMON, false);
        w.p(1).deck.add(FUEL_5);
        w.act(0, new EndTurn());
        assertThat(w.s.phase).isEqualTo(GameState.Phase.PLAYING);
    }

    // ── artillery damage type ────────────────────────────────────────────────

    private TestWorld explosive() {
        TestWorld t = new TestWorld();
        t.rules.artilleryDamageType = DamageType.EXPLOSIVE;
        t.rules.artilleryCaliber = 100;
        t.p(0).deck.add(TANK_COMMON);
        t.p(1).deck.add(TANK_COMMON);
        return t;
    }

    private int hitWith(TestWorld t, long artillery, Vehicle target) {
        t.hand(0, artillery);
        int before = target.hp;
        t.act(0, new PlayItem(artillery, List.of(target.id), List.of()));
        return before - target.hp;
    }

    @Test
    void explosiveArtilleryDoesFullDamageToSoftVehiclesAndLittleToMainBattleTanks() {
        TestWorld t = explosive();
        StrikeGroup g = t.group(1, TANK_RARE_MBT, true);                  // armor 85
        Vehicle soft = t.add(g, RECON, true);                             // armor 25
        Vehicle mbt = g.leader();
        assertThat(hitWith(t, ARTILLERY_BLIND, soft)).as("soft targets take the full 40").isEqualTo(40);
        t.act(0, new EndTurn());
        t.act(1, new EndTurn());               // a new turn, so a second Artillery card is allowed
        assertThat(hitWith(t, ARTILLERY_BLIND, mbt)).as("a main battle tank: 40 x 0.7 = 28, minus 13 for its armor").isEqualTo(15);
    }

    @Test
    void explosiveArtilleryStunsALightlyArmoredVehicleItHitsWithoutOverpressure() {
        TestWorld t = explosive();
        Vehicle medium = t.group(1, TANK_RARE, true).leader();            // armor 70: LIGHT bracket, too tough for overpressure at caliber 100
        int dealt = hitWith(t, ARTILLERY_1, medium);                      // 20 base x 1.4 = 28, minus 11 for armor 70
        assertThat(dealt).isEqualTo(17);
        assertThat(medium.stunned).isTrue();
    }

    @Test
    void artilleryCannotHitAFaceUpAircraft() {
        TestWorld t = explosive();
        StrikeGroup g = t.group(1, TANK_COMMON, false);
        Vehicle uav = t.add(g, UAV, true);
        t.hand(0, ARTILLERY_1);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> t.act(0, new PlayItem(ARTILLERY_1, List.of(uav.id), List.of())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("aircraft");
        assertThat(uav.hp).isEqualTo(uav.maxHp);
        assertThat(t.p(0).hand).containsExactly(ARTILLERY_1);
    }

    @Test
    void theTunedDefaultIsExplosiveWithCaliber100() {
        GameRules rules = GameRules.defaults();
        assertThat(rules.artilleryDamageType).isEqualTo(DamageType.EXPLOSIVE);
        assertThat(rules.artilleryCaliber).isEqualTo(100);
        assertThat(rules.loseWithNoForces).isTrue();
    }

    @Test
    void settingTheTypeToNullGoesBackToTrueDamage() {
        TestWorld t = new TestWorld();                                    // plain numbers: true damage
        t.p(0).deck.add(TANK_COMMON);
        Vehicle mbt = t.group(1, TANK_RARE_MBT, true).leader();
        assertThat(hitWith(t, ARTILLERY_1, mbt)).isEqualTo(20);
    }
}
