package com.example.valtrak.Data.CardLibrary.Vehicles.US;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.*;
import com.example.valtrak.Data.CardLibrary.Interfaces.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.VehicleAttackInterface;
import com.example.valtrak.Data.GameData.Config.Records.VehicleAttackDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

//TODO: Review vehicle health

/**
 *
 */
@Getter
@AllArgsConstructor
public enum USGroundVehicles implements GroundVehicleCardInterface {

    /*======================================== GROUND VEHICLES ========================================*/

    /*==================== LIGHT TANKS ====================*/

    /*
    M3_BRADLEY("M3 Bradley",
               "United States",
               "A light reconnaissance vehicle with devastating firepower.",
               CardLevel.RARE,
               VehicleType.GROUND,
               VehicleClass.LIGHT_TANK,
               60, //Review
               List.of(Weapon.BUSHMASTER_25MM, Weapon.TOW_ATGM),
               List.of()
    ),
    */


    /*=====================================================*/

    /*==================== MEDIUM TANKS ====================*/



    /*======================================================*/

    /*==================== HEAVY TANKS ====================*/



    /*=====================================================*/

    /*==================== MAIN BATTLE TANKS ====================*/

    M1A3_ABRAMS("M1A3 Abrams",
                "United States",
                "A state-of-the-art main battle tank designed with superior firepower and crew survivability in mind.",
                CardLevel.LEGENDARY,
                VehicleType.GROUND,
                VehicleClass.MAIN_BATTLE_TANK,
                100, //Review
                List.of(
                        new VehicleAttackDefinition("Suppressing Fire", AttackSlot.ATTACK_1, Weapon.BROWNING_50CAL, 15, 1, 0, SpecialEffect.SUPPRESSION),
                        new VehicleAttackDefinition("Cannon Shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 45, 2, 0, SpecialEffect.NONE),
                        new VehicleAttackDefinition("Sabot Barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 80, 3, 1, SpecialEffect.PIERCE)
                )
    );

    /*===========================================================*/

    /*=================================================================================================*/

    /**
     *
     */
    private final String vehicleName;
    private final String vehicleNation;
    private final String description;
    private final CardLevel level;
    private final VehicleType vehicleType;
    private final VehicleClass vehicleClass;
    private final Integer vehicleArmor; //Review
    private final List<VehicleAttackInterface> vehicleAttacks;
}