package com.example.valtrak.CardData.Information.SupplyInfo;

import com.example.valtrak.CardData.Information.WeaponInfo.Ammunition;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum AmmoSupplyCrate {

    /*==================== 1x SUPPLY CRATE ====================*/

    APFSDS_120MM_X1(Ammunition.APFSDS_120MM, 1),
    APFSDS_125MM_X1(Ammunition.APFSDS_125MM, 1),

    HEAT_120MM_X1(Ammunition.HEAT_125MM, 1),
    HEAT_125MM_X1(Ammunition.HEAT_125MM, 1),

    /*=========================================================*/

    /*==================== 5x SUPPLY CRATE ====================*/

    APFSDS_120MM_X5(Ammunition.APFSDS_120MM, 5),
    APFSDS_125MM_X5(Ammunition.APFSDS_125MM, 5),

    HEAT_120MM_X5(Ammunition.HEAT_125MM, 5),
    HEAT_125MM_X5(Ammunition.HEAT_125MM, 5),

    /*=========================================================*/

    /*==================== 10x SUPPLY CRATE ====================*/

    APFSDS_120MM_X10(Ammunition.APFSDS_120MM, 10),
    APFSDS_125MM_X10(Ammunition.APFSDS_125MM, 10),

    HEAT_120MM_X10(Ammunition.HEAT_125MM, 10),
    HEAT_125MM_X10(Ammunition.HEAT_125MM, 10);

    /*==========================================================*/

    /**
     *
     */
    private final Ammunition ammunition;
    private final Integer count;
}