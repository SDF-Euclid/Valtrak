package com.example.valtrak.Data.CardLibrary.Vehicles.Support;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.VehicleAttackInterface;
import com.example.valtrak.Data.GameData.Config.Records.VehicleAbilityDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

//TODO: Review Resupply stats and names (placeholders)

/**
 * Resupply vehicles: fill a strike group's Resupply slot. They never attack and have no ability. Their job is the convoy:
 * once per turn they move Ammo and Fuel cards from the Depot into the group's pool (up to 2 cards for a Rare vehicle,
 * 3 for a Legendary one; the numbers are in {@code GameRules.convoyCapacity}). A Resupply vehicle never has to be revealed,
 * but a face-up one can be attacked.
 */
@Getter
@AllArgsConstructor
public enum ResupplyVehicles implements GroundVehicleCardInterface {

    M977_HEMTT("M977 HEMTT Supply Truck", "United States",
            "A heavy tactical truck that keeps a strike group fed with shells and fuel.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    M1075_PLS("M1075 Palletized Load System", "United States",
            "An armored heavy resupply truck that can move whole pallets of ammunition under fire.",
            CardLevel.LEGENDARY, VehicleType.GROUND, VehicleClass.SUPPLY, 40, 200, List.of(), null),

    URAL_4320("Ural-4320 Supply Truck", "Russia",
            "A rugged all-terrain truck that carries a strike group's ammunition and fuel.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    KAMAZ_5350_ARMORED("KamAZ-5350 Armored Convoy Truck", "Russia",
            "An armored convoy lead that keeps supplies moving through contested ground.",
            CardLevel.LEGENDARY, VehicleType.GROUND, VehicleClass.SUPPLY, 40, 200, List.of(), null),

    MAN_SX_8X8("MAN SX 8x8 Supply Truck", "Germany",
            "A high-mobility cargo truck built for fast, long-range resupply.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    HX_ARMORED_LOGISTICS("Rheinmetall HX Armored Logistics Truck", "Germany",
            "An armored logistics vehicle with a protected cab and a modular cargo bed.",
            CardLevel.LEGENDARY, VehicleType.GROUND, VehicleClass.SUPPLY, 40, 200, List.of(), null),

    /*==================== ADDED IN THE ROSTER UPDATE ====================*/

    TYPE_73_TRUCK("Type 73 Heavy Truck", "Japan",
            "A Japanese heavy cargo truck that keeps a strike group supplied.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    SHAANXI_SX2190("Shaanxi SX2190 Truck", "China",
            "A rugged Chinese military cargo truck.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    MAN_SV("MAN SV Support Vehicle", "United Kingdom",
            "The British Army's standard logistics truck.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    ARQUUS_ARMIS("Arquus Armis Truck", "France",
            "A French tactical logistics truck.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    NAMER_LOGISTICS("Namer Armored Logistics Carrier", "Israel",
            "A heavily armored carrier that brings supplies through contested ground.",
            CardLevel.LEGENDARY, VehicleType.GROUND, VehicleClass.SUPPLY, 40, 200, List.of(), null),

    SCANIA_SBAT("Scania SBAT 111 Truck", "Sweden",
            "A Swedish all-terrain military truck.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    IVECO_TRAKKER("Iveco Trakker Truck", "Italy",
            "An Italian heavy tactical truck.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null),

    KM500_TRUCK("KM500 Cargo Truck", "South Korea",
            "A Korean military cargo truck.",
            CardLevel.RARE, VehicleType.GROUND, VehicleClass.SUPPLY, 20, 140, List.of(), null);

    private final String vehicleName;
    private final String vehicleNation;
    private final String description;
    private final CardLevel level;
    private final VehicleType vehicleType;
    private final VehicleClass vehicleClass;
    private final Integer vehicleArmor;
    private final Integer vehicleHP;
    private final List<VehicleAttackInterface> vehicleAttacks;
    private final VehicleAbilityDefinition ability;
}
