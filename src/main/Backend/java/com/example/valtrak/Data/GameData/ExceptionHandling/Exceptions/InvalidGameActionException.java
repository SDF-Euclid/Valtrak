package com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions;

public class InvalidGameActionException extends RuntimeException {
    public InvalidGameActionException(String message) {
        super(message);
    }
}
