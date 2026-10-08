package com.example.valtrak.UI.net;

import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.ActionRequest;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.AttackChoiceRequest;

import java.util.List;

/** Builds the move requests the match API understands (see docs/API.md). */
public final class Moves {
    private Moves() {}

    private static ActionRequest r(String type, Long cardId, Long groupId, Long vehicleId, List<Long> vehicleIds,
                                   Long resourceId, List<Long> resourceIds, Long toGroupId,
                                   List<AttackChoiceRequest> choices, List<Long> cardIds) {
        return new ActionRequest(type, cardId, groupId, vehicleId, vehicleIds, resourceId, resourceIds, toGroupId, choices, cardIds);
    }

    public static ActionRequest placeStartingTank(long cardId) { return r("PLACE_STARTING_TANK", cardId, null, null, null, null, null, null, null, null); }
    public static ActionRequest designate(long cardId, Long groupId) { return r("DESIGNATE", cardId, groupId, null, null, null, null, null, null, null); }
    public static ActionRequest deploy(long cardId, Long groupId) { return r("DEPLOY", cardId, groupId, null, null, null, null, null, null, null); }
    public static ActionRequest convoy(long groupId, List<Long> resourceIds) { return r("CONVOY", null, groupId, null, null, null, resourceIds, null, null, null); }
    public static ActionRequest repair(long resourceId, long vehicleId) { return r("REPAIR", null, null, vehicleId, null, resourceId, null, null, null, null); }
    public static ActionRequest reveal(long vehicleId) { return r("REVEAL", null, null, null, List.of(vehicleId), null, null, null, null, null); }
    public static ActionRequest revealGroup(long groupId) { return r("REVEAL_GROUP", null, groupId, null, null, null, null, null, null, null); }
    public static ActionRequest retreat(long vehicleId) { return r("RETREAT", null, null, vehicleId, null, null, null, null, null, null); }
    public static ActionRequest retreatGroup(long groupId) { return r("RETREAT_GROUP", null, groupId, null, null, null, null, null, null, null); }
    public static ActionRequest move(long vehicleId, Long toGroupId) { return r("MOVE", null, null, vehicleId, null, null, null, toGroupId, null, null); }
    public static ActionRequest useAbility(long vehicleId, List<Long> targets) { return r("USE_ABILITY", null, null, vehicleId, targets, null, null, null, null, null); }
    public static ActionRequest jammer(long groupId, boolean on) { return r(on ? "JAMMER_ON" : "JAMMER_OFF", null, groupId, null, null, null, null, null, null, null); }
    public static ActionRequest attack(long groupId, List<AttackChoiceRequest> choices) { return r("ATTACK", null, groupId, null, null, null, null, null, choices, null); }
    public static ActionRequest endTurn() { return r("END_TURN", null, null, null, null, null, null, null, null, null); }

    /** Play an item: {@code vehicleIds} are the target vehicles, {@code groupId} the target group, {@code cardIds} the cards chosen (Search, Recycle, Rapid Deployment). */
    public static ActionRequest playItem(long cardId, List<Long> vehicleIds, Long groupId, List<Long> cardIds) {
        return r("PLAY_ITEM", cardId, groupId, null, vehicleIds, null, null, null, null, cardIds);
    }
}
