package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import org.springframework.stereotype.Component;

@Component
public class ArmorBracketHelper {

    public static final double BREACH_MULTIPLIER = 0.88;
    public static final int BREACH_MAX_STACKS = 3;
    public static final double ARMOR_REDUCTION_RATE = 0.15;
    public static final double KINETIC_PIERCE_THRESHOLD = 0.8;
    public static final double KINETIC_BREACH_THRESHOLD = 3.0;
    public static final double EXPLOSIVE_OVERPRESSURE_THRESHOLD = 1.5;
    public static final double CHEMICAL_OVERPRESSURE_THRESHOLD = 3.0;

    public static ArmorBracket getBracket(int armor) {
        if (armor <= 30) return ArmorBracket.UNARMORED;
        if (armor <= 70) return ArmorBracket.LIGHT;
        if (armor <= 120) return ArmorBracket.MEDIUM;
        if (armor <= 180) return ArmorBracket.HEAVY;
        return ArmorBracket.SUPER_HEAVY;
    }

    public static int getEffectiveArmor(int baseArmor, int breachStacks) {
        double effective = baseArmor;
        for (int i = 0; i < breachStacks; i++) {
            effective *= BREACH_MULTIPLIER;
        }
        return (int) Math.round(effective);
    }

    public static boolean isOverpressure(DamageType dmgType, ArmorBracket bracket,
                                         int caliber, int targetArmor) {
        if (dmgType.getMaxOverpressureBracket() == null) return false;
        boolean bracketValid = bracket.ordinal()
                <= dmgType.getMaxOverpressureBracket().ordinal();
        double threshold = dmgType == DamageType.EXPLOSIVE
                ? EXPLOSIVE_OVERPRESSURE_THRESHOLD
                : CHEMICAL_OVERPRESSURE_THRESHOLD;
        boolean caliberValid = caliber >= (targetArmor * threshold);
        return bracketValid && caliberValid;
    }

    public static boolean isPierce(DamageType dmgType, ArmorBracket bracket,
                                   int caliber, int targetArmor) {
        return dmgType == DamageType.KINETIC
                && (bracket == ArmorBracket.HEAVY || bracket == ArmorBracket.SUPER_HEAVY)
                && caliber >= (targetArmor * KINETIC_PIERCE_THRESHOLD);
    }

    public static boolean isBreach(DamageType dmgType, ArmorBracket bracket,
                                   int caliber, int targetArmor) {
        return dmgType == DamageType.KINETIC
                && bracket == ArmorBracket.UNARMORED
                && caliber >= (targetArmor * KINETIC_BREACH_THRESHOLD);
    }

    public static boolean isSuppress(DamageType dmgType, ArmorBracket bracket,
                                     int caliber, int targetArmor) {
        return dmgType == DamageType.KINETIC
                && (bracket == ArmorBracket.UNARMORED || bracket == ArmorBracket.LIGHT)
                && caliber < targetArmor;
    }
}
