package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Gameplay.Engine.Action.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourcesAndDeploymentTest {

    private final TestWorld w = new TestWorld();

    // ── designating resources ────────────────────────────────────────────────

    @Test
    void designatePutsTheCardInTheDepotWithItsFullAmount() {
        w.hand(0, APFSDS_5);
        w.act(0, new Designate(APFSDS_5, null));
        assertThat(w.p(0).hand).isEmpty();
        assertThat(w.p(0).depot).singleElement().satisfies(r -> assertThat(r.remaining).isEqualTo(5));
        assertThat(w.p(0).designationsLeft).isZero();
    }

    @Test
    void onlyOneResourceCardPerTurn() {
        w.hand(0, APFSDS_5, FUEL_5);
        w.act(0, new Designate(APFSDS_5, null));
        assertThatThrownBy(() -> w.act(0, new Designate(FUEL_5, null)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("already played");
    }

    @Test
    void ammoAndFuelCanGoStraightIntoAPool() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.hand(0, FUEL_5);
        w.act(0, new Designate(FUEL_5, g.id));
        assertThat(w.fuelIn(g)).isEqualTo(5);
        assertThat(w.p(0).depot).isEmpty();
    }

    @Test
    void supplyAndRepairCannotGoIntoAPool() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.hand(0, SUPPLY_1, REPAIR_25);
        assertThatThrownBy(() -> w.act(0, new Designate(SUPPLY_1, g.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Only Ammo and Fuel");
        assertThatThrownBy(() -> w.act(0, new Designate(REPAIR_25, g.id)))
                .isInstanceOf(RuleViolationException.class);
    }

    @Test
    void cardMustBeInHandAndMustBeAResource() {
        w.hand(0, TANK_COMMON);
        assertThatThrownBy(() -> w.act(0, new Designate(FUEL_5, null))).isInstanceOf(RuleViolationException.class);
        assertThatThrownBy(() -> w.act(0, new Designate(TANK_COMMON, null))).isInstanceOf(RuleViolationException.class);
    }

    // ── deploying ────────────────────────────────────────────────────────────

    @Test
    void aTankDeploysAloneAsAGroupOfOneFaceDown() {
        w.hand(0, TANK_COMMON);
        w.act(0, new Deploy(TANK_COMMON, null));
        assertThat(w.p(0).groups).hasSize(1);
        Vehicle v = w.p(0).groups.get(0).leader();
        assertThat(v.faceUp).isFalse();
        assertThat(w.p(0).groups.get(0).formed).isFalse();
    }

    @Test
    void nonTanksCannotStandAlone() {
        w.hand(0, ANTI_AIR);
        assertThatThrownBy(() -> w.act(0, new Deploy(ANTI_AIR, null)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("isn't a tank");
    }

    @Test
    void groupLimitIsThreeAndGrowsByOneEveryTwoChips() {
        for (int i = 0; i < 3; i++) w.group(0, TANK_COMMON, false);
        w.hand(0, TANK_COMMON, TANK_COMMON);
        assertThatThrownBy(() -> w.act(0, new Deploy(TANK_COMMON, null)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("limit is 3");

        w.p(0).chips = 1;
        assertThatThrownBy(() -> w.act(0, new Deploy(TANK_COMMON, null))).isInstanceOf(RuleViolationException.class);

        w.p(0).chips = 2;
        w.act(0, new Deploy(TANK_COMMON, null));
        assertThat(w.p(0).groups).hasSize(4);
    }

    @Test
    void formingAGroupCostsSupplyByLeaderRarityOnlyOnce() {
        StrikeGroup g = w.group(0, TANK_RARE, false);          // RARE leader: 2 Supply
        w.hand(0, ANTI_AIR, RECON);
        w.depot(0, SUPPLY_1);
        assertThatThrownBy(() -> w.act(0, new Deploy(ANTI_AIR, g.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("2 Supply");
        assertThat(w.p(0).hand).contains(ANTI_AIR);             // nothing changed

        w.depot(0, SUPPLY_3);
        w.act(0, new Deploy(ANTI_AIR, g.id));
        assertThat(g.formed).isTrue();
        assertThat(w.p(0).depot.stream().mapToInt(r -> r.remaining).sum()).isEqualTo(2); // 4 - 2
        w.act(0, new Deploy(RECON, g.id));                      // already formed: free
        assertThat(w.p(0).depot.stream().mapToInt(r -> r.remaining).sum()).isEqualTo(2);
    }

    @Test
    void supplyCardsAreUsedUpAndDiscarded() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);        // COMMON leader: 1 Supply
        w.hand(0, ANTI_AIR);
        w.depot(0, SUPPLY_1);
        w.act(0, new Deploy(ANTI_AIR, g.id));
        assertThat(w.p(0).depot).isEmpty();
        assertThat(w.p(0).discard).containsExactly(SUPPLY_1);
    }

    @Test
    void groupSlotsAreLeaderPlusTwoLinePlusSpecialistPlusResupply() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.add(g, ANTI_AIR, false);
        w.add(g, RECON, false);
        w.hand(0, TANK_UNCOMMON, SPECIALIST, SPECIALIST, RESUPPLY, RESUPPLY, AIR);

        assertThatThrownBy(() -> w.act(0, new Deploy(TANK_UNCOMMON, g.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Line slots");
        w.act(0, new Deploy(SPECIALIST, g.id));
        assertThatThrownBy(() -> w.act(0, new Deploy(SPECIALIST, g.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Specialist");
        w.act(0, new Deploy(RESUPPLY, g.id));
        assertThatThrownBy(() -> w.act(0, new Deploy(RESUPPLY, g.id)))
                .isInstanceOf(RuleViolationException.class);
        assertThat(g.vehicles).hasSize(5);
        assertThatThrownBy(() -> w.act(0, new Deploy(AIR, g.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("full");
    }

    // ── moving vehicles ──────────────────────────────────────────────────────

    @Test
    void movingAVehicleCostsFuelByRarityFromTheGroupItLeaves() {
        StrikeGroup from = w.group(0, TANK_COMMON, false);
        Vehicle recon = w.add(from, RECON, false);              // UNCOMMON: 1 Fuel
        StrikeGroup to = w.group(0, TANK_COMMON, false);
        to.formed = true;
        assertThatThrownBy(() -> w.act(0, new Move(recon.id, to.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Fuel");

        w.pool(from, FUEL_5);
        w.act(0, new Move(recon.id, to.id));
        assertThat(w.fuelIn(from)).isEqualTo(4);
        assertThat(to.vehicles).contains(recon);
        assertThat(from.vehicles).doesNotContain(recon);
    }

    @Test
    void leadersCannotLeaveTheirGroup() {
        StrikeGroup from = w.group(0, TANK_COMMON, false);
        w.add(from, ANTI_AIR, false);
        StrikeGroup to = w.group(0, TANK_COMMON, false);
        w.pool(from, FUEL_5);
        assertThatThrownBy(() -> w.act(0, new Move(from.leader().id, to.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Leader");
    }

    @Test
    void aLineTankCanMoveOutToLeadANewGroupIfUnderTheLimit() {
        StrikeGroup from = w.group(0, TANK_COMMON, false);
        Vehicle tank = w.add(from, TANK_UNCOMMON, false);
        w.pool(from, FUEL_5);
        w.act(0, new Move(tank.id, null));
        assertThat(w.p(0).groups).hasSize(2);
        assertThat(w.p(0).groups.get(1).leader()).isSameAs(tank);
    }

    @Test
    void nonTanksCannotMoveOutAlone() {
        StrikeGroup from = w.group(0, TANK_COMMON, false);
        Vehicle aa = w.add(from, ANTI_AIR, false);
        w.pool(from, FUEL_5);
        assertThatThrownBy(() -> w.act(0, new Move(aa.id, null))).isInstanceOf(RuleViolationException.class);
    }

    @Test
    void movingIntoALoneTankPaysTheFormationCostToo() {
        StrikeGroup from = w.group(0, TANK_COMMON, false);
        Vehicle aa = w.add(from, ANTI_AIR, false);
        StrikeGroup lone = w.group(0, TANK_COMMON, false);
        w.pool(from, FUEL_5);
        assertThatThrownBy(() -> w.act(0, new Move(aa.id, lone.id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Supply");
        w.depot(0, SUPPLY_1);
        w.act(0, new Move(aa.id, lone.id));
        assertThat(lone.formed).isTrue();
    }

    // ── reveal and retreat ───────────────────────────────────────────────────

    @Test
    void revealIsFreeAndRetreatCostsFuelByRarity() {
        StrikeGroup g = w.group(0, TANK_RARE, false);          // RARE: retreat 2 Fuel
        w.pool(g, FUEL_5);
        w.act(0, new Reveal(List.of(g.leader().id)));
        assertThat(g.leader().faceUp).isTrue();
        assertThat(w.fuelIn(g)).isEqualTo(5);

        w.act(0, new Retreat(g.leader().id));
        assertThat(g.leader().faceUp).isFalse();
        assertThat(w.fuelIn(g)).isEqualTo(3);
        assertThat(g.vehicles).hasSize(1);                      // a retreated vehicle stays in its group
        assertThatThrownBy(() -> w.act(0, new Retreat(g.leader().id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("already face down");
    }

    @Test
    void retreatingAWholeGroupCostsDoubleTheLeadersRetreatCost() {
        StrikeGroup g = w.group(0, TANK_RARE, true);           // leader 2 Fuel, doubled = 4
        w.add(g, ANTI_AIR, true);
        w.add(g, RECON, true);
        w.pool(g, FUEL_5);
        w.act(0, new RetreatGroup(g.id));
        assertThat(g.vehicles).allMatch(v -> !v.faceUp);
        assertThat(w.fuelIn(g)).isEqualTo(1);
    }

    @Test
    void retreatNeedsEnoughFuel() {
        StrikeGroup g = w.group(0, TANK_RARE, true);
        w.pool(g, FUEL_1);
        assertThatThrownBy(() -> w.act(0, new Retreat(g.leader().id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Fuel");
        assertThat(g.leader().faceUp).isTrue();
    }

    @Test
    void aResupplyVehicleIsNeverRevealed() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        Vehicle supply = w.add(g, RESUPPLY, false);
        Vehicle aa = w.add(g, ANTI_AIR, false);
        assertThatThrownBy(() -> w.act(0, new Reveal(List.of(supply.id)))).isInstanceOf(RuleViolationException.class);
        w.act(0, new RevealGroup(g.id));
        assertThat(g.leader().faceUp).isTrue();
        assertThat(aa.faceUp).isTrue();
        assertThat(supply.faceUp).isFalse();
    }

    // ── convoy ───────────────────────────────────────────────────────────────

    @Test
    void convoyNeedsAResupplyVehicle() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        ResourceStack fuel = w.depot(0, FUEL_5);
        assertThatThrownBy(() -> w.act(0, new Convoy(g.id, List.of(fuel.id))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("no Resupply");
    }

    @Test
    void convoyMovesCardsUpToItsCapacityPerTurn() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.add(g, RESUPPLY, false);                              // RARE: capacity 2
        ResourceStack a = w.depot(0, FUEL_5);
        ResourceStack b = w.depot(0, APFSDS_5);
        ResourceStack c = w.depot(0, HEAT_5);

        w.act(0, new Convoy(g.id, List.of(a.id)));
        assertThat(g.pool).containsExactly(a);
        assertThatThrownBy(() -> w.act(0, new Convoy(g.id, List.of(b.id, c.id))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("per turn");
        w.act(0, new Convoy(g.id, List.of(b.id)));
        assertThat(g.pool).containsExactly(a, b);
        assertThat(w.p(0).depot).containsExactly(c);

        // a new turn resets the capacity
        w.p(0).deck.add(TANK_COMMON);
        w.p(1).deck.add(TANK_COMMON);
        w.act(0, new EndTurn());
        w.act(1, new EndTurn());
        assertThat(g.convoyMoved).isZero();
    }

    @Test
    void convoyOnlyMovesAmmoAndFuel() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        w.add(g, RESUPPLY, false);
        ResourceStack supply = w.depot(0, SUPPLY_3);
        assertThatThrownBy(() -> w.act(0, new Convoy(g.id, List.of(supply.id))))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("Only Ammo and Fuel");
    }

    // ── repairs ──────────────────────────────────────────────────────────────

    @Test
    void repairHealsUpToItsAmountThenIsDiscarded() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        g.leader().hp = 50;
        ResourceStack kit = w.depot(0, REPAIR_25);
        w.act(0, new Repair(kit.id, g.leader().id));
        assertThat(g.leader().hp).isEqualTo(75);
        assertThat(w.p(0).depot).isEmpty();
        assertThat(w.p(0).discard).containsExactly(REPAIR_25);
    }

    @Test
    void repairNeverHealsPastMaxHp() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        g.leader().hp = 90;
        ResourceStack kit = w.depot(0, REPAIR_25);
        w.act(0, new Repair(kit.id, g.leader().id));
        assertThat(g.leader().hp).isEqualTo(100);
    }

    @Test
    void fullRepairsRestoresEverythingAndRemovesBreach() {
        StrikeGroup g = w.group(0, TANK_LEGENDARY, false);
        g.leader().hp = 10;
        g.leader().breachStacks = 2;
        ResourceStack kit = w.depot(0, REPAIR_FULL);
        w.act(0, new Repair(kit.id, g.leader().id));
        assertThat(g.leader().hp).isEqualTo(300);
        assertThat(g.leader().breachStacks).isZero();
    }

    @Test
    void repairingAHealthyVehicleIsRejected() {
        StrikeGroup g = w.group(0, TANK_COMMON, false);
        ResourceStack kit = w.depot(0, REPAIR_25);
        assertThatThrownBy(() -> w.act(0, new Repair(kit.id, g.leader().id)))
                .isInstanceOf(RuleViolationException.class).hasMessageContaining("doesn't need");
        assertThat(w.p(0).depot).containsExactly(kit);
    }
}
