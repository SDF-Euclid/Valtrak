package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.ActionRequest;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.AttackChoiceRequest;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.ApiException;
import com.example.valtrak.Gameplay.Engine.Action;
import com.example.valtrak.Gameplay.Engine.AttackChoice;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActionMapperTest {

    private static ActionRequest req(String type) {
        return new ActionRequest(type, null, null, null, null, null, null, null, null);
    }

    @Test
    void mapsSimpleActions() {
        assertThat(ActionMapper.map(req("END_TURN"))).isEqualTo(new Action.EndTurn());
        assertThat(ActionMapper.map(new ActionRequest("DESIGNATE", 5L, 9L, null, null, null, null, null, null)))
                .isEqualTo(new Action.Designate(5L, 9L));
        assertThat(ActionMapper.map(new ActionRequest("DEPLOY", 5L, null, null, null, null, null, null, null)))
                .isEqualTo(new Action.Deploy(5L, null));
        assertThat(ActionMapper.map(new ActionRequest("MOVE", null, null, 7L, null, null, null, null, null)))
                .isEqualTo(new Action.Move(7L, null));
        assertThat(ActionMapper.map(new ActionRequest("CONVOY", null, 3L, null, null, null, List.of(1L, 2L), null, null)))
                .isEqualTo(new Action.Convoy(3L, List.of(1L, 2L)));
        assertThat(ActionMapper.map(new ActionRequest("REPAIR", null, null, 4L, null, 8L, null, null, null)))
                .isEqualTo(new Action.Repair(8L, 4L));
    }

    @Test
    void mapsAnAttackWithAmmoAndSlot() {
        var choice = new AttackChoiceRequest(1L, "ATTACK_2", "APFSDS_120MM", 2L);
        Action a = ActionMapper.map(new ActionRequest("ATTACK", null, 6L, null, null, null, null, null, List.of(choice)));
        assertThat(a).isEqualTo(new Action.Attack(6L, List.of(new AttackChoice(1L, AttackSlot.ATTACK_2, Ammunition.APFSDS_120MM, 2L))));
    }

    @Test
    void ammoMayBeLeftOut() {
        var choice = new AttackChoiceRequest(1L, "ATTACK_1", null, 2L);
        Action a = ActionMapper.map(new ActionRequest("ATTACK", null, 6L, null, null, null, null, null, List.of(choice)));
        assertThat(((Action.Attack) a).choices().get(0).ammo()).isNull();
    }

    @Test
    void missingFieldsAndUnknownValuesAreBadRequests() {
        assertThatThrownBy(() -> ActionMapper.map(req("DEPLOY"))).isInstanceOf(ApiException.class).hasMessageContaining("cardId");
        assertThatThrownBy(() -> ActionMapper.map(req("CONVOY"))).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> ActionMapper.map(req("ATTACK"))).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> ActionMapper.map(req("DANCE"))).isInstanceOf(ApiException.class).hasMessageContaining("Unknown");
        assertThatThrownBy(() -> ActionMapper.map(null)).isInstanceOf(ApiException.class);
        var badSlot = new AttackChoiceRequest(1L, "ATTACK_9", null, 2L);
        assertThatThrownBy(() -> ActionMapper.map(new ActionRequest("ATTACK", null, 6L, null, null, null, null, null, List.of(badSlot))))
                .isInstanceOf(ApiException.class).hasMessageContaining("slot");
        var badAmmo = new AttackChoiceRequest(1L, "ATTACK_1", "LASER", 2L);
        assertThatThrownBy(() -> ActionMapper.map(new ActionRequest("ATTACK", null, 6L, null, null, null, null, null, List.of(badAmmo))))
                .isInstanceOf(ApiException.class).hasMessageContaining("ammunition");
    }
}
