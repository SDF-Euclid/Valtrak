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
    DRAW
}
