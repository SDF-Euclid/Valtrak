package com.example.valtrak.Data.CardLibrary.Interfaces;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleType;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleAttackEntity;

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
    List<VehicleAttackInterface> getVehicleAttacks();
}