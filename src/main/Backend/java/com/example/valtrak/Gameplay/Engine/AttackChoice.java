package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;

/**
 * One vehicle's part in an attack. {@code ammo} may be null when the weapon only
 * takes one ammunition type.
 */
public record AttackChoice(long vehicleId, AttackSlot slot, Ammunition ammo, long targetVehicleId) {}
