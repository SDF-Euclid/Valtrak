package com.example.valtrak.Data.CardLibrary.Vehicles.France;

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

//TODO: Review stats (placeholders, following the bands in docs/VEHICLE_ROSTER.md)

/** France's tanks. */
@Getter
@AllArgsConstructor
public enum FrenchVehicles implements GroundVehicleCardInterface {

    AMX_10_RC("AMX-10 RC",
            "France",
            "A fast six-wheeled armored car with a 105mm gun.",
            CardLevel.COMMON,
            VehicleType.GROUND,
            VehicleClass.LIGHT_TANK,
            25,
            120,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 28, 2, 0, SpecialEffect.NONE)
            )
    ),

    AMX_30B2("AMX-30B2",
            "France",
            "A fast, lightly armored French Cold War tank.",
            CardLevel.UNCOMMON,
            VehicleType.GROUND,
            VehicleClass.MEDIUM_TANK,
            50,
            195,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 34, 2, 0, SpecialEffect.NONE)
            )
    ),

    LECLERC("Leclerc",
            "France",
            "A fast French main battle tank with an autoloaded 120mm gun.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            88,
            260,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 43, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 72, 3, 1, SpecialEffect.NONE)
            )
    ),

    LECLERC_XLR("Leclerc XLR",
            "France",
            "The modernised Leclerc with new armor and networked systems.",
            CardLevel.LEGENDARY,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            100,
            290,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 46, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 80, 3, 1, SpecialEffect.PIERCE)
            )
    );

    private final String vehicleName;
    private final String vehicleNation;
    private final String description;
    private final CardLevel level;
    private final VehicleType vehicleType;
    private final VehicleClass vehicleClass;
    private final Integer vehicleArmor;
    private final Integer vehicleHP;
    private final List<VehicleAttackInterface> vehicleAttacks;
}
