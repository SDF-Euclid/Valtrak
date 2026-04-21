package com.example.valtrak.Data.GameData.Config.Records;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.*;
import com.example.valtrak.Data.CardLibrary.Interfaces.Vehicle.VehicleAttackInterface;

/**
 * An immutable data record representing the definition of a single attack slot
 * on a vehicle card. Each vehicle can have up to 3 attack slots (ATTACK_1,
 * ATTACK_2, ATTACK_3) with escalating cost and damage.
 * Implements {@link VehicleAttackInterface} so that nation-specific vehicle enums
 * (e.g. USGroundVehicles) can return a list of these definitions without
 * depending on JPA entity classes.
 *
 * @param attackName
 *  The display name of this attack (e.g. "Sabot Barrage")
 * @param attackSlot
 *  The slot this attack occupies (ATTACK_1, ATTACK_2, ATTACK_3)
 * @param weapon
 *  The weapon used for this attack (e.g. SMOOTHBORE_CANNON_120MM)
 * @param baseDamage
 *  The flat base damage before modifiers are applied
 * @param ammoCost
 *  The amount of AMMO resource required to use this attack
 * @param fuelCost
 * The amount of FUEL resource required to use this attack (0 if none)
 * @param specialEffect
 *  The special effect this attack can trigger (e.g. PIERCE, SUPPRESS)
 */
public record VehicleAttackDefinition(
        String attackName,
        AttackSlot attackSlot,
        Weapon weapon,
        Integer baseDamage,
        Integer ammoCost,
        Integer fuelCost,
        SpecialEffect specialEffect
) implements VehicleAttackInterface {

    /**
     * @return The display name of this attack
     */
    public String getAttackName() { return attackName; }

    /**
     * @return The attack slot this definition occupies
     */
    public AttackSlot getAttackSlot() { return attackSlot; }

    /**
     * @return The weapon used for this attack
     */
    public Weapon getWeapon() { return weapon; }

    /**
     * @return The flat base damage value before any modifiers are applied
     */
    public Integer getBaseDamage() { return baseDamage; }

    /**
     * @return The AMMO cost required to use this attack
     */
    public Integer getAmmoCost() { return ammoCost; }

    /**
     * @return The FUEL cost required to use this attack, or 0 if none required
     */
    public Integer getFuelCost() { return fuelCost; }

    /**
     * @return The special effect that may trigger when this attack is used
     */
    public SpecialEffect getSpecialEffect() { return specialEffect; }
}