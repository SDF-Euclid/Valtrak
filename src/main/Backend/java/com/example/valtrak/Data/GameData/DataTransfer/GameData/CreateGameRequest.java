package com.example.valtrak.Data.GameData.DataTransfer.GameData;

import java.util.List;

public record CreateGameRequest(
        Long player1Id,
        Long player2Id,
        List<Long> player1DeckCardIds,
        List<Long> player2DeckCardIds
) {}
