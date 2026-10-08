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
    /** Card id of the Jammer attached to this group, or 0 for none. */
    public long jammerCardId;

    public StrikeGroup() {}

    public StrikeGroup(long id) {
        this.id = id;
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
        return g;
    }
}
