package com.example.valtrak.Gameplay.Engine;

import java.util.List;

/** Everything a player can do. Ids are card ids (cards in hand) or field ids (vehicles, groups, resource cards on the table). */
public sealed interface Action {

    /** Setup: put a tank from your hand on the field face down. */
    record PlaceStartingTank(long cardId) implements Action {}

    /** Play a resource card from your hand. {@code groupId} null = your Depot, otherwise that group's pool (Ammo and Fuel only). */
    record Designate(long cardId, Long groupId) implements Action {}

    /** Play a vehicle from your hand face down. {@code groupId} null = start a new group of one (tanks only). */
    record Deploy(long cardId, Long groupId) implements Action {}

    /** Convoy: move resource cards from the Depot into a group's pool. */
    record Convoy(long groupId, List<Long> resourceIds) implements Action {}

    /** Spend a Repair card from the Depot on a vehicle. */
    record Repair(long resourceId, long vehicleId) implements Action {}

    /** Turn vehicles face up (free). */
    record Reveal(List<Long> vehicleIds) implements Action {}

    /** Turn every face-down vehicle in a group face up (free). */
    record RevealGroup(long groupId) implements Action {}

    /** Turn one vehicle face down (costs Fuel). */
    record Retreat(long vehicleId) implements Action {}

    /** Turn the whole group face down (costs double the Leader's retreat Fuel). */
    record RetreatGroup(long groupId) implements Action {}

    /** Move a vehicle to another group, or (tanks only) out to a new group of one when {@code toGroupId} is null. */
    record Move(long vehicleId, Long toGroupId) implements Action {}

    /** Use a vehicle's ability (REVEAL_ENEMY: turn these face-down enemy vehicles face up). */
    record UseAbility(long vehicleId, List<Long> targetVehicleIds) implements Action {}

    /**
     * Play an item card from your hand. {@code targetIds}: ERA = one of your vehicles; Artillery = enemy vehicles.
     * {@code cardIds}: Search = the cards to take from your deck. Draw needs neither.
     */
    record PlayItem(long cardId, List<Long> targetIds, List<Long> cardIds) implements Action {}

    /** Switch a strike group's Jammer on or off (free). */
    record SetJammer(long groupId, boolean on) implements Action {}

    /** Attack with one vehicle (Skirmish) or several (Combined Assault). Ends your turn. */
    record Attack(long groupId, List<AttackChoice> choices) implements Action {}

    record EndTurn() implements Action {}
}
