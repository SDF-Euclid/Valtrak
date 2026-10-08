package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Legendary Artillery on face-down vehicles: each hit leaves a damage counter, and what it does is worked out
 * when the vehicle is turned face up (an aircraft takes nothing).
 */
class HiddenArtilleryTest {

    private final TestWorld w = new TestWorld();
    private StrikeGroup enemy;
    private Vehicle leader, line, aircraft;

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 6; i++) { w.p(0).deck.add(TANK_COMMON); w.p(1).deck.add(TANK_COMMON); }
        enemy = w.group(1, TANK_COMMON, false);            // 100 HP, armor 40
        leader = enemy.leader();
        line = w.add(enemy, ANTI_AIR, false);               // 80 HP
        aircraft = w.add(enemy, UAV, false);                // 40 HP, an aircraft
    }

    private void fire(Vehicle... targets) {
        w.hand(0, ARTILLERY_BLIND);
        w.act(0, new PlayItem(ARTILLERY_BLIND, java.util.Arrays.stream(targets).map(t -> t.id).toList(), List.of()));
    }

    /** Player 1's turn: reveal these of their own vehicles. */
    private ActionResult reveal(Vehicle... vs) {
        w.act(0, new EndTurn());
        return w.act(1, new Reveal(java.util.Arrays.stream(vs).map(v -> v.id).toList()));
    }

    @Test
    void hitsStackAsCountersAndNothingIsWorkedOutWhileTheVehicleIsHidden() {
        fire(line, aircraft);
        assertThat(line.hiddenHits).containsExactly(40);
        assertThat(aircraft.hiddenHits).containsExactly(40);
        assertThat(line.hp).isEqualTo(80);
        assertThat(line.faceUp).isFalse();
        w.act(0, new EndTurn());
        w.act(1, new EndTurn());
        fire(line);
        assertThat(line.hiddenHits).containsExactly(40, 40);
    }

    @Test
    void theOwnerRevealingTheVehicleResolvesTheCountersAndItIsDestroyedIfTheyAreEnough() {
        fire(line);
        fire2(line);
        ActionResult r = reveal(line);
        assertThat(enemy.vehicles).as("80 HP against 40 + 40 true damage").doesNotContain(line);
        assertThat(w.p(1).discard).contains(ANTI_AIR);
        assertThat(r.log).anyMatch(l -> l.contains("counter(s) resolve"));
    }

    /** A second Legendary card on the next turn. */
    private void fire2(Vehicle target) {
        w.act(0, new EndTurn());
        w.act(1, new EndTurn());
        fire(target);
    }

    @Test
    void anAircraftThatIsRevealedTakesNothing() {
        fire(aircraft);
        ActionResult r = reveal(aircraft);
        assertThat(aircraft.hp).isEqualTo(aircraft.maxHp);
        assertThat(aircraft.faceUp).isTrue();
        assertThat(aircraft.hiddenHits).isEmpty();
        assertThat(r.log).anyMatch(l -> l.contains("aircraft") && l.contains("do nothing"));
    }

    @Test
    void anEnemyAbilityRevealingTheVehicleResolvesItAndTheArtilleryOwnerTakesTheChip() {
        leader.hp = 30;
        fire(leader);
        w.act(0, new EndTurn());
        w.act(1, new EndTurn());
        Vehicle mine = w.group(0, TANK_COMMON, true).leader();
        Vehicle uav = w.add(w.p(0).groups.get(0), UAV, true);
        w.pool(w.p(0).groups.get(0), FUEL_5);
        w.act(0, new UseAbility(uav.id, List.of(leader.id)));
        assertThat(w.p(1).groups).as("the Leader had 30 HP left, so its group is destroyed").doesNotContain(enemy);
        assertThat(w.p(0).chips).isEqualTo(1);
        assertThat(mine.hp).isEqualTo(mine.maxHp);
    }

    @Test
    void revealingAWholeGroupResolvesEveryVehicleIncludingAnAircraft() {
        fire(line, aircraft);
        w.act(0, new EndTurn());
        w.act(1, new RevealGroup(enemy.id));
        assertThat(line.hp).isEqualTo(40);                   // 80 - 40
        assertThat(aircraft.hp).isEqualTo(40);               // untouched
        assertThat(enemy.vehicles.stream().allMatch(v -> v.faceUp)).isTrue();
    }

    @Test
    void theDamageIsWorkedOutWithTheArmorRulesAtTheMomentOfRevealing() {
        w.rules.artilleryDamageType = DamageType.EXPLOSIVE;
        w.rules.artilleryCaliber = 100;
        fire(leader);                                         // armor 40: LIGHT bracket, full damage by overpressure
        reveal(leader);
        assertThat(leader.hp).isEqualTo(100 - 40);
    }

    @Test
    void aRepairCardClearsTheCountersEvenOnAVehicleAtFullHealth() {
        fire(line, aircraft);
        w.act(0, new EndTurn());
        ResourceStack kit = w.depot(1, REPAIR_25);
        ActionResult r = w.act(1, new Repair(kit.id, line.id));
        assertThat(line.hiddenHits).isEmpty();
        assertThat(r.log).anyMatch(l -> l.contains("clear the artillery counters"));
        w.act(1, new Reveal(List.of(line.id)));
        assertThat(line.hp).as("nothing left to resolve").isEqualTo(80);
        assertThat(aircraft.hiddenHits).as("only the repaired vehicle is cleared").containsExactly(40);
    }

    @Test
    void aPartialRepairIsRefusedWhenItWouldDoNothing() {
        Vehicle mine = w.group(0, TANK_COMMON, true).leader();
        mine.breachStacks = 2;                                // full HP, but BREACH only goes with a full repair
        ResourceStack kit = w.depot(0, REPAIR_25);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> w.act(0, new Repair(kit.id, mine.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("doesn't need repairs");
        assertThat(w.p(0).depot).contains(kit);
        ResourceStack full = w.depot(0, REPAIR_FULL);
        w.act(0, new Repair(full.id, mine.id));
        assertThat(mine.breachStacks).isZero();
    }

    @Test
    void countersSurviveCopyingAndSaving() {
        fire(line);
        GameState copy = w.s.copy();
        assertThat(copy.player(1).groups.get(0).vehicles.get(1).hiddenHits).containsExactly(40);
        JsonMapper json = JsonMapper.builder().build();
        GameState loaded = json.readValue(json.writeValueAsString(w.s), GameState.class);
        assertThat(loaded.player(1).groups.get(0).vehicles.get(1).hiddenHits).containsExactly(40);
    }
}
