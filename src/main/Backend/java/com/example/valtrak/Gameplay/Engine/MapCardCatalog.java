package com.example.valtrak.Gameplay.Engine;

import java.util.HashMap;
import java.util.Map;

/** A catalog backed by a map. */
public class MapCardCatalog implements CardCatalog {
    private final Map<Long, CardSpec> cards = new HashMap<>();

    public MapCardCatalog add(CardSpec spec) {
        cards.put(spec.cardId(), spec);
        return this;
    }

    @Override
    public CardSpec find(long cardId) {
        return cards.get(cardId);
    }
}
