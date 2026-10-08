package com.example.valtrak.Data.CardLibrary.Vehicles.Support;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.AbilityType;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.VehicleAttackInterface;
import com.example.valtrak.Data.GameData.Config.Records.VehicleAbilityDefinition;
import com.example.valtrak.Data.GameData.Config.Records.VehicleAttackDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

//TODO: Review recon stats and names (placeholders)

/**
 * Reconnaissance vehicles: light, armed scouts that take a Line slot. While face up they can turn enemy vehicles face up
 * (Fuel from the group's pool).
 */
@Getter
@AllArgsConstructor
public enum ReconVehicles implements GroundVehicleCardInterface {

    BRDM_2("BRDM-2 Scout", "Russia",
            "A light amphibious scout car.",
            CardLevel.COMMON, VehicleType.GROUND, VehicleClass.RECON, 20, 70,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 10, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    M1127_RV("M1127 Reconnaissance Vehicle", "United States",
            "A wheeled reconnaissance vehicle with advanced sensors.",
            CardLevel.UNCOMMON, VehicleType.GROUND, VehicleClass.RECON, 30, 95,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.BROWNING_50CAL, 12, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    FENNEK("Fennek", "Germany",
            "A quiet, fast reconnaissance vehicle with a masts-mounted sensor suite.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.RECON, 25, 80,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.MG3_762MM, 12, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1));

    private final String vehicleName;
    private final String vehicleNation;
    private final String description;
    private final CardLevel level;
    private final VehicleType vehicleType;
    private final VehicleClass vehicleClass;
    private final Integer vehicleArmor;
    private final Integer vehicleHP;
    private final List<VehicleAttackInterface> vehicleAttacks;
    private final VehicleAbilityDefinition ability;
}
