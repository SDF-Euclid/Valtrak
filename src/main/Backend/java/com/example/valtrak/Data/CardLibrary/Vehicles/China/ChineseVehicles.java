package com.example.valtrak.Data.CardLibrary.Vehicles.China;

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

/** China's tanks. */
@Getter
@AllArgsConstructor
public enum ChineseVehicles implements GroundVehicleCardInterface {

    TYPE_59_II("Type 59-II",
            "China",
            "A Chinese T-54 derivative upgraded with a 105mm gun.",
            CardLevel.COMMON,
            VehicleType.GROUND,
            VehicleClass.MEDIUM_TANK,
            45,
            175,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 30, 2, 0, SpecialEffect.NONE)
            )
    ),

    ZTQ_15("ZTQ-15",
            "China",
            "A modern light tank built for mountains and high plateaus.",
            CardLevel.UNCOMMON,
            VehicleType.GROUND,
            VehicleClass.LIGHT_TANK,
            40,
            160,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 32, 2, 0, SpecialEffect.NONE)
            )
    ),

    TYPE_96B("Type 96B",
            "China",
            "A widely fielded Chinese main battle tank with an autoloaded 125mm gun.",
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

    ZTZ_99("ZTZ-99",
            "China",
            "A heavily armored Chinese main battle tank.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            90,
            270,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_125MM, 43, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_125MM, 72, 3, 1, SpecialEffect.NONE)
            )
    ),

    ZTZ_99A("ZTZ-99A",
            "China",
            "China's most advanced main battle tank.",
            CardLevel.LEGENDARY,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            105,
            300,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_125MM, 46, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_125MM, 80, 3, 1, SpecialEffect.PIERCE)
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
