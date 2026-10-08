package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Gameplay.Engine.*;

import java.util.List;
import java.util.Random;

/** Plays whole games between two bots. */
public final class GameSimulator {
    private GameSimulator() {}

    public static GameReport play(GameEngine engine, List<Long> deck0, List<Long> deck1, Bot bot0, Bot bot1,
                                  long seed, int maxActions) {
        Random rng = new Random(seed);
        GameState s = engine.newGame(deck0, deck1, rng);
        int[] attacks = new int[2];
        int destroyed = 0;
        int firstAttackTurn = -1;
        int actions = 0;
        while (s.phase != GameState.Phase.FINISHED && actions++ < maxActions) {
            int player = s.phase == GameState.Phase.SETUP ? (s.player(0).placedStartingTank ? 1 : 0) : s.activePlayer;
            Bot bot = player == 0 ? bot0 : bot1;
            Action action = bot.choose(engine, s, player, rng);
            ActionResult result;
            try {
                result = engine.apply(s, player, action);
            } catch (RuleViolationException e) {
                throw new IllegalStateException(bot.name() + " chose an illegal action " + action + ": " + e.getMessage(), e);
            }
            if (action instanceof Action.Attack) {
                attacks[player]++;
                if (firstAttackTurn < 0) firstAttackTurn = s.turnCount;
            }
            destroyed += (int) result.log.stream().filter(line -> line.contains("strike group is destroyed")).count();
        }
        int[] chips = {s.player(0).chips, s.player(1).chips};
        if (s.phase != GameState.Phase.FINISHED) {
            return new GameReport(-1, "LIMIT", s.turnCount, s.firstPlayer, chips, attacks, destroyed, firstAttackTurn);
        }
        String endedBy = s.endReason != null && s.endReason.contains("no card to draw") ? "DECK_OUT" : "CHIPS";
        return new GameReport(s.winner, endedBy, s.turnCount, s.firstPlayer, chips, attacks, destroyed, firstAttackTurn);
    }
}
