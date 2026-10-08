package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;

import java.util.List;
import java.util.Optional;

public record VehicleSpec(long cardId, String name, CardLevel level, VehicleClass vehicleClass,
                          int hp, int armor, List<AttackSpec> attacks, AbilitySpec ability, boolean air) implements CardSpec {

    /** A ground vehicle with an ability (or none). */
    public VehicleSpec(long cardId, String name, CardLevel level, VehicleClass vehicleClass,
                       int hp, int armor, List<AttackSpec> attacks, AbilitySpec ability) {
        this(cardId, name, level, vehicleClass, hp, armor, attacks, ability, false);
    }

    /** A vehicle without an ability. */
    public VehicleSpec(long cardId, String name, CardLevel level, VehicleClass vehicleClass,
                       int hp, int armor, List<AttackSpec> attacks) {
        this(cardId, name, level, vehicleClass, hp, armor, attacks, null, false);
    }


    /** Only tanks can lead a group or stand alone as a group of one. */
    public boolean isTank() {
        return switch (vehicleClass) {
            case LIGHT_TANK, MEDIUM_TANK, HEAVY_TANK, MAIN_BATTLE_TANK -> true;
            default -> false;
        };
    }

    public boolean isSpecialist() { return vehicleClass == VehicleClass.SPECIALIST; }

    public boolean isResupply() { return vehicleClass == VehicleClass.SUPPLY; }

    public Optional<AttackSpec> attack(AttackSlot slot) {
        return attacks.stream().filter(a -> a.slot() == slot).findFirst();
    }
}
