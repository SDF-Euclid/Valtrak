package com.example.valtrak.Data.GameData.Config.Records;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.*;
import com.example.valtrak.Data.CardLibrary.Interfaces.VehicleAttackInterface;

public record VehicleAttackDefinition(
        String attackName,
        AttackSlot attackSlot,
        Weapon weapon,
        Integer baseDamage,
        Integer ammoCost,
        Integer fuelCost,
        SpecialEffect specialEffect
) implements VehicleAttackInterface {
    public String getAttackName() { return attackName; }
    public AttackSlot getAttackSlot() { return attackSlot; }
    public Weapon getWeapon() { return weapon; }
    public Integer getBaseDamage() { return baseDamage; }
    public Integer getAmmoCost() { return ammoCost; }
    public Integer getFuelCost() { return fuelCost; }
    public SpecialEffect getSpecialEffect() { return specialEffect; }
}