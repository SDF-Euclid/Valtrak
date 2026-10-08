package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.AbilityType;

/**
 * A vehicle's activated ability: usable once per turn while the vehicle is face up, paid for with Fuel from its group's pool.
 * For REVEAL_ENEMY, {@code power} is how many face-down enemy vehicles it turns face up.
 */
public record AbilitySpec(AbilityType type, int power, int fuelCost) {}
