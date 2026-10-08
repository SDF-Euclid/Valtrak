package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.*;
import com.example.valtrak.Data.GameData.Entity.MatchRecord;
import com.example.valtrak.Gameplay.Engine.*;

import java.util.ArrayList;
import java.util.Comparator;
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
                isViewer ? p.deck.stream().sorted().toList() : null,        // you know your deck list; only its order is hidden
                List.copyOf(p.discard),                                   // the discard pile is public
                p.depot.stream().map(GameViewBuilder::resource).toList(),  // so is the Depot
                p.groups.stream().map(g -> group(g, isViewer)).toList(),
                p.chips, rules.baseGroupLimit + p.chips / rules.chipsPerExtraGroup,
                isViewer ? p.designationsLeft : 0, p.placedStartingTank);
    }

    private static GroupView group(StrikeGroup g, boolean isViewer) {
        return new GroupView(g.id, g.formed, isViewer ? g.convoyMoved : 0,
                (isViewer ? g.vehicles : hideOrder(g.vehicles)).stream().map(v -> vehicle(v, isViewer)).toList(),
                g.pool.stream().map(GameViewBuilder::resource).toList(),
                jammer(g, isViewer));
    }

    /**
     * The first vehicle in a group is its Leader, so an opponent must not see the real order: face-up vehicles keep their order,
     * then the face-down ones follow in a fixed scrambled order that says nothing about which is the Leader.
     */
    private static List<Vehicle> hideOrder(List<Vehicle> vehicles) {
        List<Vehicle> out = new ArrayList<>(vehicles.stream().filter(v -> v.faceUp).toList());
        out.addAll(vehicles.stream().filter(v -> !v.faceUp)
                .sorted(Comparator.comparingLong((Vehicle v) -> v.id * 0x9E3779B97F4A7C15L ^ (v.id >>> 7))).toList());
        return out;
    }

    /** Your own vehicles are shown in full. An enemy vehicle is only shown in full while it is face up. */
    private static VehicleView vehicle(Vehicle v, boolean isViewer) {
        if (!isViewer && !v.faceUp) {
            return new VehicleView(v.id, false, null, null, null, null, null, null, null, null, null, null, v.smoked, v.hiddenHits.size());
        }
        return new VehicleView(v.id, v.faceUp, v.cardId, v.hp, v.maxHp, v.breachStacks, v.stunned, v.suppressed, v.disabled, v.abilityUsed,
                v.eraCardId == 0 ? null : v.eraCardId, v.camoCardId == 0 ? null : v.camoCardId, v.smoked, v.hiddenHits.size());
    }

    /** You see your own Jammer always; an opponent only sees one that is switched on. */
    private static JammerView jammer(StrikeGroup g, boolean isViewer) {
        if (g.jammerCardId == 0 || (!isViewer && !g.jammerOn)) return null;
        return new JammerView(g.jammerId, g.jammerCardId, g.jammerHp, g.jammerMaxHp, g.jammerOn);
    }

    private static ResourceView resource(ResourceStack r) {
        return new ResourceView(r.id, r.cardId, r.kind.name(), r.ammunition == null ? null : r.ammunition.name(), r.remaining);
    }
}
