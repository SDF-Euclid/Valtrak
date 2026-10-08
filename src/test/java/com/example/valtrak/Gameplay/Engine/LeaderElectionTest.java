package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.Test;

import static com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot.ATTACK_1;
import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The Leader is the highest-rarity tank in the group; on a tie the one that has been there longest. */
class LeaderElectionTest {

    private final TestWorld w = new TestWorld();

    private int totalSupply() {
        return w.p(0).depot.stream().filter(r -> r.kind == ResourceKind.SUPPLY).mapToInt(r -> r.remaining).sum();
    }

    @Test
    void aHigherRarityTankThatJoinsBecomesTheLeader() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.hand(0, TANK_LEGENDARY);
        w.depot(0, SUPPLY_3);
        w.act(0, new Deploy(TANK_LEGENDARY, g.id));
        assertThat(g.leader().cardId).isEqualTo(TANK_LEGENDARY);
        assertThat(g.vehicles.get(1).cardId).isEqualTo(TANK_COMMON);
        assertThat(g.vehicles).hasSize(2);
    }

    @Test
    void theFormationCostFollowsTheFutureLeaderSoACheapLeaderIsNoLoophole() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);               // a Common leader alone would cost 1 Supply
        w.hand(0, TANK_LEGENDARY);
        w.depot(0, SUPPLY_1);
        assertThatThrownBy(() -> w.act(0, new Deploy(TANK_LEGENDARY, g.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("3 Supply");
        assertThat(g.vehicles).hasSize(1);
        assertThat(totalSupply()).isEqualTo(1);
        w.depot(0, SUPPLY_3);
        w.act(0, new Deploy(TANK_LEGENDARY, g.id));
        assertThat(totalSupply()).isEqualTo(1);                       // 4 - 3
    }

    @Test
    void aLowerRarityTankJoinsAsLineAndTheLeaderStays() {
        StrikeGroup g = w.group(0, TANK_EPIC, false);
        w.hand(0, TANK_COMMON);
        w.depot(0, SUPPLY_3);
        w.act(0, new Deploy(TANK_COMMON, g.id));
        assertThat(g.leader().cardId).isEqualTo(TANK_EPIC);
        assertThat(totalSupply()).isEqualTo(1);                       // cost set by the EPIC leader: 2
    }

    @Test
    void onATieTheTankThatWasThereFirstStaysLeader() {
        StrikeGroup g = w.group(0, TANK_RARE, false);                 // RARE medium tank
        Vehicle original = g.leader();
        w.hand(0, TANK_RARE_MBT);                                     // also RARE
        w.depot(0, SUPPLY_3);
        w.act(0, new Deploy(TANK_RARE_MBT, g.id));
        assertThat(g.leader()).isSameAs(original);
    }

    @Test
    void nonTanksNeverLeadEvenIfTheyAreRarer() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.hand(0, AIR, UAV);                                          // both RARE, neither is a tank
        w.depot(0, SUPPLY_3);
        w.act(0, new Deploy(AIR, g.id));
        w.act(0, new Deploy(UAV, g.id));
        assertThat(g.leader().cardId).isEqualTo(TANK_COMMON);
    }

    @Test
    void theDisplacedLeaderBecomesALineTankAndCanMoveButTheNewLeaderCannot() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.hand(0, TANK_LEGENDARY);
        w.depot(0, SUPPLY_3);
        w.act(0, new Deploy(TANK_LEGENDARY, g.id));
        w.pool(g, FUEL_5);
        Vehicle newLeader = g.leader();
        Vehicle displaced = g.vehicles.get(1);

        assertThatThrownBy(() -> w.act(0, new Move(newLeader.id, null)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Leader");
        w.act(0, new Move(displaced.id, null));                       // out to lead a group of its own
        assertThat(w.p(0).groups).hasSize(2);
        assertThat(w.p(0).groups.get(1).leader()).isSameAs(displaced);
    }

    @Test
    void aFullGroupCannotTakeABetterTankBecauseTheOldLeaderWouldNeedAThirdLineSlot() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.add(g, TANK_UNCOMMON, false);
        w.add(g, ANTI_AIR, false);                                    // leader + 2 Line
        w.hand(0, TANK_LEGENDARY);
        assertThatThrownBy(() -> w.act(0, new Deploy(TANK_LEGENDARY, g.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Line");
    }

    @Test
    void aTankMovedInFromAnotherGroupCanBecomeTheLeaderToo() {
        StrikeGroup small = w.group(0, TANK_COMMON, false);
        StrikeGroup big = w.group(0, TANK_RARE, false);
        Vehicle epic = w.add(big, TANK_EPIC, false);                  // a Line tank in the other group (set up directly)
        w.pool(big, FUEL_5);
        w.depot(0, SUPPLY_3);
        w.act(0, new Move(epic.id, small.id));
        assertThat(small.leader()).isSameAs(epic);
        assertThat(small.vehicles.get(1).cardId).isEqualTo(TANK_COMMON);
    }

    @Test
    void destroyingTheNewLeaderDestroysTheGroup() {
        StrikeGroup g = w.group(1, TANK_COMMON, true);
        w.p(1).hand.add(TANK_LEGENDARY);
        w.s.activePlayer = 1;
        w.depot(1, SUPPLY_3);
        w.act(1, new Deploy(TANK_LEGENDARY, g.id));
        g.leader().faceUp = true;
        g.leader().hp = 1;
        w.p(0).deck.add(TANK_COMMON);
        w.p(1).deck.add(TANK_COMMON);
        w.act(1, new EndTurn());

        StrikeGroup mine = w.group(0, TANK_COMMON, true);
        w.pool(mine, NATO_10);
        w.act(0, skirmish(mine, mine.leader(), ATTACK_1, null, g.leader()));
        assertThat(w.p(0).chips).isEqualTo(1);
        assertThat(w.p(1).groups.stream().anyMatch(x -> x == g)).isFalse();
        assertThat(w.p(1).discard).contains(TANK_LEGENDARY);
        assertThat(w.p(1).groups.stream().anyMatch(x -> x.leader().cardId == TANK_COMMON)).as("the survivor stands alone").isTrue();
    }
}
