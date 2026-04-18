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

    /*==================== AUTO CANNONS ====================*/

    BUSHMASTER_25MM(List.of(Ammunition.APFSDS_25MM, Ammunition.HE_25MM)),

    /*======================================================*/

    /*==================== ATGM LAUNCHERS ====================*/

    TOW_ATGM(List.of(Ammunition.BGM_71_152MM)),

    /*========================================================*/

    /*==================== MACHINE GUNS ====================*/

    BROWNING_50CAL(List.of(Ammunition.NATO_127x99MM));

    /*======================================================*/

    /**
     *
     */
    private final List<Ammunition> compatibleAmmunition;
}
