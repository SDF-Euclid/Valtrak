package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.AbilityType;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleAttackEntity;
import com.example.valtrak.Data.GameData.Repository.Cards.AmmunitionCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.VehicleCardRepository;
import com.example.valtrak.Data.GameData.Repository.EnumData.VehicleAttackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Changing a card in its enum must change it in the database on the next start. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:loadersync;DB_CLOSE_DELAY=-1")
class DataLoaderSyncTest {

    @Autowired DataLoader loader;
    @Autowired AmmunitionCardRepository ammo;
    @Autowired VehicleCardRepository vehicles;
    @Autowired VehicleAttackRepository attacks;
    @Autowired CardRepository cards;

    @Test
    void anItemCardEditedInTheDatabaseIsPutBackToWhatTheEnumSays() {
        var crate = ammo.findByName("1x 120mm HE Crate").orElseThrow();
        crate.setLevel(CardLevel.COMMANDER);
        crate.setDescription("something else");
        crate.setCount(7);
        ammo.save(crate);

        loader.run();

        var fixed = ammo.findByName("1x 120mm HE Crate").orElseThrow();
        assertThat(fixed.getLevel()).isEqualTo(CardLevel.COMMON);
        assertThat(fixed.getDescription()).isEqualTo("Re-supplies 1 120mm HE shell");
        assertThat(fixed.getCount()).isEqualTo(1);
    }

    @Test
    void aVehicleEditedInTheDatabaseGetsItsNumbersAndAttacksBack() {
        var abrams = vehicles.findByName("M1A1 Abrams").orElseThrow();
        abrams.setVehicleHP(1);
        abrams.setVehicleArmor(1);
        abrams.setLevel(CardLevel.COMMON);
        vehicles.save(abrams);
        var attackRows = attacks.findByVehicle(abrams);
        VehicleAttackEntity changed = attackRows.get(0);
        changed.setBaseDamage(9999);
        attacks.save(changed);
        attacks.delete(attackRows.get(1));

        loader.run();

        var fixed = vehicles.findByName("M1A1 Abrams").orElseThrow();
        assertThat(fixed.getVehicleHP()).isEqualTo(260);
        assertThat(fixed.getVehicleArmor()).isEqualTo(85);
        assertThat(fixed.getLevel()).isEqualTo(CardLevel.EPIC);
        var fixedAttacks = attacks.findByVehicle(fixed);
        assertThat(fixedAttacks).hasSize(3);
        assertThat(fixedAttacks).noneMatch(a -> a.getBaseDamage() == 9999);
    }

    @Test
    void anAbilityEditedInTheDatabaseIsRestored() {
        var fennek = vehicles.findByName("Fennek").orElseThrow();
        fennek.setAbilityPower(9);
        fennek.setAbilityFuelCost(0);
        vehicles.save(fennek);
        loader.run();
        var fixed = vehicles.findByName("Fennek").orElseThrow();
        assertThat(fixed.getAbilityType()).isEqualTo(AbilityType.REVEAL_ENEMY);
        assertThat(fixed.getAbilityPower()).isEqualTo(2);
        assertThat(fixed.getAbilityFuelCost()).isEqualTo(1);
    }

    @Test
    void runningTheLoaderAgainDoesNotCreateDuplicates() {
        long before = cards.count();
        loader.run();
        loader.run();
        assertThat(cards.count()).isEqualTo(before);
        assertThat(attacks.count()).isEqualTo(attacks.findAll().stream().map(a -> a.getVehicle().getId() + a.getAttackSlot().name()).distinct().count());
    }
}
