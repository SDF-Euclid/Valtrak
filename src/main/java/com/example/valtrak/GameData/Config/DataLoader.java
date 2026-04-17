package com.example.valtrak.GameData.Config;

import com.example.valtrak.CardData.Information.VehicleInfo.*;
import com.example.valtrak.CardData.Information.WeaponInfo.*;
import com.example.valtrak.CardData.Nations.Information.Nations;
import com.example.valtrak.CardData.Nations.Vehicles.UnitedStatesVehicles;
import com.example.valtrak.GameData.Entity.DataTransfer.EnumData.*;
import com.example.valtrak.GameData.Repository.EnumData.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 *
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    /**
     *
     */
    private final DamageTypeRepository damageTypeRepo;
    private final VehicleTypeRepository vehicleTypeRepo;
    private final VehicleClassRepository vehicleClassRepo;
    private final AmmunitionRepository ammoRepo;
    private final PrimaryWeaponRepository primaryWeaponRepo;
    private final SecondaryWeaponRepository secondaryWeaponRepo;
    private final VehicleRepository vehicleRepo;
    private final NationRepository nationRepo;

    @Override
    public void run(String @NonNull ... args) {
        loadDamageTypes();
        loadVehicleTypes();
        loadVehicleClasses();
        loadAmmunition();
        loadPrimaryWeapons();
        loadSecondaryWeapons();
        loadUnitedStatesVehicles();
        loadNations();
    }

    /*==================== DAMAGE TYPE ====================*/
    private void loadDamageTypes() {
        for (DamageType dt : DamageType.values()) {
            if (!damageTypeRepo.existsByName(dt.name())) {
                damageTypeRepo.save(new DamageTypeEntity(dt.name()));
            }
        }
    }

    /*==================== VEHICLE TYPE ====================*/
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

    /*==================== PRIMARY WEAPONS ====================*/
    private void loadPrimaryWeapons() {
        for (PrimaryWeapon pw : PrimaryWeapon.values()) {
            if (!primaryWeaponRepo.existsByWeaponName(pw.name())) {
                List<AmmunitionEntity> ammoEntities = pw.getCompatibleAmmunition().stream()
                        .map(a -> ammoRepo.findByName(a.name())
                                .orElseThrow(() -> new RuntimeException(
                                        "Ammunition not found: " + a.name()
                                )))
                        .toList();
                primaryWeaponRepo.save(new PrimaryWeaponEntity(pw.name(), ammoEntities));
            }
        }
    }

    /*==================== SECONDARY WEAPONS ====================*/
    private void loadSecondaryWeapons() {
        for (SecondaryWeapon sw : SecondaryWeapon.values()) {
            if (!secondaryWeaponRepo.existsByWeaponName(sw.name())) {
                List<AmmunitionEntity> ammoEntities = sw.getCompatibleAmmunition().stream()
                        .map(a -> ammoRepo.findByName(a.name())
                                .orElseThrow(() -> new RuntimeException(
                                        "Ammunition not found: " + a.name()
                                )))
                        .toList();
                secondaryWeaponRepo.save(new SecondaryWeaponEntity(sw.name(), ammoEntities));
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

    /*============================== INITIALIZE DATA BASE ==============================*/

    /*==================== UNITED STATES VEHICLES ====================*/
    private void loadUnitedStatesVehicles() {
        for (UnitedStatesVehicles vehicle : UnitedStatesVehicles.values()) {
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

                List<PrimaryWeaponEntity> primaryWeapons = vehicle.getPrimaryWeapons().stream()
                        .map(pw -> primaryWeaponRepo.findByWeaponName(pw.name())
                                .orElseThrow(() -> new RuntimeException(
                                        "PrimaryWeapon not found: " + pw.name()
                                )))
                        .toList();

                List<SecondaryWeaponEntity> secondaryWeapons = vehicle.getSecondaryWeapons().stream()
                        .map(sw -> secondaryWeaponRepo.findByWeaponName(sw.name())
                                .orElseThrow(() -> new RuntimeException(
                                        "SecondaryWeapon not found: " + sw.name()
                                )))
                        .toList();

                vehicleRepo.save(new VehicleCard(
                        vehicle.getVehicleName(),
                        vehicle.getDescription(),
                        "United States",
                        vehicleType,
                        vehicleClass,
                        vehicle.getVehicleArmor(),
                        primaryWeapons,
                        secondaryWeapons
                ));
            }
        }
    }
}
