package com.example.valtrak.Gameplay.Engine;

import java.util.ArrayList;
import java.util.List;

/** The whole game. Plain data holder, so it can be saved, copied and sent as JSON. */
public class GameState {

    public enum Phase { SETUP, PLAYING, FINISHED }

    public List<PlayerState> players = new ArrayList<>();
    public int activePlayer;
    public int firstPlayer;
    public Phase phase = Phase.SETUP;
    public int winner = -1;
    public String endReason;
    public long nextId = 1;
    public int turnCount;
    /** Turns in a row that ended without an attack (used by the optional stalemate rule). */
    public int passesInARow;

    public PlayerState player(int index) {
        return players.get(index);
    }

    public GameState copy() {
        GameState s = new GameState();
        players.forEach(p -> s.players.add(p.copy()));
        s.activePlayer = activePlayer;
        s.firstPlayer = firstPlayer;
        s.phase = phase;
        s.winner = winner;
        s.endReason = endReason;
        s.nextId = nextId;
        s.turnCount = turnCount;
        s.passesInARow = passesInARow;
        return s;
    }
}
