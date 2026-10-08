package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_1;
import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Bugs found in the code review, each pinned down so it can't come back. */
class ReviewFixesTest {

    private final TestWorld w = new TestWorld();
    private StrikeGroup mine;
    private Vehicle a, b;

    @BeforeEach
    void setUp() {
        for (int i = 0; i < 4; i++) { w.p(0).deck.add(TANK_COMMON); w.p(1).deck.add(TANK_COMMON); }
        mine = w.group(0, TANK_RARE, true);
        a = w.add(mine, ANTI_AIR, true);
        b = w.add(mine, RECON, true);
        w.pool(mine, NATO_10);
    }

    private Attack assault(Vehicle... targets) {
        Vehicle[] shooters = {a, b, mine.leader()};
        List<AttackChoice> choices = new java.util.ArrayList<>();
        for (int i = 0; i < targets.length; i++) choices.add(new AttackChoice(shooters[i].id, ATTACK_1, null, targets[i].id));
        return new Attack(mine.id, choices);
    }

    @Test
    void anAssaultDoesNotHitAVehicleThatWentBackToHandWhenItsLeaderDied() {
        StrikeGroup theirs = w.group(1, TANK_COMMON, true);
        theirs.leader().hp = 1;
        Vehicle recon = w.add(theirs, SCOUT, true);
        recon.hp = 1;
        w.act(0, assault(theirs.leader(), recon));                      // used to throw half-way, after paying and taking the chip
        assertThat(w.p(0).chips).isEqualTo(1);
        assertThat(w.p(1).hand).contains(SCOUT);
        assertThat(w.s.activePlayer).as("the attack ended the turn").isEqualTo(1);
    }

    @Test
    void anAssaultDoesNotHitATankThatWentFaceDownWhenItsLeaderDied() {
        StrikeGroup theirs = w.group(1, TANK_RARE, true);
        theirs.leader().hp = 1;
        Vehicle tank = w.add(theirs, TANK_COMMON, true);
        tank.hp = 1;
        w.act(0, assault(theirs.leader(), tank));
        assertThat(w.p(0).chips).as("one group destroyed is one chip, not two").isEqualTo(1);
        assertThat(tank.hp).isEqualTo(1);
        assertThat(tank.faceUp).isFalse();
    }

    @Test
    void aJammerIsDiscardedOnlyOnceWhenItsGroupDies() {
        StrikeGroup theirs = w.group(1, TANK_COMMON, true);
        theirs.leader().hp = 1;
        theirs.jammerCardId = JAMMER_2;
        theirs.jammerId = 7777;
        theirs.jammerHp = 80;
        theirs.jammerMaxHp = 80;
        theirs.jammerOn = true;
        Attack attack = new Attack(mine.id, List.of(
                new AttackChoice(a.id, ATTACK_1, null, theirs.leader().id),
                new AttackChoice(b.id, ATTACK_1, null, 7777)));
        w.act(0, attack);
        assertThat(w.p(1).discard.stream().filter(id -> id == JAMMER_2)).hasSize(1);
    }

    @Test
    void revealingStopsOnceTheGameIsWon() {
        w.p(0).chips = 4;
        w.s.activePlayer = 1;
        StrikeGroup g1 = w.group(1, TANK_COMMON, false);
        StrikeGroup g2 = w.group(1, TANK_COMMON, false);
        g1.leader().hp = 1;
        g2.leader().hp = 1;
        g1.leader().hiddenHits.add(40);
        g2.leader().hiddenHits.add(40);
        ActionResult r = w.act(1, new Reveal(List.of(g1.leader().id, g2.leader().id)));
        assertThat(w.s.phase).isEqualTo(GameState.Phase.FINISHED);
        assertThat(w.p(0).chips).isEqualTo(5);
        assertThat(r.log.stream().filter(l -> l.contains("Game over"))).hasSize(1);
    }

    @Test
    void aConvoyCanNotDoubleItsCapacityByChangingGroup() {
        Vehicle truck = w.add(mine, RESUPPLY, false);                    // Rare: 2 cards a turn
        StrikeGroup other = w.group(0, TANK_COMMON, false);
        other.formed = true;
        w.pool(mine, FUEL_10);
        ResourceStack f1 = w.depot(0, FUEL_1), f2 = w.depot(0, FUEL_1), f3 = w.depot(0, FUEL_1);
        w.act(0, new Convoy(mine.id, List.of(f1.id, f2.id)));
        w.act(0, new Move(truck.id, other.id));
        assertThatThrownBy(() -> w.act(0, new Convoy(other.id, List.of(f3.id))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("2 already moved");
        w.act(0, new EndTurn());
        w.act(1, new EndTurn());
        w.act(0, new Convoy(other.id, List.of(f3.id)));                  // a new turn: allowed again
        assertThat(other.pool).contains(f3);
    }

    @Test
    void theLogDoesNotNameHiddenAttachments() {
        w.hand(0, ERA_50, CAMO_1, JAMMER_2);
        ActionResult era = w.act(0, new PlayItem(ERA_50, List.of(a.id), List.of()));
        ActionResult camo = w.act(0, new PlayItem(CAMO_1, List.of(a.id), List.of()));
        ActionResult jam = w.act(0, new PlayItem(JAMMER_2, List.of(mine.id), List.of()));
        for (ActionResult r : List.of(era, camo, jam)) {
            assertThat(r.log).noneMatch(l -> l.contains("ERA") || l.contains("Camo") || l.contains("Jammer"));
        }
    }
}
