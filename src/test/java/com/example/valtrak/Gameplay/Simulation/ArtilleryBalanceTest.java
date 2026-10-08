package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Gameplay.Engine.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The real Artillery cards against the real vehicles, with the game's default rules (explosive damage, caliber 100, damage x4).
 * The design goal: a hit never kills a full-health tank, except Legendary Artillery against the weakest light tank; ground Recon
 * and Resupply vehicles can be one-shot; aircraft can't be hit at all.
 */
class ArtilleryBalanceTest {

    private final EnumCardCatalog catalog = new EnumCardCatalog();
    private final GameRules rules = GameRules.defaults();

    private List<ItemSpec> artillery() {
        return catalog.all().stream().filter(c -> c instanceof ItemSpec i && i.effect() == ItemEffect.ARTILLERY)
                .map(c -> (ItemSpec) c).toList();
    }

    private List<VehicleSpec> vehicles() {
        return catalog.all().stream().filter(c -> c instanceof VehicleSpec).map(c -> (VehicleSpec) c).toList();
    }

    /** What one hit does to a full-health vehicle, after the damage scale. */
    private int hit(ItemSpec card, VehicleSpec target) {
        int dmg = DamageCalculator.calculate(card.power(), com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect.NONE,
                rules.artilleryDamageType, rules.artilleryCaliber, target.armor(), 0).damage();
        return dmg * rules.damagePercent / 100;
    }

    @Test
    void onlyLegendaryArtilleryCanOneShotATankAndOnlyTheWeakestLightTank() {
        VehicleSpec weakestLight = vehicles().stream().filter(VehicleSpec::isTank).min(java.util.Comparator.comparingInt(VehicleSpec::hp)).orElseThrow();
        for (ItemSpec card : artillery()) {
            for (VehicleSpec tank : vehicles().stream().filter(VehicleSpec::isTank).toList()) {
                boolean kills = hit(card, tank) >= tank.hp();
                boolean allowed = card.level() == CardLevel.LEGENDARY && tank == weakestLight;
                assertThat(kills).as(card.name() + " vs " + tank.name() + " (" + hit(card, tank) + " of " + tank.hp() + " HP)").isEqualTo(allowed);
            }
        }
    }

    @Test
    void groundReconAndResupplyVehiclesCanBeOneShot() {
        ItemSpec legendary = artillery().stream().filter(c -> c.level() == CardLevel.LEGENDARY).findFirst().orElseThrow();
        ItemSpec epic = artillery().stream().filter(c -> c.level() == CardLevel.EPIC).findFirst().orElseThrow();
        for (VehicleSpec v : vehicles()) {
            if (v.vehicleClass().name().equals("RECON")) assertThat(hit(epic, v)).as(v.name()).isGreaterThanOrEqualTo(v.hp());
            if (v.isResupply() && v.level() == CardLevel.RARE) assertThat(hit(legendary, v)).as(v.name()).isGreaterThanOrEqualTo(v.hp());
        }
    }

    @Test
    void theCardsGetStrongerWithRarityAndAircraftAreFlagged() {
        List<ItemSpec> cards = artillery().stream().sorted(java.util.Comparator.comparingInt(c -> c.level().ordinal())).toList();
        for (int i = 1; i < cards.size(); i++) assertThat(cards.get(i).power()).isGreaterThan(cards.get(i - 1).power());
        assertThat(cards.get(cards.size() - 1).power()).isEqualTo(36);
        assertThat(vehicles().stream().filter(VehicleSpec::air).map(VehicleSpec::name))
                .contains("RQ-11 Raven Team", "MQ-9 Reaper Flight").hasSize(7);
    }
}
