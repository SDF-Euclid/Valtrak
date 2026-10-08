package com.example.valtrak.Gameplay.Engine;

/** Thrown when a player tries something the rules don't allow. The message is safe to show to the player. */
public class RuleViolationException extends RuntimeException {
    public RuleViolationException(String message) {
        super(message);
    }
}
