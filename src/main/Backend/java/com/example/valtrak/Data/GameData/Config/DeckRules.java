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
    /** The most copies any card can have (a Common or Uncommon one). */
    public static final int MAX_COPIES = 3;
    public static final int MAX_SAVED_DECKS = 20;
    public static final int MAX_NAME_LENGTH = 30;

    private DeckRules() {}

    /** Copies of one card a deck may hold: Common/Uncommon 3, Rare/Epic 2, Legendary/Commander 1. */
    public static int maxCopies(com.example.valtrak.Data.CardLibrary.CardLevel level) {
        return level == null ? MAX_COPIES : RULES.maxCopies(level);
    }

    /** The same, for a rarity name as the server sends it ("RARE"). */
    public static int maxCopies(String levelName) {
        if (levelName == null) return MAX_COPIES;
        try {
            return maxCopies(com.example.valtrak.Data.CardLibrary.CardLevel.valueOf(levelName));
        } catch (IllegalArgumentException e) {
            return MAX_COPIES;
        }
    }
}
