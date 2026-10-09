package com.example.valtrak.Data.CardLibrary.Vehicles.Germany;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.VehicleAttackInterface;
import com.example.valtrak.Data.GameData.Config.Records.VehicleAttackDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum GermanVehicles implements GroundVehicleCardInterface {

    /*======================================== GROUND VEHICLES ========================================*/

    /*==================== LIGHT TANKS ====================*/



    /*=====================================================*/

    /*==================== MEDIUM TANKS ====================*/



    /*======================================================*/

    /*==================== HEAVY TANKS ====================*/



    /*=====================================================*/

    /*==================== MAIN BATTLE TANKS ====================*/

    LEOPARD_2A7V("Leopard 2A7V",
            "Germany",
            "A cutting edge MBT",
            CardLevel.LEGENDARY,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            105,
            295,
            List.of(
                    new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.MG3_762MM, 12, 1, 0, SpecialEffect.SUPPRESSION),
                    new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 48, 2, 0, SpecialEffect.NONE),
                    new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 82, 3, 1, SpecialEffect.PIERCE)
            )
    ),

    /*==================== ADDED IN THE ROSTER UPDATE (stats are placeholders) ====================*/

    LEOPARD_1A1("Leopard 1A1",
            "Germany",
            "The first post-war German tank: fast and well armed, but lightly protected.",
            CardLevel.COMMON,
            VehicleType.GROUND,
            VehicleClass.MEDIUM_TANK,
            45,
            180,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.MG3_762MM, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 30, 2, 0, SpecialEffect.NONE)
            )
    ),

    LEOPARD_1A5("Leopard 1A5",
            "Germany",
            "A Leopard 1 with a modern fire-control system.",
            CardLevel.UNCOMMON,
            VehicleType.GROUND,
            VehicleClass.MEDIUM_TANK,
            55,
            200,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.MG3_762MM, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 34, 2, 0, SpecialEffect.NONE)
            )
    ),

    LEOPARD_2A4("Leopard 2A4",
            "Germany",
            "The Cold War Leopard 2 that equipped half of Europe.",
            CardLevel.RARE,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            82,
            245,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.MG3_762MM, 12, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 40, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 66, 3, 1, SpecialEffect.NONE)
            )
    ),

    LEOPARD_2A6("Leopard 2A6",
            "Germany",
            "A Leopard 2 with a longer, more powerful 120mm gun.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            92,
            275,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.MG3_762MM, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 43, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 72, 3, 1, SpecialEffect.NONE)
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
