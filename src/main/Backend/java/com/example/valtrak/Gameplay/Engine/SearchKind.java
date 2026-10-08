package com.example.valtrak.Gameplay.Engine;

/** What a Search card can look for. */
public enum SearchKind {
    /** Ammo, Fuel, Supply and Repair cards. */
    RESOURCE,
    /** Tanks. */
    TANK,
    /** Vehicles that are not tanks: Specialists, Resupply, Recon and so on. */
    SUPPORT
}
