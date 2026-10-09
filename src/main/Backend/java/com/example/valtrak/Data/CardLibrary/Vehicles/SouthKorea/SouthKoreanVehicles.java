package com.example.valtrak.Data.CardLibrary.Vehicles.SouthKorea;

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

/** South Korea's tanks. */
@Getter
@AllArgsConstructor
public enum SouthKoreanVehicles implements GroundVehicleCardInterface {

    M48A5K("M48A5K",
            "South Korea",
            "A Korean-upgraded M48 Patton.",
            CardLevel.COMMON,
            VehicleType.GROUND,
            VehicleClass.MEDIUM_TANK,
            45,
            175,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 30, 2, 0, SpecialEffect.NONE)
            )
    ),

    K1("K1",
            "South Korea",
            "South Korea's first home-built main battle tank.",
            CardLevel.UNCOMMON,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            65,
            215,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_105MM, 36, 2, 0, SpecialEffect.NONE)
            )
    ),

    K1A1("K1A1",
            "South Korea",
            "A K1 upgraded with a 120mm smoothbore gun.",
            CardLevel.RARE,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            80,
            240,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 12, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.SMOOTHBORE_CANNON_120MM, 40, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Sabot barrage", AttackSlot.ATTACK_3, Weapon.SMOOTHBORE_CANNON_120MM, 66, 3, 1, SpecialEffect.NONE)
            )
    ),

    K1A2("K1A2",
            "South Korea",
            "A networked K1A1 with new sights.",
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

    K2_BLACK_PANTHER("K2 Black Panther",
            "South Korea",
            "A cutting-edge Korean main battle tank with an autoloader and active protection.",
            CardLevel.LEGENDARY,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            105,
            300,
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
