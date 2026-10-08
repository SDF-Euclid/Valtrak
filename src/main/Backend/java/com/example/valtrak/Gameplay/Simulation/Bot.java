package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Gameplay.Engine.Action;
import com.example.valtrak.Gameplay.Engine.GameEngine;
import com.example.valtrak.Gameplay.Engine.GameState;

import java.util.random.RandomGenerator;

/** A computer player: looks at the game and picks the next action. Called again after every action. */
public interface Bot {
    String name();

    /** Must return an action the engine will accept. {@code EndTurn} is always allowed during your turn. */
    Action choose(GameEngine engine, GameState state, int player, RandomGenerator rng);
}
