package com.example.valtrak.Gameplay.Engine;

import java.util.ArrayList;
import java.util.List;

/** One player's side of the game. Cards in deck, hand and discard are card ids. Plain data holder. */
public class PlayerState {
    public int index;
    public List<Long> deck = new ArrayList<>();      // top of the deck is index 0
    public List<Long> hand = new ArrayList<>();
    public List<Long> discard = new ArrayList<>();
    public List<ResourceStack> depot = new ArrayList<>();
    public List<StrikeGroup> groups = new ArrayList<>();
    public int chips;
    public int designationsLeft;
    public int turnsTaken;
    public int mulligans;
    public boolean placedStartingTank;

    public PlayerState() {}

    public PlayerState(int index) {
        this.index = index;
    }

    public PlayerState copy() {
        PlayerState p = new PlayerState(index);
        p.deck.addAll(deck);
        p.hand.addAll(hand);
        p.discard.addAll(discard);
        depot.forEach(r -> p.depot.add(r.copy()));
        groups.forEach(g -> p.groups.add(g.copy()));
        p.chips = chips;
        p.designationsLeft = designationsLeft;
        p.turnsTaken = turnsTaken;
        p.mulligans = mulligans;
        p.placedStartingTank = placedStartingTank;
        return p;
    }
}
