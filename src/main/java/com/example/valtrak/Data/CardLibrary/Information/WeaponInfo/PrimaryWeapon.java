package com.example.valtrak.Data.CardLibrary.Information.WeaponInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum PrimaryWeapon {

    /*==================== SMOOTHBORE CANNONS ====================*/

    SMOOTHBORE_CANNON_120MM(List.of(Ammunition.APFSDS_120MM, Ammunition.HEAT_120MM)),

    /*============================================================*/

    /*==================== AUTO CANNONS ====================*/

    BUSHMASTER_25MM(List.of(Ammunition.APFSDS_25MM, Ammunition.HE_25MM)),

    /*======================================================*/

    /*==================== ATGM LAUNCHERS ====================*/

    TOW_ATGM(List.of(Ammunition.BGM_71_152MM));

    /*========================================================*/

    /**
     *
     */
    private final List<Ammunition> compatibleAmmunition;
}