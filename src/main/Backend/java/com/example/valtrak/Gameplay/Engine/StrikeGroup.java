package com.example.valtrak.Gameplay.Engine;

import java.util.ArrayList;
import java.util.List;

/** A strike group. The Leader is always {@code vehicles.get(0)}. Plain data holder. */
public class StrikeGroup {
    public long id;
    public List<Vehicle> vehicles = new ArrayList<>();
    public List<ResourceStack> pool = new ArrayList<>();
    /** True once a second vehicle has joined (the formation cost has been paid). */
    public boolean formed;
    /** Resource cards the convoy has moved into the pool this turn. */
    public int convoyMoved;
    /**
     * The group's Jammer (not a vehicle, and it takes no slot): card id (0 = none), its own id so it can be attacked, its HP,
     * and whether it is switched on. A Jammer that is on can be attacked; destroying it reveals the whole group.
     */
    public long jammerCardId;
    public long jammerId;
    public int jammerHp;
    public int jammerMaxHp;
    public boolean jammerOn;

    public StrikeGroup() {}

    public StrikeGroup(long id) {
        this.id = id;
    }

    /** True while this group's Jammer is switched on: enemy reveal abilities can't target its vehicles. */
    public boolean jammed() {
        return jammerCardId != 0 && jammerOn;
    }

    public Vehicle leader() {
        return vehicles.get(0);
    }

    public StrikeGroup copy() {
        StrikeGroup g = new StrikeGroup(id);
        vehicles.forEach(v -> g.vehicles.add(v.copy()));
        pool.forEach(r -> g.pool.add(r.copy()));
        g.formed = formed;
        g.convoyMoved = convoyMoved;
        g.jammerCardId = jammerCardId;
        g.jammerId = jammerId;
        g.jammerHp = jammerHp;
        g.jammerMaxHp = jammerMaxHp;
        g.jammerOn = jammerOn;
        return g;
    }
}
