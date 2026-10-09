package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.AmmoSupplyCrate;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.FuelSupplyDrum;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.RepairSupplyKit;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.SpecialItem;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.SupplyCrate;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.AmmunitionItemInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.FuelItemInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.RepairItemInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.SpecialItemInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.SupplyItemInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.VehicleAttackInterface;
import com.example.valtrak.Data.CardLibrary.Nations;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.*;
import com.example.valtrak.Data.GameData.Repository.Cards.AmmunitionCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.FuelCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.RepairCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.SpecialItemCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.SupplyCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.VehicleCardRepository;
import com.example.valtrak.Data.GameData.Repository.DeckRepository;
import com.example.valtrak.Data.GameData.Repository.PlayerRepository;
import com.example.valtrak.Data.GameData.Repository.EnumData.*;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.ItemCardInterface;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Resource.FuelCard;
import com.example.valtrak.Gameplay.Cards.Resource.RepairCard;
import com.example.valtrak.Gameplay.Cards.Resource.SupplyCard;
import com.example.valtrak.Gameplay.Cards.Special.SpecialItemCard;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import com.example.valtrak.Gameplay.Engine.DamageMatchups;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bootstraps the Valtrak database with all static game data on application startup.
 * This class runs once when the Spring application starts via {@link CommandLineRunner}.
 * It seeds all enumerated constants (damage types, vehicle classes, ammunition, weapons,
 * nations, etc.) as database entities, then seeds all card definitions from each nation's
 * vehicle enum. Each seed method is idempotent, and it checks for existing entries before
 * inserting to prevent duplicate data on subsequent startups.
 * Seeding order matters due to foreign key dependencies:
 * <pre>
 * DamageTypes -> DamageTypeMatchups -> VehicleTypes -> VehicleClasses
 *     → Ammunition -> Weapons -> Nations -> Ground Vehicles -> Vehicle Attacks
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    /*==================== REPOSITORIES ====================*/

    private final DamageTypeRepository damageTypeRepo;
    private final DamageTypeMatchupRepository damageTypeMatchupRepo;
    private final VehicleTypeRepository vehicleTypeRepo;
    private final VehicleClassRepository vehicleClassRepo;
    private final AmmunitionRepository ammoRepo;
    private final AmmunitionCardRepository ammunitionCardRepo;
    private final FuelCardRepository fuelCardRepo;
    private final RepairCardRepository repairCardRepo;
    private final SupplyCardRepository supplyCardRepo;
    private final SpecialItemCardRepository specialItemRepo;
    private final DeckRepository deckRepo;
    private final PlayerRepository playerRepo;
    private final com.example.valtrak.Data.GameData.Repository.MatchRepository matchRepo;
    private final com.example.valtrak.Data.GameData.Service.DbCardCatalog engineCatalog;
    private final com.example.valtrak.Data.GameData.Service.CardCatalogService cardList;
    private final WeaponRepository weaponRepo;
    private final NationRepository nationRepo;
    private final VehicleCardRepository vehicleRepo;
    private final VehicleAttackRepository vehicleAttackRepo;

    /*======================================================*/

    /**
     * Console logger used to track seeding progress and report completion
     */
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    /**
     * Entry point for database seeding. Called automatically by Spring Boot on startup.
     * Runs all seed methods in dependency order and logs completion.
     * @param args command line arguments passed to the application (unused)
     */
    @Override
    public void run(String @NonNull ... args) {
        loadDamageTypes();
        loadDamageTypeMatchups();
        loadVehicleTypes();
        loadVehicleClasses();
        loadAmmunition();
        loadWeapons();
        loadNations();
        for (var vehicles : com.example.valtrak.Data.CardLibrary.Vehicles.VehicleLibrary.ALL) loadGroundVehicles(vehicles);
        loadAmmunitionCards(AmmoSupplyCrate.values());
        loadFuelCards(FuelSupplyDrum.values());
        loadRepairCards(RepairSupplyKit.values());
        loadSupplyCards(SupplyCrate.values());
        loadSpecialItems(SpecialItem.values());
        retireCards();
        engineCatalog.refresh();      // anything read while the loader was running may be out of date
        cardList.refresh();
        logger.info("Data loaded successfully");
    }

    /**
     * Seeds all {@link DamageType} enum constants into the damage_types table.
     * Each entry represents a category of damage (KINETIC, CHEMICAL, EXPLOSIVE, ELECTRIC)
     * that determines how weapons interact with different armor brackets.
     */
    private void loadDamageTypes() {
        for (DamageType dt : DamageType.values()) {
            if (!damageTypeRepo.existsByName(dt.name())) {
                damageTypeRepo.save(new DamageTypeEntity(dt.name()));
            }
        }
    }

    /**
     * Seeds the damage type matchup table which defines how each damage type
     * performs against each armor bracket.
     * Each matchup entry contains:
     * - A damage modifier (e.g. KINETIC vs HEAVY = 1.3x), defined in {@link DamageMatchups}
     * - An auto effect that triggers on hit (e.g. EXPLOSIVE vs UNARMORED = STUN)
     * These values drive the core combat damage formula:
     * finalDamage = max(1, round(baseDamage * modifier) - round(armor * 0.15))
     */
    private void loadDamageTypeMatchups() {
        for (DamageType dt : DamageType.values()) {
            for (ArmorBracket bracket : ArmorBracket.values()) {
                if (damageTypeMatchupRepo.findByDamageTypeAndArmorBracket(dt, bracket).isEmpty()) {
                    damageTypeMatchupRepo.save(new DamageTypeMatchupEntity(
                            dt,
                            bracket,
                            DamageMatchups.modifier(dt, bracket),
                            DamageMatchups.autoEffect(dt, bracket)
                    ));
                }
            }
        }
    }

    /**
     * Seeds all {@link VehicleType} enum constants into the vehicle_types table.
     * Vehicle types categorize units by their operating environment
     * (e.g. GROUND, AIR, WATER).
     */
    private void loadVehicleTypes() {
        for (VehicleType vt : VehicleType.values()) {
            if (!vehicleTypeRepo.existsByName(vt.name())) {
                vehicleTypeRepo.save(new VehicleTypeEntity(vt.name()));
            }
        }
    }

    /**
     * Seeds all {@link VehicleClass} enum constants into the vehicle_classes table.
     * Vehicle classes define the combat role of a unit
     * (e.g. LIGHT_TANK, MAIN_BATTLE_TANK, ANTI_AIR).
     */
    private void loadVehicleClasses() {
        for (VehicleClass vc : VehicleClass.values()) {
            if (!vehicleClassRepo.existsByClassName(vc.name())) {
                vehicleClassRepo.save(new VehicleClassEntity(vc.name()));
            }
        }
    }

    /**
     * Seeds all {@link Ammunition} enum constants into the ammunition table.
     * Each ammo type references a {@link DamageType} foreign key, so damage types
     * must be seeded before this method runs.
     *
     * @throws RuntimeException if a required DamageType entity is not found
     */
    private void loadAmmunition() {
        for (Ammunition ammo : Ammunition.values()) {
            if (!ammoRepo.existsByName(ammo.name())) {
                DamageTypeEntity damageType = damageTypeRepo
                        .findByName(ammo.getDamageType().name())
                        .orElseThrow(() -> new RuntimeException(
                                "DamageType not found: " + ammo.getDamageType().name()
                        ));
                ammoRepo.save(new AmmunitionEntity(ammo.name(), damageType));
            }
        }
    }

    /**
     * Seeds all {@link Weapon} enum constants into the weapons table.
     * Each weapon references a list of compatible {@link Ammunition} entities
     * via a many-to-many join table, so ammunition must be seeded before this
     * method runs.
     *
     * @throws RuntimeException if a required Ammunition entity is not found
     */
    private void loadWeapons() {
        for (Weapon w : Weapon.values()) {
            if (!weaponRepo.existsByWeaponName(w.name())) {
                List<AmmunitionEntity> ammoEntities = w.getCompatibleAmmunition().stream()
                        .map(a -> ammoRepo.findByName(a.name())
                                .orElseThrow(() -> new RuntimeException(
                                        "Ammunition not found: " + a.name()
                                )))
                        .toList();
                weaponRepo.save(new WeaponEntity(w.name(), ammoEntities));
            }
        }
    }

    /**
     * Seeds all {@link Nations} enum constants into the nations table.
     * Nations are used as cosmetic display choices for player profiles
     * and as metadata on vehicle cards indicating their country of origin.
     */
    private void loadNations() {
        for (Nations n : Nations.values()) {
            if (!nationRepo.existsByNationName(n.getName())) {
                nationRepo.save(new NationEntity(n.getName(), n.getAbbreviation()));
            }
        }
    }

    /**
     * Seeds ground vehicle cards from a given nation's vehicle enum into the
     * database. For each vehicle, this method:
     * <ol>
     *   <li>Looks up the required {@link VehicleTypeEntity} and {@link VehicleClassEntity}</li>
     *   <li>Saves the base {@link GroundVehicleCard} entity</li>
     *   <li>Iterates over the vehicle's attack definitions and saves each as a
     *       {@link VehicleAttackEntity} linked to the card</li>
     * </ol>
     *
     * This method is generic across all nations — adding a new nation only
     * requires implementing {@link GroundVehicleCardInterface} on a new enum
     * and passing its values here.
     *
     * @param vehicles an array of {@link GroundVehicleCardInterface} values
     *                 from a nation enum (e.g. USGroundVehicles.values())
     * @throws RuntimeException if any required VehicleType, VehicleClass,
     *                          or Weapon entity is not found
     */
    private void loadGroundVehicles(GroundVehicleCardInterface @NonNull [] vehicles) {
        for (GroundVehicleCardInterface vehicle : vehicles) {
            var existing = vehicleRepo.findByName(vehicle.getVehicleName());

            if (existing.isPresent()) {
                syncVehicle(existing.get(), vehicle);
                continue;
            }

            VehicleTypeEntity vehicleType = vehicleTypeRepo
                    .findByName(vehicle.getVehicleType().name())
                    .orElseThrow(() -> new RuntimeException(
                            "VehicleType not found: " + vehicle.getVehicleType().name()
                    ));

            VehicleClassEntity vehicleClass = vehicleClassRepo
                    .findByClassName(vehicle.getVehicleClass().name())
                    .orElseThrow(() -> new RuntimeException(
                            "VehicleClass not found: " + vehicle.getVehicleClass().name()
                    ));

            GroundVehicleCard card = vehicleRepo.save(new GroundVehicleCard(
                    vehicle,
                    vehicleType,
                    vehicleClass
            ));

            for (VehicleAttackInterface attack : vehicle.getVehicleAttacks()) {
                WeaponEntity weapon = weaponRepo
                        .findByWeaponName(attack.getWeapon().name())
                        .orElseThrow(() -> new RuntimeException(
                                "Weapon not found: " + attack.getWeapon().name()
                        ));
                vehicleAttackRepo.save(new VehicleAttackEntity(
                        card,
                        attack.getAttackName(),
                        attack.getAttackSlot(),
                        weapon,
                        attack.getBaseDamage(),
                        attack.getAmmoCost(),
                        attack.getFuelCost(),
                        attack.getSpecialEffect()
                ));
            }
        }
    }

    /**
     * Seeds all {@link AmmoSupplyCrate} enum constants into the ammunition_cards table.
     * Each entry represents a playable item card that resupplies a specific
     * ammunition type to a unit on the field.
     * @param crates an array of {@link AmmunitionItemInterface} values
     * @throws RuntimeException if required data is missing
     */
    private void loadAmmunitionCards(AmmunitionItemInterface[] crates) {
        for (AmmunitionItemInterface crate : crates) {
            var existing = ammunitionCardRepo.findByName(crate.getItemName());
            if (existing.isPresent()) {
                AmmunitionCard card = existing.get();
                boolean changed = syncItem(card, crate);
                if (!Objects.equals(card.getAmmunition(), crate.getAmmunition())) { card.setAmmunition(crate.getAmmunition()); changed = true; }
                if (!Objects.equals(card.getCount(), crate.getCount())) { card.setCount(crate.getCount()); changed = true; }
                if (changed) { ammunitionCardRepo.save(card); logger.info("Updated card from the card library: {}", card.getName()); }
                continue;
            }
            ammunitionCardRepo.save(new AmmunitionCard(crate));
        }
    }

    /**
     * Seeds all {@link FuelSupplyDrum} enum constants into the fuel_cards table.
     * @param drums an array of {@link FuelItemInterface} values
     */
    private void loadFuelCards(FuelItemInterface[] drums) {
        for (FuelItemInterface drum : drums) {
            var existing = fuelCardRepo.findByName(drum.getItemName());
            if (existing.isPresent()) {
                FuelCard card = existing.get();
                boolean changed = syncItem(card, drum);
                if (!Objects.equals(card.getCount(), drum.getCount())) { card.setCount(drum.getCount()); changed = true; }
                if (changed) { fuelCardRepo.save(card); logger.info("Updated card from the card library: {}", card.getName()); }
                continue;
            }
            fuelCardRepo.save(new FuelCard(drum));
        }
    }

    /**
     * Seeds all {@link RepairSupplyKit} enum constants into the repair_cards table.
     * @param kits an array of {@link RepairItemInterface} values
     */
    private void loadRepairCards(RepairItemInterface[] kits) {
        for (RepairItemInterface kit : kits) {
            var existing = repairCardRepo.findByName(kit.getItemName());
            if (existing.isPresent()) {
                RepairCard card = existing.get();
                boolean changed = syncItem(card, kit);
                if (!Objects.equals(card.getCount(), kit.getCount())) { card.setCount(kit.getCount()); changed = true; }
                if (!Objects.equals(card.getRepairAmount(), kit.getRepairAmount())) { card.setRepairAmount(kit.getRepairAmount()); changed = true; }
                if (changed) { repairCardRepo.save(card); logger.info("Updated card from the card library: {}", card.getName()); }
                continue;
            }
            repairCardRepo.save(new RepairCard(kit));
        }
    }

    /**
     * Seeds all {@link SupplyCrate} enum constants into the supply_cards table.
     * @param crates an array of {@link SupplyItemInterface} values
     */
    private void loadSupplyCards(SupplyItemInterface[] crates) {
        for (SupplyItemInterface crate : crates) {
            var existing = supplyCardRepo.findByName(crate.getItemName());
            if (existing.isPresent()) {
                SupplyCard card = existing.get();
                boolean changed = syncItem(card, crate);
                if (!Objects.equals(card.getCount(), crate.getCount())) { card.setCount(crate.getCount()); changed = true; }
                if (changed) { supplyCardRepo.save(card); logger.info("Updated card from the card library: {}", card.getName()); }
                continue;
            }
            supplyCardRepo.save(new SupplyCard(crate));
        }
    }

    /** Seeds the {@link SpecialItem} enum constants (ERA, Artillery, Search, Draw) into the special_item_cards table. */
    private void loadSpecialItems(SpecialItemInterface[] items) {
        for (SpecialItemInterface item : items) {
            var existing = specialItemRepo.findByName(item.getItemName());
            if (existing.isPresent()) {
                SpecialItemCard card = existing.get();
                boolean changed = syncItem(card, item);
                int primary = (int) Math.round(item.getPrimaryEffectValue());
                int secondary = (int) Math.round(item.getSecondaryEffectValue());
                if (card.getEffect() != item.getSpecialItemEffect()) { card.setEffect(item.getSpecialItemEffect()); changed = true; }
                if (!Objects.equals(card.getPrimaryValue(), primary)) { card.setPrimaryValue(primary); changed = true; }
                if (!Objects.equals(card.getSecondaryValue(), secondary)) { card.setSecondaryValue(secondary); changed = true; }
                if (changed) { specialItemRepo.save(card); logger.info("Updated card from the card library: {}", card.getName()); }
                continue;
            }
            specialItemRepo.save(new SpecialItemCard(item));
        }
    }

    /** Cards that were in the card library once and were removed from it. They are deleted unless a saved deck still uses them. */
    private static final List<String> RETIRED_CARDS = List.of("Cyber Intrusion", "Strategic Disruption");

    private void retireCards() {
        for (String name : RETIRED_CARDS) {
            specialItemRepo.findByName(name).ifPresent(card -> {
                if (deckRepo.countUsingCard(card.getId()) > 0) {
                    logger.warn("Retired card '{}' is still in a saved deck, so it was kept.", name);
                    return;
                }
                if (inUnfinishedMatch(card.getId())) {
                    logger.warn("Retired card '{}' is still in a game or challenge in progress, so it was kept for now.", name);
                    return;
                }
                for (var player : playerRepo.findAll()) {
                    if (player.getFavoriteCardIds().remove(card.getId())) playerRepo.save(player);
                }
                specialItemRepo.delete(card);
                logger.info("Removed retired card: {}", name);
            });
        }
    }

    /** True if a pending challenge's deck or a game in progress still holds this card (deleting it would break that game). */
    private boolean inUnfinishedMatch(long cardId) {
        if (matchRepo.countPendingChallengesUsingCard(cardId) > 0) return true;
        tools.jackson.databind.json.JsonMapper json = tools.jackson.databind.json.JsonMapper.builder().build();
        for (var m : matchRepo.findByStatus(com.example.valtrak.Data.GameData.Enums.MatchStatus.ACTIVE)) {
            if (m.getStateJson() == null) continue;
            var s = json.readValue(m.getStateJson(), com.example.valtrak.Gameplay.Engine.GameState.class);
            for (var p : s.players) {
                if (p.deck.contains(cardId) || p.hand.contains(cardId) || p.discard.contains(cardId)) return true;
                for (var g : p.groups) {
                    if (g.jammerCardId == cardId) return true;
                    for (var v : g.vehicles) if (v.cardId == cardId || v.eraCardId == cardId || v.camoCardId == cardId) return true;
                }
            }
        }
        return false;
    }

    // ── keeping existing cards in step with the enums ────────────────────────
    // Cards are matched by name. Change a card's numbers, rarity or text in its enum and the database follows on the next
    // start. (Renaming a card creates a new card; the old one stays in the database.)

    /** Copies rarity, description and item type from the enum; returns true if anything changed. */
    private static boolean syncItem(ItemCard card, ItemCardInterface data) {
        boolean changed = false;
        if (card.getLevel() != data.getCardLevel()) { card.setLevel(data.getCardLevel()); changed = true; }
        if (!Objects.equals(card.getDescription(), data.getItemDescription())) { card.setDescription(data.getItemDescription()); changed = true; }
        if (card.getItemType() != data.getItemType()) { card.setItemType(data.getItemType()); changed = true; }
        return changed;
    }

    private void syncVehicle(GroundVehicleCard card, GroundVehicleCardInterface data) {
        VehicleTypeEntity type = vehicleTypeRepo.findByName(data.getVehicleType().name())
                .orElseThrow(() -> new RuntimeException("VehicleType not found: " + data.getVehicleType().name()));
        VehicleClassEntity vehicleClass = vehicleClassRepo.findByClassName(data.getVehicleClass().name())
                .orElseThrow(() -> new RuntimeException("VehicleClass not found: " + data.getVehicleClass().name()));
        var ability = data.getAbility();

        boolean changed = false;
        if (card.getLevel() != data.getLevel()) { card.setLevel(data.getLevel()); changed = true; }
        if (!Objects.equals(card.getDescription(), data.getDescription())) { card.setDescription(data.getDescription()); changed = true; }
        if (!Objects.equals(card.getVehicleNation(), data.getVehicleNation())) { card.setVehicleNation(data.getVehicleNation()); changed = true; }
        if (!Objects.equals(card.getVehicleArmor(), data.getVehicleArmor())) { card.setVehicleArmor(data.getVehicleArmor()); changed = true; }
        if (!Objects.equals(card.getVehicleHP(), data.getVehicleHP())) { card.setVehicleHP(data.getVehicleHP()); changed = true; }
        if (card.getVehicleType() == null || !card.getVehicleType().getId().equals(type.getId())) { card.setVehicleType(type); changed = true; }
        if (card.getVehicleClass() == null || !card.getVehicleClass().getId().equals(vehicleClass.getId())) { card.setVehicleClass(vehicleClass); changed = true; }
        var abilityType = ability == null ? null : ability.type();
        Integer power = ability == null ? null : ability.power();
        Integer fuel = ability == null ? null : ability.fuelCost();
        if (card.getAbilityType() != abilityType || !Objects.equals(card.getAbilityPower(), power)
                || !Objects.equals(card.getAbilityFuelCost(), fuel)) {
            card.setAbilityType(abilityType);
            card.setAbilityPower(power);
            card.setAbilityFuelCost(fuel);
            changed = true;
        }
        if (changed) {
            vehicleRepo.save(card);
            logger.info("Updated card from the card library: {}", card.getName());
        }

        List<VehicleAttackEntity> have = vehicleAttackRepo.findByVehicle(card);
        List<? extends VehicleAttackInterface> wanted = data.getVehicleAttacks();
        boolean same = have.size() == wanted.size()
                && wanted.stream().allMatch(w -> have.stream().anyMatch(h -> attackMatches(h, w)));
        if (!same) {
            vehicleAttackRepo.deleteAll(have);
            for (VehicleAttackInterface attack : wanted) {
                WeaponEntity weapon = weaponRepo.findByWeaponName(attack.getWeapon().name())
                        .orElseThrow(() -> new RuntimeException("Weapon not found: " + attack.getWeapon().name()));
                vehicleAttackRepo.save(new VehicleAttackEntity(card, attack.getAttackName(), attack.getAttackSlot(), weapon,
                        attack.getBaseDamage(), attack.getAmmoCost(), attack.getFuelCost(), attack.getSpecialEffect()));
            }
            logger.info("Updated attacks from the card library: {}", card.getName());
        }
    }

    private static boolean attackMatches(VehicleAttackEntity have, VehicleAttackInterface wanted) {
        return have.getAttackSlot() == wanted.getAttackSlot()
                && Objects.equals(have.getAttackName(), wanted.getAttackName())
                && have.getWeapon().getWeaponName().equals(wanted.getWeapon().name())
                && Objects.equals(have.getBaseDamage(), wanted.getBaseDamage())
                && Objects.equals(have.getAmmoCost(), wanted.getAmmoCost())
                && Objects.equals(have.getFuelCost(), wanted.getFuelCost())
                && have.getSpecialEffect() == wanted.getSpecialEffect();
    }
}
