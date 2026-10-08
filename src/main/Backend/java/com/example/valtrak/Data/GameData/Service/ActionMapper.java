package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.ActionRequest;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.AttackChoiceRequest;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.ApiException;
import com.example.valtrak.Gameplay.Engine.Action;
import com.example.valtrak.Gameplay.Engine.AttackChoice;
import org.springframework.http.HttpStatus;

import java.util.List;

/** Turns a request body into an engine {@link Action}, checking that the fields that move needs are present. */
public final class ActionMapper {
    private ActionMapper() {}

    public static Action map(ActionRequest r) {
        if (r == null || r.type() == null) throw bad("Say which action you want to take (type).");
        return switch (r.type()) {
            case "PLACE_STARTING_TANK" -> new Action.PlaceStartingTank(need(r.cardId(), "cardId"));
            case "DESIGNATE" -> new Action.Designate(need(r.cardId(), "cardId"), r.groupId());
            case "DEPLOY" -> new Action.Deploy(need(r.cardId(), "cardId"), r.groupId());
            case "CONVOY" -> new Action.Convoy(need(r.groupId(), "groupId"), needList(r.resourceIds(), "resourceIds"));
            case "REPAIR" -> new Action.Repair(need(r.resourceId(), "resourceId"), need(r.vehicleId(), "vehicleId"));
            case "REVEAL" -> new Action.Reveal(needList(r.vehicleIds(), "vehicleIds"));
            case "REVEAL_GROUP" -> new Action.RevealGroup(need(r.groupId(), "groupId"));
            case "RETREAT" -> new Action.Retreat(need(r.vehicleId(), "vehicleId"));
            case "RETREAT_GROUP" -> new Action.RetreatGroup(need(r.groupId(), "groupId"));
            case "MOVE" -> new Action.Move(need(r.vehicleId(), "vehicleId"), r.toGroupId());
            case "USE_ABILITY" -> new Action.UseAbility(need(r.vehicleId(), "vehicleId"), needList(r.vehicleIds(), "vehicleIds"));
            case "PLAY_ITEM" -> new Action.PlayItem(need(r.cardId(), "cardId"),
                    r.vehicleIds() == null ? List.of() : r.vehicleIds(), r.cardIds() == null ? List.of() : r.cardIds());
            case "ATTACK" -> new Action.Attack(need(r.groupId(), "groupId"), choices(r.choices()));
            case "END_TURN" -> new Action.EndTurn();
            default -> throw bad("Unknown action type: " + r.type());
        };
    }

    private static List<AttackChoice> choices(List<AttackChoiceRequest> requested) {
        if (requested == null || requested.isEmpty()) throw bad("An attack needs at least one choice.");
        return requested.stream().map(c -> {
            AttackSlot slot;
            try {
                slot = AttackSlot.valueOf(String.valueOf(c.slot()));
            } catch (IllegalArgumentException e) {
                throw bad("Unknown attack slot: " + c.slot());
            }
            Ammunition ammo = null;
            if (c.ammo() != null) {
                try {
                    ammo = Ammunition.valueOf(c.ammo());
                } catch (IllegalArgumentException e) {
                    throw bad("Unknown ammunition: " + c.ammo());
                }
            }
            return new AttackChoice(need(c.vehicleId(), "vehicleId"), slot, ammo, need(c.targetVehicleId(), "targetVehicleId"));
        }).toList();
    }

    private static long need(Long value, String field) {
        if (value == null) throw bad("Missing " + field + ".");
        return value;
    }

    private static List<Long> needList(List<Long> value, String field) {
        if (value == null || value.isEmpty()) throw bad("Missing " + field + ".");
        return value;
    }

    private static ApiException bad(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
