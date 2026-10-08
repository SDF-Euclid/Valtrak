package com.example.valtrak.Gameplay.Engine;

import java.util.ArrayList;
import java.util.List;

/** What happened as a result of an action, in plain words (for the game log). */
public class ActionResult {
    public final List<String> log = new ArrayList<>();

    void say(String message) {
        log.add(message);
    }
}
