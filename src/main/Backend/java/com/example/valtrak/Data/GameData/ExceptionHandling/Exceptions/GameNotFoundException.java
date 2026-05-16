package com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(Long gameId) {
        super("Game not found: " + gameId);
    }
}
