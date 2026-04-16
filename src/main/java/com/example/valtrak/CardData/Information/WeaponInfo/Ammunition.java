package com.example.valtrak.CardData.Information.WeaponInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum Ammunition {
    /*====================PRIMARY WEAPON AMMO====================*/

    APFSDS_120MM(DamageType.KINETIC),
    APFSDS_125MM(DamageType.KINETIC),

    HEAT_120MM(DamageType.CHEMICAL),
    HEAT_125MM(DamageType.CHEMICAL),

    /*=============================================================*/

    /*====================SECONDARY WEAPON AMMO====================*/

    NATO_127x99MM(DamageType.KINETIC);

    /*=============================================================*/

    private final DamageType damageType;
}