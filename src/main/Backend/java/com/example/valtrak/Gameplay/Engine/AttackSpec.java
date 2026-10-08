package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon;

/** One attack on a vehicle card. The costs are spent from the strike group's pool when it is used. */
public record AttackSpec(AttackSlot slot, String name, Weapon weapon, int baseDamage,
                         int ammoCost, int fuelCost, SpecialEffect effect) {}
