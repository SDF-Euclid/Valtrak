package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.*;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.*;
import com.example.valtrak.Data.CardLibrary.Interfaces.*;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.*;
import com.example.valtrak.Data.CardLibrary.Nations;
import com.example.valtrak.Data.CardLibrary.Vehicles.US.USGroundVehicles;
import com.example.valtrak.Data.GameData.Repository.EnumData.*;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final DamageTypeRepository damageTypeRepo;
    private final DamageTypeMatchupRepository damageTypeMatchupRepo;
    private final VehicleTypeRepository vehicleTypeRepo;
    private final VehicleClassRepository vehicleClassRepo;
    private final AmmunitionRepository ammoRepo;
    private final WeaponRepository weaponRepo;
    private final NationRepository nationRepo;
    private final VehicleRepository vehicleRepo;
    private final VehicleAttackRepository vehicleAttackRepo;

    @Override
    public void run(String @NonNull ... args) {
        loadDamageTypes();
        loadVehicleTypes();
        loadVehicleClasses();
        loadAmmunition();
        loadWeapons();
        loadNations();
        loadGroundVehicles(USGroundVehicles.values());
    }

    /*==================== DAMAGE TYPES ====================*/
    private void loadDamageTypes() {
        for (DamageType dt : DamageType.values()) {
            if (!damageTypeRepo.existsByName(dt.name())) {
                damageTypeRepo.save(new DamageTypeEntity(dt.name()));
            }
        }
    }

    /*==================== DAMAGE MATCHUPS ====================*/
    private void loadDamageTypeMatchups() {
        Map<DamageType, Map<ArmorBracket, Double>> modifiers = Map.of(
                DamageType.KINETIC, Map.of(
                        ArmorBracket.UNARMORED,   0.6,
                        ArmorBracket.LIGHT,       0.8,
                        ArmorBracket.MEDIUM,      1.0,
                        ArmorBracket.HEAVY,       1.3,
                        ArmorBracket.SUPER_HEAVY, 1.5
                ),
                DamageType.CHEMICAL, Map.of(
                        ArmorBracket.UNARMORED,   1.0,
                        ArmorBracket.LIGHT,       1.0,
                        ArmorBracket.MEDIUM,      1.0,
                        ArmorBracket.HEAVY,       1.0,
                        ArmorBracket.SUPER_HEAVY, 1.0
                ),
                DamageType.EXPLOSIVE, Map.of(
                        ArmorBracket.UNARMORED,   1.8,
                        ArmorBracket.LIGHT,       1.4,
                        ArmorBracket.MEDIUM,      0.7,
                        ArmorBracket.HEAVY,       0.4,
                        ArmorBracket.SUPER_HEAVY, 0.2
                ),
                DamageType.ELECTRIC, Map.of(
                        ArmorBracket.UNARMORED,   0.0,
                        ArmorBracket.LIGHT,       0.0,
                        ArmorBracket.MEDIUM,      0.0,
                        ArmorBracket.HEAVY,       0.0,
                        ArmorBracket.SUPER_HEAVY, 0.0
                )
        );

        Map<DamageType, Map<ArmorBracket, SpecialEffect>> autoEffects = Map.of(
                DamageType.KINETIC, Map.of(
                        ArmorBracket.UNARMORED,   SpecialEffect.NONE,
                        ArmorBracket.LIGHT,       SpecialEffect.NONE,
                        ArmorBracket.MEDIUM,      SpecialEffect.NONE,
                        ArmorBracket.HEAVY,       SpecialEffect.NONE,
                        ArmorBracket.SUPER_HEAVY, SpecialEffect.NONE
                ),
                DamageType.CHEMICAL, Map.of(
                        ArmorBracket.UNARMORED,   SpecialEffect.NONE,
                        ArmorBracket.LIGHT,       SpecialEffect.NONE,
                        ArmorBracket.MEDIUM,      SpecialEffect.NONE,
                        ArmorBracket.HEAVY,       SpecialEffect.NONE,
                        ArmorBracket.SUPER_HEAVY, SpecialEffect.NONE
                ),
                DamageType.EXPLOSIVE, Map.of(
                        ArmorBracket.UNARMORED,   SpecialEffect.STUN,
                        ArmorBracket.LIGHT,       SpecialEffect.STUN,
                        ArmorBracket.MEDIUM,      SpecialEffect.NONE,
                        ArmorBracket.HEAVY,       SpecialEffect.NONE,
                        ArmorBracket.SUPER_HEAVY, SpecialEffect.NONE
                ),
                DamageType.ELECTRIC, Map.of(
                        ArmorBracket.UNARMORED,   SpecialEffect.DISABLE,
                        ArmorBracket.LIGHT,       SpecialEffect.DISABLE,
                        ArmorBracket.MEDIUM,      SpecialEffect.DISABLE,
                        ArmorBracket.HEAVY,       SpecialEffect.DISABLE,
                        ArmorBracket.SUPER_HEAVY, SpecialEffect.DISABLE
                )
        );

        for (DamageType dt : DamageType.values()) {
            for (ArmorBracket bracket : ArmorBracket.values()) {
                if (damageTypeMatchupRepo.findByDamageTypeAndArmorBracket(dt, bracket).isEmpty()) {
                    damageTypeMatchupRepo.save(new DamageTypeMatchupEntity(
                            dt,
                            bracket,
                            modifiers.get(dt).get(bracket),
                            autoEffects.get(dt).get(bracket)
                    ));
                }
            }
        }
    }

    /*==================== VEHICLE TYPES ====================*/
    private void loadVehicleTypes() {
        for (VehicleType vt : VehicleType.values()) {
            if (!vehicleTypeRepo.existsByName(vt.name())) {
                vehicleTypeRepo.save(new VehicleTypeEntity(vt.name()));
            }
        }
    }

    /*==================== VEHICLE CLASSES ====================*/
    private void loadVehicleClasses() {
        for (VehicleClass vc : VehicleClass.values()) {
            if (!vehicleClassRepo.existsByClassName(vc.name())) {
                vehicleClassRepo.save(new VehicleClassEntity(vc.name()));
            }
        }
    }

    /*==================== AMMUNITION ====================*/
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

    /*==================== WEAPONS ====================*/
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

    /*==================== NATIONS ====================*/
    private void loadNations() {
        for (Nations n : Nations.values()) {
            if (!nationRepo.existsByNationName(n.getName())) {
                nationRepo.save(new NationEntity(n.getName(), n.getAbbreviation()));
            }
        }
    }

    /*==================== GROUND VEHICLES ====================*/
    private void loadGroundVehicles(GroundVehicleCardInterface[] vehicles) {
        for (GroundVehicleCardInterface vehicle : vehicles) {
            if (vehicleRepo.findByVehicleName(vehicle.getVehicleName()).isEmpty()) {

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
    }
}
