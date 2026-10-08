package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_2;
import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** ERA, Artillery, Search and Draw (rulebook section 7c): free, any number per turn, never end the turn. */
class ItemCardTest {

    private final TestWorld w = new TestWorld();
    private StrikeGroup enemy;
    private Vehicle enemyLeader;

    @BeforeEach
    void setUp() {
        w.p(0).deck.addAll(List.of(TANK_COMMON, TANK_COMMON, TANK_COMMON, TANK_COMMON));
        w.p(1).deck.addAll(List.of(TANK_COMMON, TANK_COMMON));
        enemy = w.group(1, TANK_RARE_MBT, true);        // 200 HP, armor 85
        enemyLeader = enemy.leader();
    }

    private PlayItem play(long card, Vehicle... targets) {
        return new PlayItem(card, java.util.Arrays.stream(targets).map(t -> t.id).toList(), List.of());
    }

    // ── general ──────────────────────────────────────────────────────────────

    @Test
    void itemsAreFreeAndYouCanPlayAsManyAsYouLikeWithoutEndingTheTurn() {
        w.hand(0, DRAW_1, DRAW_1, DRAW_3);
        w.act(0, new PlayItem(DRAW_1, List.of(), List.of()));
        w.act(0, new PlayItem(DRAW_1, List.of(), List.of()));
        // the deck only has 2 cards left, so the 3-card draw is refused
        assertThatThrownBy(() -> w.act(0, new PlayItem(DRAW_3, List.of(), List.of())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("at least 3");
        assertThat(w.s.activePlayer).isZero();
        assertThat(w.p(0).hand).containsExactlyInAnyOrder(DRAW_3, TANK_COMMON, TANK_COMMON);
        assertThat(w.p(0).discard).containsExactly(DRAW_1, DRAW_1);
    }

    @Test
    void youCannotPlayAnItemThatIsNotInYourHand() {
        assertThatThrownBy(() -> w.act(0, new PlayItem(DRAW_1, List.of(), List.of())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("not in your hand");
    }

    @Test
    void itCannotBePlayedOnTheOpponentsTurn() {
        w.hand(1, DRAW_1);
        assertThatThrownBy(() -> w.act(1, new PlayItem(DRAW_1, List.of(), List.of())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("not your turn");
    }

    @Test
    void anOptionalSupplyCostCanBeSwitchedOn() {
        w.rules.itemSupply.put(CardLevel.LEGENDARY, 3);
        w.hand(0, DRAW_3);
        w.p(0).deck.addAll(List.of(TANK_COMMON, TANK_COMMON));
        assertThatThrownBy(() -> w.act(0, new PlayItem(DRAW_3, List.of(), List.of())))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("3 Supply");
        assertThat(w.p(0).hand).containsExactly(DRAW_3);

        w.depot(0, SUPPLY_3);
        w.act(0, new PlayItem(DRAW_3, List.of(), List.of()));
        assertThat(w.p(0).hand).hasSize(3);
        assertThat(w.p(0).depot).isEmpty();
    }

    // ── draw ─────────────────────────────────────────────────────────────────

    @Test
    void drawGivesTheCardsAndGoesToTheDiscardPile() {
        w.hand(0, DRAW_3);
        w.p(0).deck.add(TANK_COMMON);
        ActionResult r = w.act(0, new PlayItem(DRAW_3, List.of(), List.of()));
        assertThat(w.p(0).hand).hasSize(3);
        assertThat(w.p(0).deck).hasSize(2);
        assertThat(w.p(0).discard).containsExactly(DRAW_3);
        assertThat(r.log).anyMatch(l -> l.contains("draws 3"));
    }

    // ── ERA ──────────────────────────────────────────────────────────────────

    @Test
    void eraAttachesToOneOfYourVehiclesAndStaysOnIt() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        w.hand(0, ERA_20);
        w.act(0, play(ERA_20, mine.leader()));
        assertThat(mine.leader().eraCardId).isEqualTo(ERA_20);
        assertThat(w.p(0).hand).isEmpty();
        assertThat(w.p(0).discard).as("ERA stays on the vehicle").isEmpty();
    }

    @Test
    void eraNeedsExactlyOneOfYourOwnVehicles() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        w.hand(0, ERA_20);
        assertThatThrownBy(() -> w.act(0, new PlayItem(ERA_20, List.of(), List.of()))).isInstanceOf(RuleViolationException.class);
        assertThatThrownBy(() -> w.act(0, play(ERA_20, enemyLeader))).isInstanceOf(RuleViolationException.class);
        assertThatThrownBy(() -> w.act(0, play(ERA_20, mine.leader(), mine.leader()))).isInstanceOf(RuleViolationException.class);
        assertThat(w.p(0).hand).containsExactly(ERA_20);
    }

    @Test
    void aNewEraReplacesTheOldOneAndTheOldGoesToTheDiscardPile() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        w.hand(0, ERA_20, ERA_50);
        w.act(0, play(ERA_20, mine.leader()));
        w.act(0, play(ERA_50, mine.leader()));
        assertThat(mine.leader().eraCardId).isEqualTo(ERA_50);
        assertThat(w.p(0).discard).containsExactly(ERA_20);
    }

    private int hpLostFromHit(long eraCard, Ammunition ammo, long ammoCard) {
        TestWorld t = new TestWorld();
        t.p(0).deck.add(TANK_COMMON);
        t.p(1).deck.add(TANK_COMMON);
        StrikeGroup mine = t.group(0, TANK_RARE_MBT, true);
        t.pool(mine, ammoCard);
        t.pool(mine, FUEL_5);
        StrikeGroup theirs = t.group(1, TANK_RARE, true);
        Vehicle target = theirs.leader();
        if (eraCard != 0) target.eraCardId = eraCard;
        t.act(0, skirmish(mine, mine.leader(), ATTACK_2, ammo, target));
        return target.maxHp - target.hp;
    }

    @Test
    void eraCutsChemicalDamageByItsPercentageButNotOtherDamage() {
        int plain = hpLostFromHit(0, Ammunition.HEAT_120MM, HEAT_5);
        int light = hpLostFromHit(ERA_20, Ammunition.HEAT_120MM, HEAT_5);
        int advanced = hpLostFromHit(ERA_50, Ammunition.HEAT_120MM, HEAT_5);
        assertThat(light).isEqualTo(Math.round(plain * 0.8f));
        assertThat(advanced).isEqualTo(Math.round(plain * 0.5f));

        int kineticPlain = hpLostFromHit(0, Ammunition.APFSDS_120MM, APFSDS_5);
        assertThat(hpLostFromHit(ERA_50, Ammunition.APFSDS_120MM, APFSDS_5)).as("kinetic damage is not reduced").isEqualTo(kineticPlain);
    }

    @Test
    void eraGoesToTheDiscardPileWhenItsVehicleIsDestroyed() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        Vehicle line = w.add(mine, ANTI_AIR, true);
        line.eraCardId = ERA_20;
        line.hp = 1;
        w.hand(1, ARTILLERY_1);
        w.s.activePlayer = 1;
        w.p(1).deck.add(TANK_COMMON);
        w.act(1, play(ARTILLERY_1, line));
        assertThat(mine.vehicles).doesNotContain(line);
        assertThat(w.p(0).discard).contains(ANTI_AIR, ERA_20);
    }

    @Test
    void eraComesBackToYourHandWithAVehicleThatReturnsThere() {
        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        Vehicle recon = w.add(mine, RECON, false);
        recon.eraCardId = ERA_50;
        mine.leader().hp = 1;
        w.hand(1, ARTILLERY_1);
        w.s.activePlayer = 1;
        w.p(1).deck.add(TANK_COMMON);
        w.act(1, play(ARTILLERY_1, mine.leader()));
        assertThat(w.p(0).hand).containsExactlyInAnyOrder(RECON, ERA_50);
    }

    // ── artillery ────────────────────────────────────────────────────────────

    @Test
    void artilleryDoesTrueDamageAndIgnoresArmorAndDoesNotEndTheTurn() {
        w.hand(0, ARTILLERY_1);
        ActionResult r = w.act(0, play(ARTILLERY_1, enemyLeader));
        assertThat(enemyLeader.hp).isEqualTo(200 - 20);               // armor 85 changes nothing
        assertThat(w.s.activePlayer).isZero();
        assertThat(w.p(0).discard).containsExactly(ARTILLERY_1);
        assertThat(r.log).anyMatch(l -> l.contains("20") && l.contains("true damage"));
    }

    @Test
    void artilleryOnlyHitsFaceUpVehiclesBelowLegendary() {
        Vehicle hidden = w.add(enemy, ANTI_AIR, false);
        w.hand(0, ARTILLERY_1);
        assertThatThrownBy(() -> w.act(0, play(ARTILLERY_1, hidden)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("face-up");
        assertThat(hidden.hp).isEqualTo(hidden.maxHp);
        assertThat(w.p(0).hand).containsExactly(ARTILLERY_1);
    }

    @Test
    void legendaryArtilleryAlsoHitsFaceDownVehiclesAndTurnsThemFaceUp() {
        Vehicle hidden = w.add(enemy, ANTI_AIR, false);
        w.hand(0, ARTILLERY_BLIND);
        w.act(0, play(ARTILLERY_BLIND, hidden));
        assertThat(hidden.hp).isEqualTo(80 - 40);
        assertThat(hidden.faceUp).isTrue();
    }

    @Test
    void artilleryHitsAsManyTargetsAsItsRarityAllowsAndNoMore() {
        Vehicle a = w.add(enemy, ANTI_AIR, true);
        Vehicle b = w.add(enemy, RECON, true);
        w.hand(0, ARTILLERY_2, ARTILLERY_2);
        assertThatThrownBy(() -> w.act(0, play(ARTILLERY_2, enemyLeader, a, b)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("at most 2");
        assertThatThrownBy(() -> w.act(0, play(ARTILLERY_2, a, a)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("only be chosen once");
        assertThatThrownBy(() -> w.act(0, new PlayItem(ARTILLERY_2, List.of(), List.of())))
                .isInstanceOf(RuleViolationException.class);
        w.act(0, play(ARTILLERY_2, a, b));
        assertThat(a.hp).isEqualTo(80 - 30);
        assertThat(b.hp).isEqualTo(70 - 30);
        assertThat(enemyLeader.hp).isEqualTo(200);
    }

    @Test
    void killingALeaderWithArtilleryTakesTheChipAndBreaksUpTheGroup() {
        Vehicle line = w.add(enemy, ANTI_AIR, true);
        enemyLeader.hp = 20;
        w.hand(0, ARTILLERY_1);
        w.act(0, play(ARTILLERY_1, enemyLeader));
        assertThat(w.p(0).chips).isEqualTo(1);
        assertThat(w.p(1).groups).doesNotContain(enemy);
        assertThat(w.p(1).hand).contains(ANTI_AIR);                    // a non-tank returns to its owner's hand
        assertThat(line.hp).isEqualTo(80);
    }

    @Test
    void aLaterTargetThatHasLeftTheFieldIsSkipped() {
        Vehicle line = w.add(enemy, ANTI_AIR, true);
        enemyLeader.hp = 20;
        w.hand(0, ARTILLERY_2);
        w.act(0, play(ARTILLERY_2, enemyLeader, line));                // the Leader dies first; the Line vehicle is back in hand
        assertThat(w.p(0).chips).isEqualTo(1);
        assertThat(line.hp).isEqualTo(80);
        assertThat(w.p(1).hand).contains(ANTI_AIR);
    }

    @Test
    void artilleryCanWinTheGame() {
        w.p(0).chips = 4;
        enemyLeader.hp = 5;
        w.hand(0, ARTILLERY_1);
        w.act(0, play(ARTILLERY_1, enemyLeader));
        assertThat(w.s.phase).isEqualTo(GameState.Phase.FINISHED);
        assertThat(w.s.winner).isZero();
    }

    @Test
    void artilleryIsLimitedToOnePerTurnAndTheLimitResetsEachTurn() {
        w.hand(0, ARTILLERY_1, ARTILLERY_1);
        w.act(0, play(ARTILLERY_1, enemyLeader));
        assertThatThrownBy(() -> w.act(0, play(ARTILLERY_1, enemyLeader)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("only play 1 Artillery card per turn");
        assertThat(enemyLeader.hp).isEqualTo(180);
        assertThat(w.p(0).hand).containsExactly(ARTILLERY_1);

        w.act(0, new EndTurn());
        w.act(1, new EndTurn());
        w.act(0, play(ARTILLERY_1, enemyLeader));        // a new turn: allowed again
        assertThat(enemyLeader.hp).isEqualTo(160);
    }

    @Test
    void otherItemsHaveNoLimit() {
        w.hand(0, DRAW_1, DRAW_1);
        w.act(0, new PlayItem(DRAW_1, List.of(), List.of()));
        w.act(0, new PlayItem(DRAW_1, List.of(), List.of()));
        assertThat(w.p(0).discard).containsExactly(DRAW_1, DRAW_1);
    }

    // ── search ───────────────────────────────────────────────────────────────

    @Test
    void searchTakesTheChosenCardsOfTheRightKindAndShufflesTheDeck() {
        w.p(0).deck.clear();
        w.p(0).deck.addAll(List.of(TANK_COMMON, FUEL_5, SUPPLY_1, APFSDS_5, UAV, TANK_RARE, FUEL_5, HEAT_5, NATO_10, SUPPLY_3));
        w.hand(0, SEARCH_RESOURCES_2);
        long seedBefore = w.s.rngSeed;
        ActionResult r = w.act(0, new PlayItem(SEARCH_RESOURCES_2, List.of(), List.of(APFSDS_5, FUEL_5)));
        assertThat(w.p(0).hand).containsExactlyInAnyOrder(APFSDS_5, FUEL_5);
        assertThat(w.p(0).deck).hasSize(8).containsExactlyInAnyOrder(TANK_COMMON, SUPPLY_1, UAV, TANK_RARE, FUEL_5, HEAT_5, NATO_10, SUPPLY_3);
        assertThat(w.p(0).discard).containsExactly(SEARCH_RESOURCES_2);
        assertThat(w.s.rngSeed).as("the shuffle uses up the game's seed").isNotEqualTo(seedBefore);
        assertThat(r.log).anyMatch(l -> l.contains("5x Sabot") && l.contains("5x Fuel"));
        assertThat(w.s.activePlayer).isZero();
    }

    @Test
    void searchMayFindFewerCardsThanItsLimit() {
        w.hand(0, SEARCH_TANK_1);
        w.act(0, new PlayItem(SEARCH_TANK_1, List.of(), List.of()));
        assertThat(w.p(0).hand).isEmpty();
        assertThat(w.p(0).discard).containsExactly(SEARCH_TANK_1);
    }

    @Test
    void searchCanOnlyTakeTheKindItNamesAndOnlyCardsThatAreInTheDeck() {
        w.p(0).deck.clear();
        w.p(0).deck.addAll(List.of(TANK_COMMON, UAV, FUEL_5, RESUPPLY));
        w.hand(0, SEARCH_TANK_1, SEARCH_SUPPORT_2);
        assertThatThrownBy(() -> w.act(0, new PlayItem(SEARCH_TANK_1, List.of(), List.of(UAV))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("only finds tanks");
        assertThatThrownBy(() -> w.act(0, new PlayItem(SEARCH_SUPPORT_2, List.of(), List.of(TANK_COMMON))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("support vehicles");
        assertThatThrownBy(() -> w.act(0, new PlayItem(SEARCH_SUPPORT_2, List.of(), List.of(UAV, UAV))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("that many copies");
        assertThatThrownBy(() -> w.act(0, new PlayItem(SEARCH_SUPPORT_2, List.of(), List.of(UAV, RESUPPLY, SPECIALIST))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("at most 2");
        assertThat(w.p(0).deck).hasSize(4);
        assertThat(w.p(0).hand).containsExactlyInAnyOrder(SEARCH_TANK_1, SEARCH_SUPPORT_2);
        w.act(0, new PlayItem(SEARCH_SUPPORT_2, List.of(), List.of(UAV, RESUPPLY)));
        assertThat(w.p(0).hand).containsExactlyInAnyOrder(SEARCH_TANK_1, UAV, RESUPPLY);
    }

    // ── the engine plumbing ──────────────────────────────────────────────────

    @Test
    void legalActionsOfferTheItemsInYourHand() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        w.hand(0, DRAW_1, ERA_20, ARTILLERY_1, SEARCH_TANK_1);
        w.p(0).deck.add(TANK_RARE);
        List<Action> legal = w.engine.legalActions(w.s, 0);
        assertThat(legal).contains(new PlayItem(DRAW_1, List.of(), List.of()));
        assertThat(legal).contains(new PlayItem(ERA_20, List.of(mine.leader().id), List.of()));
        assertThat(legal).contains(new PlayItem(ARTILLERY_1, List.of(enemyLeader.id), List.of()));
        assertThat(legal).contains(new PlayItem(SEARCH_TANK_1, List.of(), List.of(TANK_COMMON)));
    }

    @Test
    void theGameWithEraAndASearchSeedSavesAndLoadsAsJson() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        mine.leader().eraCardId = ERA_50;
        w.s.rngSeed = 123456789012345L;
        JsonMapper json = JsonMapper.builder().build();
        GameState loaded = json.readValue(json.writeValueAsString(w.s), GameState.class);
        assertThat(loaded.player(0).groups.get(0).leader().eraCardId).isEqualTo(ERA_50);
        assertThat(loaded.rngSeed).isEqualTo(123456789012345L);
        assertThat(w.s.copy().rngSeed).isEqualTo(123456789012345L);
        assertThat(w.s.copy().player(0).groups.get(0).leader().eraCardId).isEqualTo(ERA_50);
    }
}
