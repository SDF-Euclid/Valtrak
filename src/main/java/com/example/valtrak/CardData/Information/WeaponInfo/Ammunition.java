package com.example.valtrak.CardData.Information.WeaponInfo;

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

    APFSDS_25MM(DamageType.KINETIC, 20),

    APFSDS_120MM(DamageType.KINETIC, 50),
    APFSDS_125MM(DamageType.KINETIC, 50),

    /*=================================================*/

    /*==================== CHEMICAL ====================*/

    HEAT_120MM(DamageType.CHEMICAL, 40),
    HEAT_125MM(DamageType.CHEMICAL, 40),

    BGM_71_152MM(DamageType.CHEMICAL, 75),

    /*==================================================*/

    /*==================== EXPLOSIVE ====================*/

    HE_25MM(DamageType.EXPLOSIVE, 30),

    /*===================================================*/

    /*==================== ELECTRIC ====================*/



    /*==================================================*/

    /*===========================================================================================================*/



    /*======================================== SECONDARY WEAPON AMMUNITION ========================================*/

    /*==================== SECONDARY WEAPON AMMO ====================*/

    NATO_127x99MM(DamageType.KINETIC, 10);

    /*===============================================================*/

    /*=============================================================================================================*/

    private final DamageType damageType;
    private final Integer damageAmount;
}