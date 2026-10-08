package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;

/**
 * A resource card. {@code amount} is how much it holds when played (a "5x" crate = 5, a Repair card = HP restored).
 * {@code ammunition} is only set for AMMO.
 */
public record ResourceSpec(long cardId, String name, CardLevel level, ResourceKind kind,
                           Ammunition ammunition, int amount) implements CardSpec {}
