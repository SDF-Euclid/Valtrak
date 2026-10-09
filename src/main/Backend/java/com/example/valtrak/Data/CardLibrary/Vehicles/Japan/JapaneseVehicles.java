package com.example.valtrak.Data.CardLibrary.Vehicles.Japan;

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

/** Japan's tanks. */
@Getter
@AllArgsConstructor
public enum JapaneseVehicles implements GroundVehicleCardInterface {

    TYPE_16_MCV("Type 16 MCV",
            "Japan",
            "A fast wheeled fire-support vehicle with a 105mm gun.",
            CardLevel.COMMON,
            VehicleType.GROUND,
            VehicleClass.LIGHT_TANK,
            28,
            125,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 28, 2, 0, SpecialEffect.NONE)
            )
    ),

    TYPE_74("Type 74",
            "Japan",
            "A Cold War Japanese tank with hydropneumatic suspension.",
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

    TYPE_90("Type 90",
            "Japan",
            "A third-generation main battle tank with an autoloaded 120mm gun.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            88,
            265,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 43, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 72, 3, 1, SpecialEffect.NONE)
            )
    ),

    TYPE_10("Type 10",
            "Japan",
            "A light but advanced main battle tank with modular armor and networked fire control.",
            CardLevel.LEGENDARY,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            100,
            295,
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
