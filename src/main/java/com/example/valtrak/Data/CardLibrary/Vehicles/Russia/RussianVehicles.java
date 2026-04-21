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
                    new VehicleAttackDefinition("Sabo barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_125MM, 85, 3, 1, SpecialEffect.PIERCE)
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
