package com.example.valtrak.Data.GameData.Config.Records;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.AbilityType;

/**
 * A vehicle's activated ability, usable once per turn while the vehicle is face up.
 *
 * @param type     what it does
 * @param power    how strong it is (for REVEAL_ENEMY: how many enemy vehicles it reveals)
 * @param fuelCost Fuel spent from the group's pool each time it is used
 */
public record VehicleAbilityDefinition(AbilityType type, int power, int fuelCost) {}
