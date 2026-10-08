package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.AmmoSupplyCrate;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.FuelSupplyDrum;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.RepairSupplyKit;
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
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.VehicleAttackInterface;
import com.example.valtrak.Data.CardLibrary.Nations;
import com.example.valtrak.Data.CardLibrary.Vehicles.Germany.GermanVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.Russia.RussianVehicles;
import com.example.valtrak.Data.CardLibrary.Vehicles.US.USGroundVehicles;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.*;
import com.example.valtrak.Data.GameData.Repository.Cards.AmmunitionCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.FuelCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.RepairCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.VehicleCardRepository;
import com.example.valtrak.Data.GameData.Repository.EnumData.*;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Resource.FuelCard;
import com.example.valtrak.Gameplay.Cards.Resource.RepairCard;
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
        loadGroundVehicles(USGroundVehicles.values());
        loadGroundVehicles(RussianVehicles.values());
        loadGroundVehicles(GermanVehicles.values());
        loadAmmunitionCards(AmmoSupplyCrate.values());
        loadFuelCards(FuelSupplyDrum.values());
        loadRepairCards(RepairSupplyKit.values());
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
                GroundVehicleCard card = existing.get();
                if (card.getVehicleHP() == null) {
                    card.setVehicleHP(vehicle.getVehicleHP());
                    vehicleRepo.save(card);
                }
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
            if (ammunitionCardRepo.existsByName(crate.getItemName())) continue;
            ammunitionCardRepo.save(new AmmunitionCard(crate));
        }
    }

    /**
     * Seeds all {@link FuelSupplyDrum} enum constants into the fuel_cards table.
     * @param drums an array of {@link FuelItemInterface} values
     */
    private void loadFuelCards(FuelItemInterface[] drums) {
        for (FuelItemInterface drum : drums) {
            if (fuelCardRepo.existsByName(drum.getItemName())) continue;
            fuelCardRepo.save(new FuelCard(drum));
        }
    }

    /**
     * Seeds all {@link RepairSupplyKit} enum constants into the repair_cards table.
     * @param kits an array of {@link RepairItemInterface} values
     */
    private void loadRepairCards(RepairItemInterface[] kits) {
        for (RepairItemInterface kit : kits) {
            if (repairCardRepo.existsByName(kit.getItemName())) continue;
            repairCardRepo.save(new RepairCard(kit));
        }
    }
}
