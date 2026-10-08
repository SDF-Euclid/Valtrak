package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Gameplay.Engine.Action;
import com.example.valtrak.Gameplay.Engine.GameEngine;
import com.example.valtrak.Gameplay.Engine.GameState;

import java.util.List;
import java.util.random.RandomGenerator;

/** Picks a random legal action, ending its turn about one time in five. A baseline to compare real strategies against. */
public final class RandomBot implements Bot {

    @Override
    public String name() {
        return "random";
    }

    @Override
    public Action choose(GameEngine engine, GameState s, int player, RandomGenerator rng) {
        List<Action> legal = engine.legalActions(s, player);
        List<Action> others = legal.stream().filter(a -> !(a instanceof Action.EndTurn)).toList();
        if (!others.isEmpty() && rng.nextDouble() < 0.8) return others.get(rng.nextInt(others.size()));
        return legal.contains(new Action.EndTurn()) ? new Action.EndTurn() : legal.get(rng.nextInt(legal.size()));
    }
}
