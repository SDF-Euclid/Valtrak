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
    DRAW_CARDS,
    /** Chosen vehicles can't be targeted (and can't attack) until their owner's next turn. */
    SMOKE_SCREEN,
    /** Attached to a strike group: enemy reveal abilities can't target it; costs Fuel each turn. */
    JAMMER,
    /** Attached to a vehicle: retreating costs less Fuel. */
    CAMOUFLAGE,
    /** The opponent discards cards at random. */
    SABOTAGE,
    /** Return resource cards from your discard pile to your hand. */
    RECYCLE,
    /** Deploy vehicles from your hand into a group without the formation cost. */
    RAPID_DEPLOYMENT
}
