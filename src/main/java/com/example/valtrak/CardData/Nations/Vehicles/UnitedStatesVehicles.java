package com.example.valtrak.CardData.Nations.Vehicles;

import com.example.valtrak.CardData.Information.VehicleInfo.*;
import com.example.valtrak.CardData.Information.WeaponInfo.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

//TODO: Review vehicle health

/**
 *
 */
@Getter
@AllArgsConstructor
public enum UnitedStatesVehicles {

    /*======================================== GROUND VEHICLES ========================================*/

    /*==================== LIGHT TANKS ====================*/

    M3_BRADLEY("M3 Bradley",
            "A light reconnaissance vehicle with devastating firepower.",
            VehicleType.GROUND,
            VehicleClass.LIGHT_TANK,
            60, //Review
            List.of(PrimaryWeapon.BUSHMASTER_25MM, PrimaryWeapon.TOW_ATGM),
            List.of()
    ),

    /*=====================================================*/

    /*==================== MEDIUM TANKS ====================*/



    /*======================================================*/

    /*==================== HEAVY TANKS ====================*/



    /*=====================================================*/

    /*==================== MAIN BATTLE TANKS ====================*/

    M1A3_ABRAMS("M1A3 Abrams",
            "A state-of-the-art main battle tank designed with superior firepower and crew survivability in mind.",
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            100, //Review
            List.of(PrimaryWeapon.SMOOTHBORE_CANNON_120MM),
            List.of(SecondaryWeapon.BROWNING_50CAL, SecondaryWeapon.BROWNING_50CAL)
    );

    /*===========================================================*/

    /*=================================================================================================*/

    /**
     *
     */
    private final String vehicleName;
    private final String description;
    private final VehicleType vehicleType;
    private final VehicleClass vehicleClass;
    private final Integer vehicleArmor; //Review
    private final List<PrimaryWeapon> primaryWeapons;
    private final List<SecondaryWeapon> secondaryWeapons;
}