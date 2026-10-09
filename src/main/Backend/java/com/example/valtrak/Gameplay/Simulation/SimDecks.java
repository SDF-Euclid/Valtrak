package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Gameplay.Engine.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;

/** Builds legal decks from whatever cards the catalog has, for simulations. */
public final class SimDecks {
    private SimDecks() {}

    /**
     * All tanks (3 copies each); {@code scouts} copies of every UAV team and Recon vehicle; one 5x or 10x Ammo card (2 copies) for every weapon the tanks carry, so every attack can
     * be paid for; the 5x and 10x Fuel cards; the 1x and 3x Supply cards; the small Repair cards (3 copies each) - topped up
     * with more Ammo until the deck has {@code size} cards.
     */
    public static List<Long> standard(CardCatalog catalog, Iterable<CardSpec> all, int size, RandomGenerator rng) {
        return standard(catalog, all, size, rng, 1);
    }

    /** @param scouts copies of each UAV team / Recon vehicle to include (0 = none) */
    public static List<Long> standard(CardCatalog catalog, Iterable<CardSpec> all, int size, RandomGenerator rng, int scouts) {
        return standard(catalog, all, size, rng, scouts, 0);
    }

    private static final GameRules LIMITS = GameRules.defaults();

    /** Copies of a card a deck may hold (by rarity), never more than {@code wanted}. */
    private static int copies(CardSpec spec, int wanted) {
        return Math.min(wanted, LIMITS.maxCopies(spec.level()));
    }

    /** Item effects GreedyBot knows how to play; the others would only clog its hand. */
    private static final java.util.Set<ItemEffect> BOT_ITEMS = java.util.EnumSet.of(ItemEffect.ERA, ItemEffect.ARTILLERY,
            ItemEffect.SEARCH, ItemEffect.DRAW, ItemEffect.SABOTAGE, ItemEffect.RECYCLE, ItemEffect.RAPID_DEPLOY);

    /**
     * @param items copies (up to 3) of each item card the bots can play, using at most a quarter of the deck. They replace filler
     *              Ammo, never the Ammo each weapon needs.
     */
    public static List<Long> standard(CardCatalog catalog, Iterable<CardSpec> all, int size, RandomGenerator rng, int scouts, int items) {
        List<Long> itemCards = new ArrayList<>();
        for (CardSpec spec : all) {
            if (spec instanceof ItemSpec i && BOT_ITEMS.contains(i.effect())) addCopies(itemCards, spec.cardId(), copies(spec, items));
        }
        while (itemCards.size() > size / 4) itemCards.remove(itemCards.size() - 1);
        List<Long> deck = new ArrayList<>(standardWithoutItems(catalog, all, size - itemCards.size(), rng, Math.min(3, scouts)));
        deck.addAll(itemCards);
        return deck;
    }

    /**
     * In order of importance, so a small deck loses the least important cards: tanks, Ammo for every weapon, Fuel, Supply,
     * Repair, UAV/Recon vehicles, then more Ammo up to the size.
     */
    private static List<Long> standardWithoutItems(CardCatalog catalog, Iterable<CardSpec> all, int size, RandomGenerator rng, int scouts) {
        List<Long> tanks = new ArrayList<>(), fuel = new ArrayList<>(), supply = new ArrayList<>(), repair = new ArrayList<>(), scoutCards = new ArrayList<>();
        List<ResourceSpec> ammo = new ArrayList<>();
        java.util.Set<com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon> weapons =
                java.util.EnumSet.noneOf(com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon.class);
        for (CardSpec spec : all) {
            if (spec instanceof VehicleSpec v && v.isTank()) {
                addCopies(tanks, spec.cardId(), copies(spec, 3));
                v.attacks().forEach(a -> weapons.add(a.weapon()));
            } else if (spec instanceof VehicleSpec v && v.ability() != null) {
                addCopies(scoutCards, spec.cardId(), copies(spec, scouts));       // UAV teams and Recon vehicles
                if (scouts > 0) v.attacks().forEach(a -> weapons.add(a.weapon()));
            } else if (spec instanceof ResourceSpec r) {
                switch (r.kind()) {
                    case AMMO -> { if (r.amount() == 5 || r.amount() == 10) ammo.add(r); }
                    case FUEL -> { if (r.amount() == 5 || r.amount() == 10) addCopies(fuel, r.cardId(), copies(r, 3)); }
                    case SUPPLY -> { if (r.amount() <= 3) addCopies(supply, r.cardId(), copies(r, 3)); }
                    case REPAIR -> { if (r.amount() <= 75) addCopies(repair, r.cardId(), copies(r, 2)); }
                }
            }
        }
        Collections.shuffle(ammo, rng);
        List<Long> deck = new ArrayList<>(tanks);
        for (var weapon : weapons) {                                   // Ammo for every weapon, so every attack can be paid for
            ammo.stream().filter(r -> weapon.getCompatibleAmmunition().contains(r.ammunition())).findFirst()
                    .ifPresent(r -> { if (!deck.contains(r.cardId())) addCopies(deck, r.cardId(), copies(r, 2)); });
        }
        deck.addAll(fuel);
        deck.addAll(supply);
        deck.addAll(repair);
        deck.addAll(scoutCards);
        for (int pass = 0; deck.size() < size && pass < 10; pass++) {  // then more Ammo
            for (ResourceSpec r : ammo) {
                if (deck.size() >= size) break;
                if (Collections.frequency(deck, r.cardId()) < copies(r, 3)) deck.add(r.cardId());
            }
        }
        while (deck.size() > size) deck.remove(deck.size() - 1);     // a small deck: drop the least important cards
        return deck;
    }

    private static void addCopies(List<Long> deck, long id, int copies) {
        for (int i = 0; i < copies; i++) deck.add(id);
    }
}
