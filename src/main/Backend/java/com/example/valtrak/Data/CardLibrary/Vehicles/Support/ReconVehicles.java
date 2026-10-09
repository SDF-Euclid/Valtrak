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
            "A quiet, fast reconnaissance vehicle with a mast-mounted sensor suite.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.RECON, 25, 80,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.MG3_762MM, 12, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    /*==================== ADDED IN THE ROSTER UPDATE ====================*/

    TYPE_87_RCV("Type 87 Recon Vehicle", "Japan",
            "A six-wheeled Japanese scout vehicle.",
            CardLevel.UNCOMMON, VehicleType.GROUND, VehicleClass.RECON, 25, 85,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 12, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    ZBL_08_RECON("ZBL-08 Recon Vehicle", "China",
            "An eight-wheeled Chinese reconnaissance vehicle with a sensor mast.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.RECON, 25, 90,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.PKT_762MM, 12, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    FV107_SCIMITAR("FV107 Scimitar", "United Kingdom",
            "A small, fast British tracked scout.",
            CardLevel.COMMON, VehicleType.GROUND, VehicleClass.RECON, 20, 70,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    VBL("VBL Scout Car", "France",
            "A light, nimble French armored scout car.",
            CardLevel.COMMON, VehicleType.GROUND, VehicleClass.RECON, 20, 65,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    SAND_CAT("Sand Cat Scout", "Israel",
            "A protected Israeli patrol and scout vehicle.",
            CardLevel.UNCOMMON, VehicleType.GROUND, VehicleClass.RECON, 20, 75,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 12, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    PATGB_360("Patgb 360 Recon", "Sweden",
            "A Swedish eight-wheeled vehicle fitted for reconnaissance.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.RECON, 30, 95,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 12, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    LINCE("Lince Recon Vehicle", "Italy",
            "An Italian light multirole vehicle used for scouting.",
            CardLevel.COMMON, VehicleType.GROUND, VehicleClass.RECON, 20, 70,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    K151("K151 Recon Vehicle", "South Korea",
            "A Korean light tactical vehicle used for scouting.",
            CardLevel.COMMON, VehicleType.GROUND, VehicleClass.RECON, 15, 65,
            List.of(new VehicleAttackDefinition("MG Fire", AttackSlot.ATTACK_1, Weapon.COAX_MG_762_NATO, 10, 1, 0, SpecialEffect.SUPPRESSION)),
            new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1));

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
