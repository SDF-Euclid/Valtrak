package com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;

import com.example.valtrak.Data.GameData.Config.Records.VehicleAbilityDefinition;
import java.util.List;

/**
 *
 */
public interface GroundVehicleCardInterface {
    String getVehicleName();
    String getVehicleNation();
    String getDescription();
    CardLevel getLevel();
    VehicleType getVehicleType();
    VehicleClass getVehicleClass();
    Integer getVehicleArmor();
    Integer getVehicleHP();
    List<VehicleAttackInterface> getVehicleAttacks();

    /** The vehicle's activated ability, or null if it has none. */
    default VehicleAbilityDefinition getAbility() { return null; }
}