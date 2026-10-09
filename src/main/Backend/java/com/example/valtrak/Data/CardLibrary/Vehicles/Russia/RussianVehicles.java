package com.example.valtrak.Data.CardLibrary.Vehicles.Russia;

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
public enum RussianVehicles implements GroundVehicleCardInterface {

    /*======================================== GROUND VEHICLES ========================================*/

    /*==================== LIGHT TANKS ====================*/



    /*=====================================================*/

    /*==================== MEDIUM TANKS ====================*/



    /*======================================================*/

    /*==================== HEAVY TANKS ====================*/



    /*=====================================================*/

    /*==================== MAIN BATTLE TANKS ====================*/

    T14_ARMATA("T-14 Armata",
            "Russia",
            "A cutting-edge advanced MBT",
            CardLevel.LEGENDARY,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            110,
            310,
            List.of(
                    new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 12, 1, 0, SpecialEffect.SUPPRESSION),
                    new VehicleAttackDefinition("Cannon fire", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_125MM, 50, 2, 0, SpecialEffect.NONE),
                    new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_125MM, 85, 3, 1, SpecialEffect.PIERCE)
            )
    ),

    /*==================== ADDED IN THE ROSTER UPDATE (stats are placeholders) ====================*/

    T72A("T-72A",
            "Russia",
            "The Soviet workhorse tank of the late Cold War.",
            CardLevel.COMMON,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            60,
            200,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_125MM, 34, 2, 0, SpecialEffect.NONE)
            )
    ),

    SPRUT_SD("2S25 Sprut-SD",
            "Russia",
            "An air-droppable light tank carrying a full-size 125mm gun.",
            CardLevel.UNCOMMON,
            VehicleType.GROUND,
            VehicleClass.LIGHT_TANK,
            35,
            150,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_125MM, 32, 2, 0, SpecialEffect.NONE)
            )
    ),

    T72B3("T-72B3",
            "Russia",
            "A modernised T-72 with a new fire-control system and reactive armor.",
            CardLevel.RARE,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            80,
            240,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 12, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_125MM, 40, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_125MM, 66, 3, 1, SpecialEffect.NONE)
            )
    ),

    T80BVM("T-80BVM",
            "Russia",
            "A gas-turbine tank upgraded for speed and protection.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            88,
            265,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_125MM, 43, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_125MM, 72, 3, 1, SpecialEffect.NONE)
            )
    ),

    T90M("T-90M",
            "Russia",
            "Russia's most capable serial-production tank.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            92,
            275,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_125MM, 43, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_125MM, 72, 3, 1, SpecialEffect.NONE)
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
