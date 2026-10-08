package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Gameplay.Engine.GameRules;

/**
 * Deck limits shared by the server and the deck builder. The numbers come from the engine's
 * {@link GameRules} so there is one place to change them.
 */
public final class DeckRules {
    private static final GameRules RULES = GameRules.defaults();

    public static final int MAX_DECK_SIZE = RULES.maxDeckSize;
    /** A deck below this size can be saved as a draft but can't be taken into a game. */
    public static final int MIN_PLAYABLE_DECK_SIZE = RULES.minDeckSize;
    public static final int MIN_TANKS = RULES.minTanksInDeck;
    public static final int MAX_COPIES = RULES.maxCopies;
    public static final int MAX_SAVED_DECKS = 20;
    public static final int MAX_NAME_LENGTH = 30;

    private DeckRules() {}
}
