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
     * All tanks (3 copies each); 2 copies of as many different Ammo cards as fit; the 5x and 10x Fuel cards;
     * the 1x and 3x Supply cards; and the two smallest Repair cards (3 copies each, 3 copies each of the rest)
     * - topped up with more Ammo until the deck has {@code size} cards.
     */
    public static List<Long> standard(CardCatalog catalog, Iterable<CardSpec> all, int size, RandomGenerator rng) {
        List<Long> deck = new ArrayList<>();
        List<ResourceSpec> ammo = new ArrayList<>();
        for (CardSpec spec : all) {
            if (spec instanceof VehicleSpec v && v.isTank()) {
                addCopies(deck, spec.cardId(), 3);
            } else if (spec instanceof ResourceSpec r) {
                switch (r.kind()) {
                    case AMMO -> ammo.add(r);
                    case FUEL -> { if (r.amount() == 5 || r.amount() == 10) addCopies(deck, r.cardId(), 3); }
                    case SUPPLY -> { if (r.amount() <= 3) addCopies(deck, r.cardId(), 3); }
                    case REPAIR -> { if (r.amount() <= 75) addCopies(deck, r.cardId(), 3); }
                }
            }
        }
        Collections.shuffle(ammo, rng);
        for (ResourceSpec r : ammo) {
            if (deck.size() >= size) break;
            addCopies(deck, r.cardId(), Math.min(2, size - deck.size()));
        }
        for (int pass = 0; deck.size() < size && pass < 10; pass++) {
            for (ResourceSpec r : ammo) {
                if (deck.size() >= size) break;
                if (Collections.frequency(deck, r.cardId()) < 3) deck.add(r.cardId());
            }
        }
        return deck;
    }

    private static void addCopies(List<Long> deck, long id, int copies) {
        for (int i = 0; i < copies; i++) deck.add(id);
    }
}
