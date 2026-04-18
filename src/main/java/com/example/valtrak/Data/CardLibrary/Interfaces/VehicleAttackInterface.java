package com.example.valtrak.Data.CardLibrary.Interfaces;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.*;

public interface VehicleAttackInterface {
    String getAttackName();
    AttackSlot getAttackSlot();
    Weapon getWeapon();
    Integer getBaseDamage();
    Integer getAmmoCost();
    Integer getFuelCost();
    SpecialEffect getSpecialEffect();
}
