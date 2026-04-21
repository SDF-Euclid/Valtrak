package com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;


@Getter
@AllArgsConstructor
public enum Weapon {

    /*==================== SMOOTHBORE CANNONS ====================*/

    SMOOTHBORE_CANNON_120MM(List.of(Ammunition.APFSDS_120MM, Ammunition.HEAT_120MM)),
    SMOOTHBORE_CANNON_125MM(List.of(Ammunition.APFSDS_125MM, Ammunition.HEAT_125MM)),

    /*============================================================*/

    /*==================== RIFLED CANNONS ====================*/

    RIFLED_CANNON_105MM(List.of(Ammunition.APDS_105MM, Ammunition.APFSDS_105MM, Ammunition.SQUASH_HEAD_105MM)),

    /*========================================================*/

    /*==================== AUTO CANNONS ====================*/

    BUSHMASTER_25MM(List.of(Ammunition.APFSDS_25MM, Ammunition.HE_25MM)),

    /*======================================================*/

    /*==================== ATGM LAUNCHERS ====================*/

    TOW_ATGM(List.of(Ammunition.BGM_71_152MM)),

    /*========================================================*/

    /*==================== GRENADE LAUNCHERS ====================*/

    MK19_40MM(List.of(Ammunition.HE_40MM)),

    /*===========================================================*/

    /*==================== MACHINE GUNS ====================*/

    BROWNING_50CAL(List.of(Ammunition.NATO_127x99MM)),

    MG3_762MM(List.of(Ammunition.MG3_762x51MM)),
    PKT_762MM(List.of(Ammunition.PKT_762x54MM));

    /*======================================================*/

    /**
     *
     */
    private final List<Ammunition> compatibleAmmunition;
}
