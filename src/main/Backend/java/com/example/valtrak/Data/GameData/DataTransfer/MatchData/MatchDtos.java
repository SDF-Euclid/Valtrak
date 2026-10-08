package com.example.valtrak.Data.GameData.DataTransfer.MatchData;

import java.time.LocalDateTime;
import java.util.List;

/** Request and response bodies for the /matches endpoints. */
public final class MatchDtos {
    private MatchDtos() {}

    public record ChallengeRequest(String opponentName, Long deckId) {}

    public record AcceptRequest(Long deckId) {}

    /**
     * A practice game against the computer. {@code botDeck} is STANDARD (a deck the server builds), MIRROR (a copy of your deck),
     * or the id of one of your other saved decks. {@code style} is AGGRESSIVE or CAUTIOUS.
     */
    public record BotMatchRequest(Long deckId, String botDeck, String style) {}

    /**
     * One move. {@code type} is one of PLACE_STARTING_TANK, DESIGNATE, DEPLOY, CONVOY, REPAIR, REVEAL,
     * REVEAL_GROUP, RETREAT, RETREAT_GROUP, MOVE, USE_ABILITY, PLAY_ITEM, JAMMER_ON, JAMMER_OFF, ATTACK, END_TURN; the other fields are the ones that move needs.
     */
    public record ActionRequest(String type, Long cardId, Long groupId, Long vehicleId, List<Long> vehicleIds,
                                Long resourceId, List<Long> resourceIds, Long toGroupId,
                                List<AttackChoiceRequest> choices, List<Long> cardIds) {

        /** A request with no card list (only PLAY_ITEM needs {@code cardIds}). */
        public ActionRequest(String type, Long cardId, Long groupId, Long vehicleId, List<Long> vehicleIds,
                             Long resourceId, List<Long> resourceIds, Long toGroupId, List<AttackChoiceRequest> choices) {
            this(type, cardId, groupId, vehicleId, vehicleIds, resourceId, resourceIds, toGroupId, choices, null);
        }
    }

    public record AttackChoiceRequest(Long vehicleId, String slot, String ammo, Long targetVehicleId) {}

    /** @param result WON or LOST once the match is finished, otherwise null */
    public record MatchSummary(Long id, String status, Long opponentId, String opponentName, boolean youChallenged,
                               boolean yourTurn, String result, LocalDateTime updatedAt, boolean vsBot) {}

    public record LogLine(int seq, int turn, String text) {}

    public record ActionResponse(List<String> log, GameView view) {}

    /**
     * A game as one of its players may see it. Your own hand is shown; your opponent's hand and deck order are not
     * (only their sizes), and face-down enemy vehicles are shown without their identity or stats.
     */
    public record GameView(Long matchId, String matchStatus, String phase, long version, int youIndex,
                           int activePlayer, boolean yourTurn, int turnCount, int winner, String endReason,
                           int winChips, PlayerView you, PlayerView opponent) {}

    public record PlayerView(Long playerId, String displayName, String nation, int deckSize, int handSize,
                             List<Long> hand,                 // null for the opponent
                             List<Long> deckCards,            // the cards left in your deck, sorted (order hidden); null for the opponent
                             List<Long> discard, List<ResourceView> depot, List<GroupView> groups,
                             int chips, int groupLimit, int designationsLeft, boolean placedStartingTank) {}

    /** {@code jammer} is null if the group has none, or (for an enemy group) while it is switched off. */
    public record GroupView(long id, boolean formed, int convoyMoved, List<VehicleView> vehicles, List<ResourceView> pool,
                            JammerView jammer) {}

    /** A group's Jammer. {@code id} is what an attack targets while {@code on}. */
    public record JammerView(long id, long cardId, int hp, int maxHp, boolean on) {}

    /** For a face-down enemy vehicle, everything except {@code id}, {@code faceUp} and {@code smoked} is null. */
    public record VehicleView(long id, boolean faceUp, Long cardId, Integer hp, Integer maxHp, Integer breachStacks,
                              Boolean stunned, Boolean suppressed, Boolean disabled, Boolean abilityUsed,
                              Long eraCardId, Long camoCardId, boolean smoked) {}

    public record ResourceView(long id, long cardId, String kind, String ammunition, int remaining) {}
}
