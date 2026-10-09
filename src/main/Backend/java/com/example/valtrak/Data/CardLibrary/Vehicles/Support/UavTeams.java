package com.example.valtrak.Data.CardLibrary.Vehicles.Support;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.AbilityType;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.GroundVehicleCardInterface;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.VehicleAttackInterface;
import com.example.valtrak.Data.GameData.Config.Records.VehicleAbilityDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

//TODO: Review UAV stats and names (placeholders)

/**
 * UAV teams: Specialist vehicles that fill a strike group's Specialist slot. They don't attack. Instead, while face up,
 * they can turn enemy vehicles face up (the higher the rarity, the more vehicles). Using one costs Fuel from the group's pool.
 */
@Getter
@AllArgsConstructor
public enum UavTeams implements GroundVehicleCardInterface {

    RQ_11_RAVEN("RQ-11 Raven Team", "United States",
            "A hand-launched drone team that spots enemy positions.",
            CardLevel.COMMON, VehicleType.AIR, VehicleClass.SPECIALIST, 5, 30,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    ORLAN_10("Orlan-10 Team", "Russia",
            "A long-endurance reconnaissance drone team.",
            CardLevel.UNCOMMON, VehicleType.AIR, VehicleClass.SPECIALIST, 5, 35,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    LUNA_NG("LUNA NG Team", "Germany",
            "A tactical reconnaissance drone team.",
            CardLevel.UNCOMMON, VehicleType.AIR, VehicleClass.SPECIALIST, 5, 35,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    MQ_1C_GRAY_EAGLE("MQ-1C Gray Eagle Flight", "United States",
            "A medium-altitude drone flight that can watch several positions at once.",
            CardLevel.RARE, VehicleType.AIR, VehicleClass.SPECIALIST, 10, 60,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    FORPOST_R("Forpost-R Flight", "Russia",
            "A medium-altitude drone flight with a wide sensor sweep.",
            CardLevel.EPIC, VehicleType.AIR, VehicleClass.SPECIALIST, 10, 65,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    HERON_TP("Heron TP Flight", "Germany",
            "A high-endurance drone flight with a wide sensor sweep.",
            CardLevel.EPIC, VehicleType.AIR, VehicleClass.SPECIALIST, 10, 65,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    MQ_9_REAPER("MQ-9 Reaper Flight", "United States",
            "A high-altitude drone flight that leaves nowhere to hide.",
            CardLevel.LEGENDARY, VehicleType.AIR, VehicleClass.SPECIALIST, 15, 90,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 3, 1)),

    /*==================== ADDED IN THE ROSTER UPDATE ====================*/

    FFRS_TEAM("FFRS Team", "Japan",
            "A helicopter-drone team that scouts ahead of Japanese armor.",
            CardLevel.UNCOMMON, VehicleType.AIR, VehicleClass.SPECIALIST, 5, 35,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    CH4_RAINBOW("CH-4 Rainbow Flight", "China",
            "A long-range Chinese reconnaissance drone flight.",
            CardLevel.EPIC, VehicleType.AIR, VehicleClass.SPECIALIST, 10, 65,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    WATCHKEEPER("Watchkeeper WK450 Flight", "United Kingdom",
            "A British all-weather surveillance drone flight.",
            CardLevel.RARE, VehicleType.AIR, VehicleClass.SPECIALIST, 10, 60,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    PATROLLER("Patroller Flight", "France",
            "A French long-endurance tactical drone flight.",
            CardLevel.RARE, VehicleType.AIR, VehicleClass.SPECIALIST, 10, 60,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    HERMES_900("Hermes 900 Flight", "Israel",
            "A large Israeli surveillance drone flight with a powerful sensor suite.",
            CardLevel.LEGENDARY, VehicleType.AIR, VehicleClass.SPECIALIST, 15, 90,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 3, 1)),

    ORNEN("UAV 03 Ornen Team", "Sweden",
            "A Swedish tactical reconnaissance drone team.",
            CardLevel.UNCOMMON, VehicleType.AIR, VehicleClass.SPECIALIST, 5, 35,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1)),

    FALCO("Falco Flight", "Italy",
            "An Italian tactical surveillance drone flight.",
            CardLevel.RARE, VehicleType.AIR, VehicleClass.SPECIALIST, 10, 60,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 2, 1)),

    SONGGOLMAE("RQ-101 Songgolmae Team", "South Korea",
            "A Korean corps-level reconnaissance drone team.",
            CardLevel.COMMON, VehicleType.AIR, VehicleClass.SPECIALIST, 5, 30,
            List.of(), new VehicleAbilityDefinition(AbilityType.REVEAL_ENEMY, 1, 1));

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
