package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DamageCalculatorTest {

    @Test
    void kineticVsMediumArmorIsStraightModifierMinusArmorReduction() {
        // armor 100 = MEDIUM (x1.0); reduction = round(100 * 0.15) = 15
        var r = DamageCalculator.calculate(40, SpecialEffect.NONE, Ammunition.APFSDS_120MM, 100, 0);
        assertThat(r.damage()).isEqualTo(25);
        assertThat(r.piercing()).isFalse();
    }

    @Test
    void kineticPiercesHeavyArmorWhenCaliberIsHighEnough() {
        // armor 150 = HEAVY (x1.3); caliber 120 >= 150 * 0.8 so it pierces and ignores armor reduction
        var r = DamageCalculator.calculate(40, SpecialEffect.NONE, Ammunition.APFSDS_120MM, 150, 0);
        assertThat(r.piercing()).isTrue();
        assertThat(r.damage()).isEqualTo(52);
        assertThat(r.effect()).isEqualTo(SpecialEffect.PIERCE);
    }

    @Test
    void damageIsAtLeastOne() {
        var r = DamageCalculator.calculate(5, SpecialEffect.NONE, Ammunition.NATO_127x99MM, 200, 0);
        assertThat(r.damage()).isEqualTo(1);
    }

    @Test
    void explosiveRoundsStunLightTargets() {
        // armor 30 = UNARMORED: explosive x1.8 and an automatic STUN
        var r = DamageCalculator.calculate(30, SpecialEffect.NONE, Ammunition.HE_40MM, 30, 0);
        assertThat(r.effect()).isEqualTo(SpecialEffect.STUN);
        assertThat(r.damage()).isEqualTo(49); // round(30 * 1.8) = 54, minus round(30 * 0.15) = 5
    }

    @Test
    void breachStacksLowerEffectiveArmor() {
        // explosive rounds do more against lighter armor: armor 75 (MEDIUM) drops to 66 (LIGHT) with one breach stack
        var fresh = DamageCalculator.calculate(40, SpecialEffect.NONE, Ammunition.HE_40MM, 75, 0);
        var breached = DamageCalculator.calculate(40, SpecialEffect.NONE, Ammunition.HE_40MM, 75, 1);
        assertThat(fresh.damage()).isEqualTo(17);
        assertThat(breached.damage()).isEqualTo(46);
    }

    @Test
    void overpressureIsTrueDamage() {
        // 40mm HE against armor 20: caliber >= armor * 1.5, so the base damage goes through untouched
        var r = DamageCalculator.calculate(30, SpecialEffect.NONE, Ammunition.HE_40MM, 20, 0);
        assertThat(r.trueDamage()).isTrue();
        assertThat(r.damage()).isEqualTo(30);
        assertThat(r.effect()).isEqualTo(SpecialEffect.OVERPRESSURE);
    }

    @Test
    void slotEffectIsUsedWhenNothingElseTriggers() {
        var r = DamageCalculator.calculate(15, SpecialEffect.SUPPRESSION, Ammunition.HEAT_120MM, 100, 0);
        assertThat(r.effect()).isEqualTo(SpecialEffect.SUPPRESSION);
    }
}
