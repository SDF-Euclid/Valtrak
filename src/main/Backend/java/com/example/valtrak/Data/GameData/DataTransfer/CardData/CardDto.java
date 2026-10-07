package com.example.valtrak.Data.GameData.DataTransfer.CardData;

/**
 * Flat, display-ready view of a card sent from the server to clients.
 * Fields that don't apply to a card's category are null.
 *
 * @param category VEHICLE, AMMUNITION, FUEL, REPAIR or OTHER
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
        Integer repairAmount
) {}
