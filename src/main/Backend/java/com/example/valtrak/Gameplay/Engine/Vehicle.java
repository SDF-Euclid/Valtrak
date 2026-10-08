package com.example.valtrak.Gameplay.Engine;

/** A vehicle on the field. Plain data holder: the card data lives in the {@link CardCatalog}. */
public class Vehicle {
    public long id;
    public long cardId;
    public int hp;
    public int maxHp;
    public int breachStacks;
    public boolean faceUp;
    public boolean stunned;
    public boolean suppressed;
    public boolean disabled;
    /** True once this vehicle has used its ability this turn. */
    public boolean abilityUsed;
    /** Card id of the ERA attached to this vehicle, or 0 for none. */
    public long eraCardId;

    public Vehicle() {}

    public Vehicle(long id, long cardId, int maxHp) {
        this.id = id;
        this.cardId = cardId;
        this.hp = maxHp;
        this.maxHp = maxHp;
    }

    public Vehicle copy() {
        Vehicle v = new Vehicle(id, cardId, maxHp);
        v.hp = hp;
        v.breachStacks = breachStacks;
        v.faceUp = faceUp;
        v.stunned = stunned;
        v.suppressed = suppressed;
        v.disabled = disabled;
        v.abilityUsed = abilityUsed;
        v.eraCardId = eraCardId;
        return v;
    }
}
