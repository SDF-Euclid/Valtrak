package com.example.valtrak.Data.CardLibrary.Vehicles.US;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.*;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.VehicleAttackInterface;
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

    M3_BRADLEY("M3 Bradley",
            "United States",
            "A state-of-the-art IFV.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.LIGHT_TANK,
            45, //Review
            175,
            List.of(
                    new VehicleAttackDefinition("Autocannon", AttackSlot.ATTACK_1, Weapon.BROWNING_50CAL, 20, 1, 0, SpecialEffect.SUPPRESSION),
                    new VehicleAttackDefinition("TOW missile", AttackSlot.ATTACK_2, Weapon.TOW_ATGM, 55, 2, 1, SpecialEffect.NONE),
                    new VehicleAttackDefinition("Missile Barrage", AttackSlot.ATTACK_3, Weapon.TOW_ATGM, 85, 3, 2, SpecialEffect.DISABLE)
            )
    ),

   M1126_STRYKER("M1126 Stryker",
            "United States",
            "A state-of-the-art APC.",
            CardLevel.UNCOMMON,
            VehicleType.GROUND,
            VehicleClass.LIGHT_TANK,
            25, //Review
            130,
            List.of(
                    new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.BROWNING_50CAL, 12, 1, 0, SpecialEffect.SUPPRESSION),
                    new VehicleAttackDefinition("Grenade Launcher", AttackSlot.ATTACK_2, Weapon.MK19_40MM, 30, 2, 0, SpecialEffect.STUN)
            )
    ),

    /*=====================================================*/

    /*==================== MEDIUM TANKS ====================*/

    M60_PATTON("M60 Patton",
            "United States",
            "A older medium tank.",
            CardLevel.RARE,
            VehicleType.GROUND,
            VehicleClass.MEDIUM_TANK,
            70, //Review
            220,
            List.of(
                    new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.BROWNING_50CAL, 12, 1, 0, SpecialEffect.SUPPRESSION),
                    new VehicleAttackDefinition("Cannon Shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 35, 2, 0, SpecialEffect.NONE)
            )
    ),

    /*======================================================*/

    /*==================== HEAVY TANKS ====================*/



    /*=====================================================*/

    /*==================== MAIN BATTLE TANKS ====================*/

    M1A1_ABRAMS("M1A1 Abrams",
            "United States",
            "A state-of-the-art main battle tank designed with superior firepower and crew survivability in mind.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            85, //Review
            260,
            List.of(
                    new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.BROWNING_50CAL, 15, 1, 0, SpecialEffect.SUPPRESSION),
                    new VehicleAttackDefinition("Cannon Shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 40, 2, 0, SpecialEffect.NONE),
                    new VehicleAttackDefinition("Sabot Barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 70, 3, 1, SpecialEffect.NONE)
            )
    ),

    M1A3_ABRAMS("M1A3 Abrams",
                "United States",
                "A state-of-the-art main battle tank designed with superior firepower and crew survivability in mind.",
                CardLevel.LEGENDARY,
                VehicleType.GROUND,
                VehicleClass.MAIN_BATTLE_TANK,
                100, //Review
                300,
                List.of(
                        new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.BROWNING_50CAL, 15, 1, 0, SpecialEffect.SUPPRESSION),
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
    private final Integer vehicleHP; //Review
    private final List<VehicleAttackInterface> vehicleAttacks;
}