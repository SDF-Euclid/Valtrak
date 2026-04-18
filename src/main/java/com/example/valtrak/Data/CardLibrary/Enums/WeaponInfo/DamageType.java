package com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An Enumerated Constant representing the different types of damage that shells do.
 */
@Getter
@AllArgsConstructor
public enum DamageType {
    KINETIC(null, 0),
    CHEMICAL(ArmorBracket.LIGHT, 76),
    EXPLOSIVE(ArmorBracket.MEDIUM, 37),
    ELECTRIC(null, 0);

    private final ArmorBracket maxOverpressureBracket;
    private final Integer minCaliberForOverpressure;
}