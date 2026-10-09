package com.example.valtrak.Data.CardLibrary.Vehicles.UK;

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

/** United Kingdom's tanks. */
@Getter
@AllArgsConstructor
public enum BritishVehicles implements GroundVehicleCardInterface {

    CHIEFTAIN_MK11("Chieftain Mk11",
            "United Kingdom",
            "A heavily armed and armored Cold War British tank.",
            CardLevel.UNCOMMON,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            65,
            215,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_120MM, 36, 2, 0, SpecialEffect.NONE)
            )
    ),

    CHALLENGER_1("Challenger 1",
            "United Kingdom",
            "A British main battle tank protected by Chobham armor.",
            CardLevel.RARE,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            82,
            245,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 12, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_120MM, 40, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Main gun barrage", AttackSlot.ATTACK_3, Weapon.RIFLED_CANNON_120MM, 66, 3, 1, SpecialEffect.NONE)
            )
    ),

    CHALLENGER_2("Challenger 2",
            "United Kingdom",
            "A famously well-protected British main battle tank.",
            CardLevel.EPIC,
            VehicleType.GROUND,
            VehicleClass.MAIN_BATTLE_TANK,
            95,
            280,
            List.of(
                new VehicleAttackDefinition("Coax MG", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new VehicleAttackDefinition("Cannon shot", AttackSlot.ATTACK_2, Weapon.RIFLED_CANNON_120MM, 43, 2, 0, SpecialEffect.NONE),
                new VehicleAttackDefinition("Main gun barrage", AttackSlot.ATTACK_3, Weapon.RIFLED_CANNON_120MM, 72, 3, 1, SpecialEffect.NONE)
            )
    ),

    CHALLENGER_3("Challenger 3",
            "United Kingdom",
            "The Challenger 2 rebuilt with a smoothbore 120mm gun and new armor.",
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
