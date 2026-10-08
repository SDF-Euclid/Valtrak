package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;

/** A resource card on the table, with how much is left on it (the "chips" on the card). Plain data holder. */
public class ResourceStack {
    public long id;
    public long cardId;
    public ResourceKind kind;
    public Ammunition ammunition;   // AMMO only
    public int remaining;

    public ResourceStack() {}

    public ResourceStack(long id, long cardId, ResourceKind kind, Ammunition ammunition, int remaining) {
        this.id = id;
        this.cardId = cardId;
        this.kind = kind;
        this.ammunition = ammunition;
        this.remaining = remaining;
    }

    public ResourceStack copy() {
        return new ResourceStack(id, cardId, kind, ammunition, remaining);
    }
}
