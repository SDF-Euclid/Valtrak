package com.example.valtrak.CardData;

import com.example.valtrak.CardData.General.VehicleInfo.*;
import com.example.valtrak.CardData.General.WeaponInfo.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum UnitedStatesVehicles {
    M1A3_ABRAMS("M1A3 Abrams",
                "A state-of-the-art main battle tank designed with superior firepower and crew survivability in mind",
                VehicleType.GROUND,
                VehicleClass.MEDIUM_BATTLE_TANK,
                List.of(PrimaryWeapon.SMOOTHBORE_CANNON_120MM),
                List.of(SecondaryWeapon.BROWNING_50CAL)
    );

    /**
     *
     */
    private final String vehicleName;
    private final String description;
    private final VehicleType vehicleType;
    private final VehicleClass vehicleClass;
    private final List<PrimaryWeapon> primaryWeapons;
    private final List<SecondaryWeapon> secondaryWeapons;
}