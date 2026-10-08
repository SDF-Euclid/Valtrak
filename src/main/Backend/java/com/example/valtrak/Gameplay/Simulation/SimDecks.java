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
     * All tanks (3 copies each); one 5x or 10x Ammo card (2 copies) for every weapon the tanks carry, so every attack can
     * be paid for; the 5x and 10x Fuel cards; the 1x and 3x Supply cards; the small Repair cards (3 copies each) - topped up
     * with more Ammo until the deck has {@code size} cards.
     */
    public static List<Long> standard(CardCatalog catalog, Iterable<CardSpec> all, int size, RandomGenerator rng) {
        List<Long> deck = new ArrayList<>();
        List<ResourceSpec> ammo = new ArrayList<>();
        java.util.Set<com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon> weapons =
                java.util.EnumSet.noneOf(com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon.class);
        for (CardSpec spec : all) {
            if (spec instanceof VehicleSpec v && v.isTank()) {
                addCopies(deck, spec.cardId(), 3);
                v.attacks().forEach(a -> weapons.add(a.weapon()));
            } else if (spec instanceof ResourceSpec r) {
                switch (r.kind()) {
                    case AMMO -> { if (r.amount() == 5 || r.amount() == 10) ammo.add(r); }
                    case FUEL -> { if (r.amount() == 5 || r.amount() == 10) addCopies(deck, r.cardId(), 3); }
                    case SUPPLY -> { if (r.amount() <= 3) addCopies(deck, r.cardId(), 3); }
                    case REPAIR -> { if (r.amount() <= 75) addCopies(deck, r.cardId(), 3); }
                }
            }
        }
        Collections.shuffle(ammo, rng);
        // first, ammo for every weapon
        for (var weapon : weapons) {
            ammo.stream().filter(r -> weapon.getCompatibleAmmunition().contains(r.ammunition())).findFirst()
                    .ifPresent(r -> {
                        if (Collections.frequency(deck, r.cardId()) == 0 && deck.size() < size) addCopies(deck, r.cardId(), 2);
                    });
        }
        // then fill up with more ammo
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
