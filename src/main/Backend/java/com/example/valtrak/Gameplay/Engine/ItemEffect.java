package com.example.valtrak.Gameplay.Engine;

/** What an item card does when it is played. */
public enum ItemEffect {
    /** Attach to one of your vehicles: it takes less CHEMICAL damage until it is destroyed. */
    ERA,
    /** True damage to enemy vehicles. */
    ARTILLERY,
    /** Look through your deck for cards of one kind. */
    SEARCH,
    /** Draw cards. */
    DRAW,
    /** Chosen vehicles can't be targeted (and can't attack) until the start of your next turn. */
    SMOKE,
    /** Attach to a strike group: enemy reveal abilities can't target it, and it costs Fuel each turn. */
    JAMMER,
    /** Attach to a vehicle: retreating it costs less Fuel. */
    CAMO,
    /** The opponent discards cards at random. */
    SABOTAGE,
    /** Return resource cards from your discard pile to your hand. */
    RECYCLE,
    /** Deploy vehicles from your hand into a group without the formation cost. */
    RAPID_DEPLOY
}
