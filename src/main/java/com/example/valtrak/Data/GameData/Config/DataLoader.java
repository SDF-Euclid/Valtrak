package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.CardLibrary.Information.VehicleInfo.*;
import com.example.valtrak.Data.CardLibrary.Information.WeaponInfo.*;
import com.example.valtrak.Data.CardLibrary.Vehicles.Interfaces.GroundVehicleCardInterface;
import com.example.valtrak.Data.GameData.DataTransfer.EnumData.*;
import com.example.valtrak.Data.Nations;
import com.example.valtrak.Data.CardLibrary.Vehicles.US.USGroundVehicles;
import com.example.valtrak.Data.GameData.Repository.EnumData.*;
import com.example.valtrak.Gameplay.Cards.GroundVehicleCard;
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
        loadNations();
        loadGroundVehicles(USGroundVehicles.values());
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

                vehicleRepo.save(new GroundVehicleCard(
                        vehicle,
                        vehicleType,
                        vehicleClass,
                        primaryWeapons,
                        secondaryWeapons
                ));
            }
        }
    }
}
