package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Every tunable number in the rulebook, in one place. Change a value here (or on a
 * copy used by a test or simulation) and the engine follows; nothing else hard-codes them.
 * The values mirror the bracketed numbers in docs/RULEBOOK.md.
 */
public class GameRules {

    // goal and decks
    public int winChips = 3;
    public int minDeckSize = 60;
    public int maxDeckSize = 100;
    public int maxCopies = 3;
    public int minTanksInDeck = 12;

    // setup
    public int startingHandSize = 7;
    public int mulliganExtraDrawCap = 3;

    // losing: at the start of your turn (after you draw) with no strike group on the field and no tank in your hand to start one
    public boolean loseWithNoForces = true;

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

    // damage scale: every attack's (and Artillery's) damage is multiplied by this percent. 400 with 3 chips gives about 13-15 turns
    // per player (docs/SIMULATION.md); 100 = the numbers on the cards
    public int damagePercent = 400;

    // resources
    public int designationsPerTurn = 1;
    public int fullRepairThreshold = 999;

    // costs by vehicle rarity
    public Map<CardLevel, Integer> retreatFuel = byLevel(1, 1, 2, 2, 3, 3);
    public Map<CardLevel, Integer> moveFuel = byLevel(1, 1, 2, 2, 3, 3);
    public Map<CardLevel, Integer> formationSupply = byLevel(1, 1, 2, 2, 3, 3);
    public Map<CardLevel, Integer> convoyCapacity = byLevel(1, 1, 2, 2, 3, 3);
    public int groupRetreatMultiplier = 2;

    // item cards: Supply to play one, by rarity (all 0 = items are free, which is the rulebook), and the rarity from which
    // Artillery may also choose face-down vehicles
    public Map<CardLevel, Integer> itemSupply = byLevel(0, 0, 0, 0, 0, 0);
    public CardLevel artilleryBlindFrom = CardLevel.LEGENDARY;
    // what kind of damage Artillery does: null = true damage (ignores armor); otherwise that damage type, worked out with the normal
    // armor rules using artilleryCaliber. EXPLOSIVE with caliber 100: full damage to unarmored and light vehicles (and it stuns them),
    // but heavily reduced against main battle tanks (armor above about 66), so tanks are hard to kill with Artillery.
    public DamageType artilleryDamageType = DamageType.EXPLOSIVE;
    public int artilleryCaliber = 100;
    // how many cards of a limited kind a player may play per turn (kinds not listed have no limit)
    public Map<ItemEffect, Integer> itemLimitPerTurn = new EnumMap<>(Map.of(ItemEffect.ARTILLERY, 1, ItemEffect.SABOTAGE, 1, ItemEffect.AIRDROP, 1));

    public static GameRules defaults() {
        return new GameRules();
    }

    public int retreatFuel(CardLevel level) { return retreatFuel.get(level); }

    public int moveFuel(CardLevel level) { return moveFuel.get(level); }

    public int formationSupply(CardLevel level) { return formationSupply.get(level); }

    public int convoyCapacity(CardLevel level) { return convoyCapacity.get(level); }

    /** 0 = no limit. */
    public int itemLimit(ItemEffect effect) { return itemLimitPerTurn.getOrDefault(effect, 0); }

    public int itemSupply(CardLevel level) { return itemSupply.get(level); }

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
