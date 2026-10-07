package com.example.valtrak.Data.GameData.DataTransfer.GameData;

import com.example.valtrak.Data.GameData.Entity.GameState.StrikeGroup;
import com.example.valtrak.Data.GameData.Enums.GamePhase;
import com.example.valtrak.Data.GameData.Enums.GameStatus;

import java.util.List;
import java.util.Map;

/**
 * A game as one of its players is allowed to see it: your own hand is shown,
 * your opponent's hand and deck order are not (only their sizes).
 */
public record GameView(
        Long id,
        GameStatus status,
        GamePhase phase,
        int turnNumber,
        Long activePlayerId,
        Long winnerId,
        PlayerView you,
        PlayerView opponent
) {
    public record PlayerView(
            Long playerId,
            String displayName,
            String nation,
            int deckSize,
            int handSize,
            List<Long> hand,                 // null for the opponent
            List<Long> discard,
            Map<String, Integer> ammoInventory,
            int fuelPool,
            int supplyPool,
            int repairPool,
            int territoryChips,
            List<StrikeGroup> strikeGroups
    ) {}
}
