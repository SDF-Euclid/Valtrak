package com.example.valtrak.CardData.Information.WeaponInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;


@Getter
@AllArgsConstructor
public enum SecondaryWeapon {
    BROWNING_50CAL(List.of(Ammunition.NATO_127x99MM));


    private final List<Ammunition> compatibleAmmunition;
}
