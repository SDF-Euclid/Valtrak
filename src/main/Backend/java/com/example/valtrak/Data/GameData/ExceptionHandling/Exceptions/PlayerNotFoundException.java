package com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String message) {
        super(message);
    }
}
