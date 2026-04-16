package com.example.valtrak.CardData.Nations;

import com.example.valtrak.CardData.Information.VehicleInfo.*;
import com.example.valtrak.CardData.Information.WeaponInfo.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

//TODO: Review vehicle health and primary/secondary weapon damage for balance

/**
 *
 */
@Getter
@AllArgsConstructor
public enum UnitedStatesVehicles {

    /*==================== TANKS ====================*/

    M1A3_ABRAMS("M1A3 Abrams",
                "A state-of-the-art main battle tank designed with superior firepower and crew survivability in mind.",
                VehicleType.GROUND,
                VehicleClass.MEDIUM_BATTLE_TANK,
                100, //Review
                List.of(PrimaryWeapon.SMOOTHBORE_CANNON_120MM),
                40, //Review
                List.of(SecondaryWeapon.BROWNING_50CAL, SecondaryWeapon.BROWNING_50CAL),
                5 //Review
    );

    /**
     *
     */
    private final String vehicleName;
    private final String description;
    private final VehicleType vehicleType;
    private final VehicleClass vehicleClass;
    private final Integer vehicleHealth; //Review
    private final List<PrimaryWeapon> primaryWeapons;
    private final Integer primaryDamage; //Review
    private final List<SecondaryWeapon> secondaryWeapons;
    private final Integer secondaryDamage; //Review
}