package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.*;
import com.example.valtrak.Data.GameData.Entity.MatchRecord;
import com.example.valtrak.Data.GameData.Entity.Player;
import com.example.valtrak.Data.GameData.Enums.MatchStatus;
import com.example.valtrak.Gameplay.Engine.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;

/** The view is the only thing a player ever sees of the game, so these tests are the leak check. */
class GameViewBuilderTest {

    private final TestWorld w = new TestWorld();
    private final JsonMapper json = JsonMapper.builder().build();

    private MatchRecord match() {
        Player a = new Player("a@x.com", "Alice", "United States", "a@x.com");
        a.setId(1L);
        Player b = new Player("b@x.com", "Bobby", "Germany", "b@x.com");
        b.setId(2L);
        MatchRecord m = new MatchRecord(a, b, List.of());
        m.setId(10L);
        m.setStatus(MatchStatus.ACTIVE);
        return m;
    }

    @Test
    void youSeeYourOwnHandButOnlyTheSizeOfYourOpponents() {
        w.hand(0, TANK_COMMON, FUEL_5);
        w.hand(1, TANK_LEGENDARY, SUPPLY_3, SPECIALIST);
        w.p(0).deck.addAll(List.of(1L, 2L, 3L, 4L));
        w.p(1).deck.addAll(List.of(5L, 6L));

        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);

