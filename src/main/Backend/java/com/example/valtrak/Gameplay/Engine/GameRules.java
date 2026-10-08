package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;

import java.util.EnumMap;
import java.util.Map;

/**
 * Every tunable number in the rulebook, in one place. Change a value here (or on a
 * copy used by a test or simulation) and the engine follows; nothing else hard-codes them.
 * The values mirror the bracketed numbers in docs/RULEBOOK.md.
 */
public class GameRules {

    // goal and decks
    public int winChips = 5;
    public int minDeckSize = 60;
    public int maxDeckSize = 100;
    public int maxCopies = 3;
    public int minTanksInDeck = 12;

    // setup
    public int startingHandSize = 7;
    public int mulliganExtraDrawCap = 3;

    // strike groups
    public int baseGroupLimit = 3;
    public int chipsPerExtraGroup = 2;
    public int maxGroupSize = 5;
    public int maxLineVehicles = 2;
    public int bonusChipMinVehicles = 4;
    public int bonusChips = 1;

    // optional experiment (0 = off, which is the rulebook): after this many rounds in a row where neither player
    // attacks, every vehicle except Resupply vehicles is turned face up
    public int stalemateRounds = 0;

    // optional experiment (100 = the numbers on the cards): scales every attack's damage
    public int damagePercent = 100;

    // resources
    public int designationsPerTurn = 1;
    public int fullRepairThreshold = 999;

    // costs by vehicle rarity
    public Map<CardLevel, Integer> retreatFuel = byLevel(1, 1, 2, 2, 3, 3);
    public Map<CardLevel, Integer> moveFuel = byLevel(1, 1, 2, 2, 3, 3);
    public Map<CardLevel, Integer> formationSupply = byLevel(1, 1, 2, 2, 3, 3);
    public Map<CardLevel, Integer> convoyCapacity = byLevel(1, 1, 2, 2, 3, 3);
    public int groupRetreatMultiplier = 2;

    public static GameRules defaults() {
        return new GameRules();
    }

    public int retreatFuel(CardLevel level) { return retreatFuel.get(level); }

    public int moveFuel(CardLevel level) { return moveFuel.get(level); }

    public int formationSupply(CardLevel level) { return formationSupply.get(level); }

    public int convoyCapacity(CardLevel level) { return convoyCapacity.get(level); }

    /** Values in the order COMMON, UNCOMMON, RARE, EPIC, LEGENDARY, COMMANDER. */
    private static Map<CardLevel, Integer> byLevel(int common, int uncommon, int rare, int epic, int legendary, int commander) {
        Map<CardLevel, Integer> m = new EnumMap<>(CardLevel.class);
        m.put(CardLevel.COMMON, common);
        m.put(CardLevel.UNCOMMON, uncommon);
        m.put(CardLevel.RARE, rare);
        m.put(CardLevel.EPIC, epic);
        m.put(CardLevel.LEGENDARY, legendary);
        m.put(CardLevel.COMMANDER, commander);
        return m;
    }
}
