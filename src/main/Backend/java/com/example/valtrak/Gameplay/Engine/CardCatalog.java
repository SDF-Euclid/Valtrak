package com.example.valtrak.Gameplay.Engine;

/** Looks up card data by card id. The server builds one from the database; tests build one by hand. */
public interface CardCatalog {

    /** @return the card, or null if the id is unknown */
    CardSpec find(long cardId);

    default CardSpec spec(long cardId) {
        CardSpec spec = find(cardId);
        if (spec == null) throw new RuleViolationException("Unknown card: " + cardId);
        return spec;
    }

    default VehicleSpec vehicle(long cardId) {
        if (spec(cardId) instanceof VehicleSpec v) return v;
        throw new RuleViolationException("Card " + cardId + " is not a vehicle.");
    }

    default ItemSpec item(long cardId) {
        if (spec(cardId) instanceof ItemSpec i) return i;
        throw new RuleViolationException("Card " + cardId + " is not an item card.");
    }

    default ResourceSpec resource(long cardId) {
        if (spec(cardId) instanceof ResourceSpec r) return r;
        throw new RuleViolationException("Card " + cardId + " is not a resource.");
    }
}