        assertThat(view.you().hand()).containsExactly(TANK_COMMON, FUEL_5);
        assertThat(view.opponent().hand()).isNull();
        assertThat(view.opponent().handSize()).isEqualTo(3);
        assertThat(view.opponent().deckSize()).isEqualTo(2);
        assertThat(view.you().displayName()).isEqualTo("Alice");
        assertThat(view.opponent().displayName()).isEqualTo("Bobby");
    }

    @Test
    void faceDownEnemyVehiclesAreShownWithoutIdentityOrStats() {
        StrikeGroup g = w.group(1, TANK_LEGENDARY, false);
        Vehicle hidden = w.add(g, ANTI_AIR, false);
        Vehicle revealed = w.add(g, RECON, true);
        hidden.hp = 5;
        hidden.stunned = true;

        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        List<VehicleView> seen = view.opponent().groups().get(0).vehicles();

        VehicleView up = seen.get(0);                 // face-up vehicles come first, then the face-down ones
        VehicleView leader = byId(seen, g.leader().id);
        assertThat(leader.faceUp()).isFalse();
        assertThat(leader.cardId()).isNull();
        assertThat(leader.hp()).isNull();
        VehicleView down = byId(seen, hidden.id);
        assertThat(down.cardId()).isNull();
        assertThat(down.hp()).isNull();
        assertThat(down.stunned()).isNull();
        assertThat(up.faceUp()).isTrue();
        assertThat(up.cardId()).isEqualTo(RECON);
        assertThat(up.hp()).isEqualTo(70);
        assertThat(up.id()).isEqualTo(revealed.id);
    }

    private static VehicleView byId(List<VehicleView> views, long id) {
        return views.stream().filter(v -> v.id() == id).findFirst().orElseThrow();
    }

    @Test
    void anOpponentCannotTellWhichFaceDownVehicleIsTheLeader() {
        StrikeGroup first = w.group(1, TANK_LEGENDARY, false);
        Vehicle a = w.add(first, ANTI_AIR, false);
        Vehicle b = w.add(first, RECON, false);
        Vehicle shown = w.add(first, UAV, true);
        List<Long> before = idsOf(GameViewBuilder.build(match(), w.s, 0, w.rules).opponent().groups().get(0).vehicles());

        // the same vehicles with the Leader somewhere else in the list must look exactly the same from outside
        first.vehicles.remove(0);
        first.vehicles.add(1, w.vehicle(TANK_LEGENDARY));
        first.vehicles.set(1, first.vehicles.get(1));
        java.util.Collections.swap(first.vehicles, 0, 2);
        List<Long> after = idsOf(GameViewBuilder.build(match(), w.s, 0, w.rules).opponent().groups().get(0).vehicles());

        assertThat(before.get(0)).isEqualTo(shown.id);
        assertThat(before).hasSize(4);
        assertThat(after.get(0)).isEqualTo(shown.id);
        // and the Leader itself is not first among the hidden ones just because it leads
        assertThat(idsOf(GameViewBuilder.build(match(), w.s, 1, w.rules).you().groups().get(0).vehicles()))
                .as("you still see your own group in its real order").isEqualTo(first.vehicles.stream().map(v -> v.id).toList());
        assertThat(a.id).isNotEqualTo(b.id);
    }

    private static List<Long> idsOf(List<VehicleView> views) {
        return views.stream().map(VehicleView::id).toList();
    }

    @Test
    void eraIsShownOnYourVehiclesAndOnFaceUpEnemiesButNotOnFaceDownOnes() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        mine.leader().eraCardId = ERA_20;
        StrikeGroup theirs = w.group(1, TANK_RARE, true);
        theirs.leader().eraCardId = ERA_50;
        Vehicle hidden = w.add(theirs, ANTI_AIR, false);
        hidden.eraCardId = ERA_20;

        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        assertThat(view.you().groups().get(0).vehicles().get(0).eraCardId()).isEqualTo(ERA_20);
        assertThat(byId(view.opponent().groups().get(0).vehicles(), theirs.leader().id).eraCardId()).isEqualTo(ERA_50);
        assertThat(byId(view.opponent().groups().get(0).vehicles(), hidden.id).eraCardId()).isNull();
        assertThat(json.writeValueAsString(view.opponent().groups().get(0).vehicles().get(1))).doesNotContain("" + ERA_20);
    }

    @Test
    void smokeIsPublicButAttachmentsOnAFaceDownVehicleAreNot() {
        StrikeGroup theirs = w.group(1, TANK_RARE, false);
        theirs.leader().smoked = true;
        Vehicle hidden = w.add(theirs, ANTI_AIR, false);
        hidden.camoCardId = CAMO_1;
        Vehicle shown = w.add(theirs, RECON, true);
        shown.camoCardId = CAMO_3;

        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        GroupView group = view.opponent().groups().get(0);

        assertThat(byId(group.vehicles(), theirs.leader().id).smoked()).isTrue();
        assertThat(byId(group.vehicles(), theirs.leader().id).cardId()).isNull();
        assertThat(byId(group.vehicles(), hidden.id).camoCardId()).isNull();
        assertThat(byId(group.vehicles(), shown.id).camoCardId()).isEqualTo(CAMO_3);
    }

    @Test
    void youSeeYourOwnJammerAlwaysButAnOpponentOnlySeesOneThatIsOn() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        mine.jammerCardId = JAMMER_2;
        mine.jammerId = 77;
        mine.jammerHp = 50;
        mine.jammerMaxHp = 80;
        StrikeGroup theirs = w.group(1, TANK_RARE, false);
        theirs.jammerCardId = JAMMER_1;
        theirs.jammerId = 78;
        theirs.jammerHp = 160;
        theirs.jammerMaxHp = 160;

        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        assertThat(view.you().groups().get(0).jammer()).isEqualTo(new JammerView(77, JAMMER_2, 50, 80, false));
        assertThat(view.opponent().groups().get(0).jammer()).as("their Jammer is off").isNull();

        theirs.jammerOn = true;
        view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        assertThat(view.opponent().groups().get(0).jammer()).isEqualTo(new JammerView(78, JAMMER_1, 160, 160, true));
    }

    @Test
    void abilityUseIsShownForYourVehiclesAndHiddenForFaceDownEnemies() {
        StrikeGroup mine = w.group(0, TANK_COMMON, false);
        Vehicle uav = w.add(mine, UAV, true);
        uav.abilityUsed = true;
        StrikeGroup theirs = w.group(1, TANK_RARE, false);
        Vehicle theirUav = w.add(theirs, UAV, false);
        theirUav.abilityUsed = true;

        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        assertThat(view.you().groups().get(0).vehicles().get(1).abilityUsed()).isTrue();
        assertThat(view.opponent().groups().get(0).vehicles()).allMatch(v -> v.abilityUsed() == null);
    }

    @Test
    void yourOwnFaceDownVehiclesAreShownInFull() {
        StrikeGroup g = w.group(0, TANK_LEGENDARY, false);
        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        VehicleView mine = view.you().groups().get(0).vehicles().get(0);
        assertThat(mine.faceUp()).isFalse();
        assertThat(mine.cardId()).isEqualTo(TANK_LEGENDARY);
        assertThat(mine.hp()).isEqualTo(300);
    }

    @Test
    void theDepotPoolsAndDiscardArePublic() {
        StrikeGroup g = w.group(1, TANK_COMMON, false);
        w.pool(g, FUEL_5);
        w.depot(1, SUPPLY_3);
        w.p(1).discard.add(HEAT_5);
        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        assertThat(view.opponent().depot()).hasSize(1);
        assertThat(view.opponent().groups().get(0).pool()).hasSize(1);
        assertThat(view.opponent().discard()).containsExactly(HEAT_5);
    }

    @Test
    void theOpponentsHandAndHiddenCardsNeverAppearInTheJson() {
        w.hand(1, SPECIALIST, REPAIR_FULL);
        StrikeGroup g = w.group(1, TANK_LEGENDARY_HEAVY, false);
        w.add(g, RESUPPLY, false);
        String text = json.writeValueAsString(GameViewBuilder.build(match(), w.s, 0, w.rules));
        assertThat(text).doesNotContain("\"cardId\":" + SPECIALIST);
        assertThat(text).doesNotContain("\"cardId\":" + REPAIR_FULL);
        assertThat(text).doesNotContain("\"cardId\":" + TANK_LEGENDARY_HEAVY);
        assertThat(text).doesNotContain("\"cardId\":" + RESUPPLY);
        assertThat(text).doesNotContain("@x.com");                   // no email addresses
    }

    @Test
    void yourTurnFollowsThePhase() {
        MatchRecord m = match();
        assertThat(GameViewBuilder.build(m, w.s, 0, w.rules).yourTurn()).isTrue();
        assertThat(GameViewBuilder.build(m, w.s, 1, w.rules).yourTurn()).isFalse();

        w.s.phase = GameState.Phase.SETUP;
        w.p(0).placedStartingTank = false;
        w.p(1).placedStartingTank = true;
        assertThat(GameViewBuilder.build(m, w.s, 0, w.rules).yourTurn()).isTrue();    // you still have to place a tank
        assertThat(GameViewBuilder.build(m, w.s, 1, w.rules).yourTurn()).isFalse();

        w.s.phase = GameState.Phase.FINISHED;
        assertThat(GameViewBuilder.build(m, w.s, 0, w.rules).yourTurn()).isFalse();
    }

    @Test
    void groupLimitInTheViewGrowsWithChips() {
        w.p(0).chips = 4;
        GameView view = GameViewBuilder.build(match(), w.s, 0, w.rules);
        assertThat(view.you().groupLimit()).isEqualTo(5);
        assertThat(view.opponent().groupLimit()).isEqualTo(3);
    }
}
