package com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo;

/** What a special item card does (see docs/RULEBOOK.md section 7c). */
public enum SpecialItemEffect {
    /** Attached to a vehicle: it takes less CHEMICAL damage until it is destroyed. */
    ERA_PROTECTION,
    /** True damage to enemy vehicles. */
    ARTILLERY_STRIKE,
    /** Look through your deck for resource cards. */
    SEARCH_RESOURCES,
    /** Look through your deck for tanks. */
    SEARCH_TANKS,
    /** Look through your deck for support vehicles (Specialists, Resupply, Recon and other non-tanks). */
    SEARCH_SUPPORT,
    /** Draw cards. */
    DRAW_CARDS
}
