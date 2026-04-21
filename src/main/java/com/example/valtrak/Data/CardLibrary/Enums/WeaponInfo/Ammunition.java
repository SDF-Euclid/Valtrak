package com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;

//TODO: Review damage amounts

/**
 *
 */
@Getter
@AllArgsConstructor
public enum Ammunition {

    /*======================================== PRIMARY WEAPON AMMUNITION ========================================*/

    /*==================== KINETIC ====================*/

    APDS_105MM(DamageType.KINETIC, 105),

    APFSDS_25MM(DamageType.KINETIC, 25),
    APFSDS_105MM(DamageType.KINETIC, 105),
    APFSDS_120MM(DamageType.KINETIC, 120),
    APFSDS_125MM(DamageType.KINETIC, 125),

    /*=================================================*/

    /*==================== CHEMICAL ====================*/

    HEAT_120MM(DamageType.CHEMICAL, 120),
    HEAT_125MM(DamageType.CHEMICAL, 125),

    BGM_71_152MM(DamageType.CHEMICAL, 152),

    /*==================================================*/

    /*==================== EXPLOSIVE ====================*/

    HE_40MM(DamageType.EXPLOSIVE, 40),

    HE_25MM(DamageType.EXPLOSIVE, 25),
    HE_120MM(DamageType.EXPLOSIVE, 120),

    SQUASH_HEAD_105MM(DamageType.EXPLOSIVE, 105),

    /*===================================================*/

    /*==================== ELECTRIC ====================*/



    /*==================================================*/

    /*===========================================================================================================*/



    /*======================================== SECONDARY WEAPON AMMUNITION ========================================*/

    NATO_127x99MM(DamageType.KINETIC, 12),

    MG3_762x51MM(DamageType.KINETIC, 8),
    PKT_762x54MM(DamageType.KINETIC, 8);

    /*=============================================================================================================*/

    private final DamageType damageType;
    private final Integer caliber;
}