package com.example.valtrak.Data.GameData.DataTransfer.DeckData;

import java.time.LocalDateTime;
import java.util.Map;

public final class DeckDtos {
    private DeckDtos() {}

    /** @param playable true if the deck can be taken into a game (60-100 cards, at least 12 tanks) */
    public record DeckDto(Long id, String name, Map<Long, Integer> cardCounts, int totalCards,
                          boolean playable, LocalDateTime updatedAt) {}

    public record SaveDeckRequest(String name, Map<Long, Integer> cardCounts) {}
}
