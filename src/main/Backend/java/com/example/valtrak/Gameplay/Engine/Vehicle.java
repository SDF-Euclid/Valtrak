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
    /** Resource cards this (Resupply) vehicle has moved by convoy this turn: the limit belongs to the vehicle, not the group. */
    public int convoyMoved;
    /** Card id of the ERA attached to this vehicle, or 0 for none. */
    public long eraCardId;
    /** Card id of the Camouflage attached to this vehicle, or 0 for none. */
    public long camoCardId;
    /**
     * Artillery that landed on this vehicle while it was face down: the base damage of each hit. Nobody knows what it does
     * until the vehicle is turned face up (an aircraft takes nothing; anything else takes the damage its armor allows).
     */
    public java.util.List<Integer> hiddenHits = new java.util.ArrayList<>();
    /** Under a Smoke Screen: can't be targeted and can't attack, until its owner's next turn starts. */
    public boolean smoked;

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
        v.convoyMoved = convoyMoved;
        v.eraCardId = eraCardId;
        v.camoCardId = camoCardId;
        v.smoked = smoked;
        v.hiddenHits.addAll(hiddenHits);
        return v;
    }
}
