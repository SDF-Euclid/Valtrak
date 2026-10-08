package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.*;
import com.example.valtrak.Data.GameData.Entity.MatchRecord;
import com.example.valtrak.Gameplay.Engine.*;

import java.util.List;

/**
 * Turns the full game state into what one player is allowed to see.
 * This is the only place that decides what is hidden, so it is also the place to check for leaks.
 */
public final class GameViewBuilder {
    private GameViewBuilder() {}

    public static GameView build(MatchRecord match, GameState s, int viewer, GameRules rules) {
        boolean yourTurn = switch (s.phase) {
            case SETUP -> !s.player(viewer).placedStartingTank;
            case PLAYING -> s.activePlayer == viewer;
            case FINISHED -> false;
        };
        return new GameView(match.getId(), match.getStatus().name(), s.phase.name(), match.getVersion(), viewer,
                s.activePlayer, yourTurn, s.turnCount, s.winner, s.endReason, rules.winChips,
                playerView(match, s, viewer, true, rules),
                playerView(match, s, 1 - viewer, false, rules));
    }

    private static PlayerView playerView(MatchRecord match, GameState s, int index, boolean isViewer, GameRules rules) {
        PlayerState p = s.player(index);
        var who = match.player(index);
        return new PlayerView(who.getId(), who.getDisplayName(), who.getDisplayNation(),
                p.deck.size(), p.hand.size(),
                isViewer ? List.copyOf(p.hand) : null,
                List.copyOf(p.discard),                                   // the discard pile is public
                p.depot.stream().map(GameViewBuilder::resource).toList(),  // so is the Depot
                p.groups.stream().map(g -> group(g, isViewer)).toList(),
                p.chips, rules.baseGroupLimit + p.chips / rules.chipsPerExtraGroup,
                isViewer ? p.designationsLeft : 0, p.placedStartingTank);
    }

    private static GroupView group(StrikeGroup g, boolean isViewer) {
        return new GroupView(g.id, g.formed, isViewer ? g.convoyMoved : 0,
                g.vehicles.stream().map(v -> vehicle(v, isViewer)).toList(),
                g.pool.stream().map(GameViewBuilder::resource).toList());
    }

    /** Your own vehicles are shown in full. An enemy vehicle is only shown in full while it is face up. */
    private static VehicleView vehicle(Vehicle v, boolean isViewer) {
        if (!isViewer && !v.faceUp) {
            return new VehicleView(v.id, false, null, null, null, null, null, null, null);
        }
        return new VehicleView(v.id, v.faceUp, v.cardId, v.hp, v.maxHp, v.breachStacks, v.stunned, v.suppressed, v.disabled);
    }

    private static ResourceView resource(ResourceStack r) {
        return new ResourceView(r.id, r.cardId, r.kind.name(), r.ammunition == null ? null : r.ammunition.name(), r.remaining);
    }
}
