package com.example.valtrak.CardData.Information.WeaponInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum PrimaryWeapon {

    SMOOTHBORE_CANNON_120MM(List.of(Ammunition.APFSDS_120MM, Ammunition.HEAT_120MM));

    /**
     *
     */
    private final List<Ammunition> compatibleAmmunition;
}