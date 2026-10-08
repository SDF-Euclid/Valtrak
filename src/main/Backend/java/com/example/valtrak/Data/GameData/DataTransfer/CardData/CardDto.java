package com.example.valtrak.Data.GameData.DataTransfer.CardData;

import java.util.List;

/**
 * Flat, display-ready view of a card sent from the server to clients.
 * Fields that don't apply to a card's category are null.
 *
 * @param category VEHICLE, AMMUNITION, FUEL, SUPPLY, REPAIR, ITEM (ERA, Artillery, Search, Draw) or OTHER
 * @param ability  a short description of the vehicle's ability or the item's effect, or null
 */
public record CardDto(
        Long id,
        String name,
        String description,
        String level,
        String category,
        String nation,
        String vehicleClass,
        Integer hp,
        Integer armor,
        String damageType,
        String ammunition,
        String itemType,
        Integer count,
        Integer repairAmount,
        String ability,
        List<AttackDto> attacks,
        Integer abilityPower,
        Integer abilityFuelCost,
        Integer effectPrimary,
        Integer effectSecondary
) {

    /** A card without attacks or numbers (everything except vehicles and special items). */
    public CardDto(Long id, String name, String description, String level, String category, String nation,
                   String vehicleClass, Integer hp, Integer armor, String damageType, String ammunition,
                   String itemType, Integer count, Integer repairAmount, String ability) {
        this(id, name, description, level, category, nation, vehicleClass, hp, armor, damageType, ammunition,
                itemType, count, repairAmount, ability, null, null, null, null, null);
    }

    /** One attack of a vehicle: {@code ammo} lists the ammunition types its weapon can fire. */
    public record AttackDto(String slot, String name, int baseDamage, int ammoCost, int fuelCost, List<String> ammo, String effect) {}
}
