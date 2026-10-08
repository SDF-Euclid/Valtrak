package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;

/** The damage-type vs armor-bracket table: a damage multiplier and an automatic special effect. */
public final class DamageMatchups {
    private DamageMatchups() {}

    public static double modifier(DamageType type, ArmorBracket bracket) {
        return switch (type) {
            case KINETIC -> switch (bracket) {
                case UNARMORED -> 0.6;
                case LIGHT -> 0.8;
                case MEDIUM -> 1.0;
                case HEAVY -> 1.3;
                case SUPER_HEAVY -> 1.5;
            };
            case CHEMICAL -> 1.0;
            case EXPLOSIVE -> switch (bracket) {
                case UNARMORED -> 1.8;
                case LIGHT -> 1.4;
                case MEDIUM -> 0.7;
                case HEAVY -> 0.4;
                case SUPER_HEAVY -> 0.2;
            };
            case ELECTRIC -> 0.0;
        };
    }

    public static SpecialEffect autoEffect(DamageType type, ArmorBracket bracket) {
        return switch (type) {
            case EXPLOSIVE -> (bracket == ArmorBracket.UNARMORED || bracket == ArmorBracket.LIGHT)
                    ? SpecialEffect.STUN : SpecialEffect.NONE;
            case ELECTRIC -> SpecialEffect.DISABLE;
            default -> SpecialEffect.NONE;
        };
    }
}
