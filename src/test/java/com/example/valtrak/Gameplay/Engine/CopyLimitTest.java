package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.GameData.Config.DeckRules;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;

/** How many copies of one card a deck may hold depends on its rarity: Common/Uncommon 3, Rare/Epic 2, Legendary/Commander 1. */
class CopyLimitTest {

    private final GameEngine engine = new GameEngine(GameRules.defaults(), TestWorld.buildCatalog());

    /** A legal deck: 12 tanks within the limits, padded with Common Fuel... cards up to 60. */
    private List<Long> base() {
        List<Long> deck = new ArrayList<>();
        for (int i = 0; i < 3; i++) deck.add(TANK_COMMON);
        for (int i = 0; i < 3; i++) deck.add(TANK_UNCOMMON);
        for (int i = 0; i < 2; i++) deck.add(TANK_RARE);
        for (int i = 0; i < 2; i++) deck.add(TANK_RARE_MBT);
        for (int i = 0; i < 2; i++) deck.add(TANK_EPIC);
        deck.add(TANK_LEGENDARY);
        deck.add(TANK_COMMANDER);
        long[] pad = {FUEL_1, SUPPLY_1, REPAIR_25, ANTI_AIR, SCOUT, APFSDS_5, HEAT_5, FUEL_5, SPECIALIST, DRAW_1, ERA_20,
                ARTILLERY_1, SEARCH_TANK_1, SMOKE_1, CAMO_1, JAMMER_2, SABOTAGE_1, RECYCLE_1, RAPID_1, AIRDROP_1};
        for (long id : pad) {
            int n = GameRules.defaults().maxCopies(engine.catalog().spec(id).level());
            for (int i = 0; i < n && deck.size() < 60; i++) deck.add(id);
        }
        return deck;
    }

    @Test
    void theDefaultLimitsFollowRarity() {
        GameRules r = GameRules.defaults();
        assertThat(r.maxCopies(CardLevel.COMMON)).isEqualTo(3);
        assertThat(r.maxCopies(CardLevel.UNCOMMON)).isEqualTo(3);
        assertThat(r.maxCopies(CardLevel.RARE)).isEqualTo(2);
        assertThat(r.maxCopies(CardLevel.EPIC)).isEqualTo(2);
        assertThat(r.maxCopies(CardLevel.LEGENDARY)).isEqualTo(1);
        assertThat(r.maxCopies(CardLevel.COMMANDER)).isEqualTo(1);
        assertThat(DeckRules.maxCopies("RARE")).isEqualTo(2);
        assertThat(DeckRules.maxCopies("nonsense")).isEqualTo(3);
    }

    @Test
    void aDeckWithinTheLimitsIsAllowed() {
        List<Long> deck = base();
        assertThat(deck).hasSize(60);
        assertThat(engine.validateDeck(deck)).isEmpty();
    }

    @Test
    void aSecondLegendaryOrAThirdRareIsNot() {
        List<Long> deck = base();
        deck.set(deck.size() - 1, TANK_LEGENDARY);                  // 2 copies of a Legendary
        assertThat(engine.validateDeck(deck)).anyMatch(p -> p.contains("at most 1 copy of MBT Legendary"));

        deck = base();
        deck.set(deck.size() - 1, TANK_RARE);                       // 3 copies of a Rare
        assertThat(engine.validateDeck(deck)).anyMatch(p -> p.contains("at most 2 copies of Medium Rare"));
    }
}
