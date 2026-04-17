package com.example.valtrak.Data.CardLibrary.Vehicles.Interfaces;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Information.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Information.VehicleInfo.VehicleType;
import com.example.valtrak.Data.CardLibrary.Information.WeaponInfo.PrimaryWeapon;
import com.example.valtrak.Data.CardLibrary.Information.WeaponInfo.SecondaryWeapon;
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
    List<PrimaryWeapon> getPrimaryWeapons();
    List<SecondaryWeapon> getSecondaryWeapons();
}