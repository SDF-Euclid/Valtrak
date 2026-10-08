package com.example.valtrak.Gameplay.Simulation;

/** What happened in one simulated game. */
public record GameReport(int winner, String endedBy, int turns, int firstPlayer, int[] chips, int[] attacks,
                         int groupsDestroyed, int firstAttackTurn, int itemsPlayed) {

    public boolean firstPlayerWon() {
        return winner == firstPlayer;
    }
}
