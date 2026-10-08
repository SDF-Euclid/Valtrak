package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.AbilityType;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleAttackEntity;
import com.example.valtrak.Data.GameData.Repository.Cards.AmmunitionCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.SpecialItemCardRepository;
import com.example.valtrak.Data.GameData.Service.DbCardCatalog;
import com.example.valtrak.Gameplay.Engine.ItemEffect;
import com.example.valtrak.Gameplay.Engine.ItemSpec;
import com.example.valtrak.Gameplay.Engine.SearchKind;
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
    @Autowired SpecialItemCardRepository items;
    @Autowired DbCardCatalog catalog;

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
    void itemCardsAreLoadedAndGiveTheEngineTheRightNumbers() {
        assertThat(items.count()).isEqualTo(39);
        var era = (ItemSpec) catalog.find(items.findByName("Advanced ERA Suite").orElseThrow().getId());
        assertThat(era.effect()).isEqualTo(ItemEffect.ERA);
        assertThat(era.power()).isEqualTo(50);
        assertThat(era.level()).isEqualTo(CardLevel.LEGENDARY);
        var artillery = (ItemSpec) catalog.find(items.findByName("Strategic Bombardment").orElseThrow().getId());
        assertThat(artillery.effect()).isEqualTo(ItemEffect.ARTILLERY);
        assertThat(artillery.power()).isEqualTo(40);
        assertThat(artillery.count()).isEqualTo(3);
        var search = (ItemSpec) catalog.find(items.findByName("Armored Reinforcements").orElseThrow().getId());
        assertThat(search.effect()).isEqualTo(ItemEffect.SEARCH);
        assertThat(search.searchKind()).isEqualTo(SearchKind.TANK);
        assertThat(search.count()).isEqualTo(2);
        var draw = (ItemSpec) catalog.find(items.findByName("Total Mobilization").orElseThrow().getId());
        assertThat(draw.effect()).isEqualTo(ItemEffect.DRAW);
        assertThat(draw.count()).isEqualTo(3);
    }

    @Test
    void anItemCardEditedInTheDatabaseGetsItsNumbersBack() {
        var card = items.findByName("Mortar Strike").orElseThrow();
        card.setPrimaryValue(999);
        card.setSecondaryValue(9);
        card.setLevel(CardLevel.COMMANDER);
        items.save(card);
        loader.run();
        var fixed = items.findByName("Mortar Strike").orElseThrow();
        assertThat(fixed.getPrimaryValue()).isEqualTo(20);
        assertThat(fixed.getSecondaryValue()).isEqualTo(1);
        assertThat(fixed.getLevel()).isEqualTo(CardLevel.COMMON);
    }

    @Test
    void everyCardDescriptionFitsInTheDatabaseColumn() {
        for (var item : com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.SpecialItem.values()) {
            assertThat(item.getItemDescription().length()).as(item.getItemName()).isLessThanOrEqualTo(255);
        }
    }

    @Test
    void sabotageIsASingleCardThatDiscardsUpToThree() {
        var sabotage = (ItemSpec) catalog.find(items.findByName("Sabotage").orElseThrow().getId());
        assertThat(sabotage.effect()).isEqualTo(ItemEffect.SABOTAGE);
        assertThat(sabotage.count()).isEqualTo(3);
        assertThat(items.findByName("Cyber Intrusion")).isEmpty();
        assertThat(items.findByName("Strategic Disruption")).isEmpty();
    }

    @Test
    void aRetiredCardIsRemovedFromTheDatabaseUnlessADeckStillUsesIt() {
        var old = new com.example.valtrak.Gameplay.Cards.Special.SpecialItemCard(
                com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.SpecialItem.SABOTAGE_RAID);
        old.setName("Cyber Intrusion");
        items.save(old);
        loader.run();
        assertThat(items.findByName("Cyber Intrusion")).isEmpty();
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
