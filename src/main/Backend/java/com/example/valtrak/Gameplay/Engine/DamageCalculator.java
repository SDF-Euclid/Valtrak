package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.GameData.Config.ArmorBracketHelper;

/**
 * The damage formula: finalDamage = max(1, round(baseDamage * modifier) - round(armor * 0.15)),
 * with pierce, overpressure and breach rules. Pure maths, no database.
 */
public final class DamageCalculator {
    private DamageCalculator() {}

    public record Result(int damage, boolean trueDamage, boolean piercing, SpecialEffect effect) {}

    public static Result calculate(int baseDamage, SpecialEffect slotEffect, Ammunition ammo,
                                   int targetBaseArmor, int targetBreachStacks) {
        int armor = ArmorBracketHelper.getEffectiveArmor(targetBaseArmor, targetBreachStacks);
        ArmorBracket bracket = ArmorBracketHelper.getBracket(armor);
        DamageType type = ammo.getDamageType();
        int caliber = ammo.getCaliber();

        if (ArmorBracketHelper.isOverpressure(type, bracket, caliber, armor)) {
            return new Result(baseDamage, true, false, SpecialEffect.OVERPRESSURE);
        }

        boolean piercing = ArmorBracketHelper.isPierce(type, bracket, caliber, armor);
        double modified = baseDamage * DamageMatchups.modifier(type, bracket);
        int armorReduction = piercing ? 0 : (int) Math.round(armor * ArmorBracketHelper.ARMOR_REDUCTION_RATE);
        int damage = Math.max(1, (int) Math.round(modified) - armorReduction);

        SpecialEffect effect;
        if (ArmorBracketHelper.isBreach(type, bracket, caliber, armor)) effect = SpecialEffect.BREACH;
        else if (piercing) effect = SpecialEffect.PIERCE;
        else if (ArmorBracketHelper.isSuppress(type, bracket, caliber, armor)) effect = SpecialEffect.SUPPRESSION;
        else if (type == DamageType.ELECTRIC) effect = SpecialEffect.DISABLE;
        else {
            SpecialEffect auto = DamageMatchups.autoEffect(type, bracket);
            effect = auto != SpecialEffect.NONE ? auto : slotEffect;
        }
        return new Result(damage, false, piercing, effect);
    }
}
