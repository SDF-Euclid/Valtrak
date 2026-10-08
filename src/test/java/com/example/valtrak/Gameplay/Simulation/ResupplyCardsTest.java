package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Gameplay.Engine.*;
import com.example.valtrak.Gameplay.Engine.Action.Convoy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The real Resupply vehicles from the card library, and the convoy working with them. */
class ResupplyCardsTest {

    private final EnumCardCatalog catalog = new EnumCardCatalog();

    private List<VehicleSpec> resupply() {
        return catalog.all().stream().filter(c -> c instanceof VehicleSpec v && v.vehicleClass() == VehicleClass.SUPPLY)
                .map(c -> (VehicleSpec) c).toList();
    }

    @Test
    void thereAreSixTwoPerNationRareAndLegendaryWithNoAttacksOrAbility() {
        assertThat(resupply()).hasSize(6);
        assertThat(resupply()).allMatch(v -> v.attacks().isEmpty() && v.ability() == null && v.isResupply() && !v.isTank());
        assertThat(resupply().stream().filter(v -> v.level() == CardLevel.RARE)).hasSize(3);
        assertThat(resupply().stream().filter(v -> v.level() == CardLevel.LEGENDARY)).hasSize(3);
        assertThat(resupply().stream().map(VehicleSpec::name)).contains("M977 HEMTT Supply Truck", "Ural-4320 Supply Truck", "MAN SX 8x8 Supply Truck");
    }

    /** One player with a tank and a Resupply vehicle in a group, and a Depot full of Fuel. */
    private Object[] setup(VehicleSpec truck) {
        GameEngine engine = new GameEngine(GameRules.defaults(), catalog);
        GameState s = new GameState();
        s.phase = GameState.Phase.PLAYING;
        s.nextId = 1000;
        for (int i = 0; i < 2; i++) {
            PlayerState p = new PlayerState(i);
            p.turnsTaken = 2;
            p.placedStartingTank = true;
            s.players.add(p);
        }
        VehicleSpec tank = catalog.all().stream().filter(c -> c instanceof VehicleSpec v && v.isTank()).map(c -> (VehicleSpec) c).findFirst().orElseThrow();
        StrikeGroup g = new StrikeGroup(1);
        g.vehicles.add(new Vehicle(10, tank.cardId(), tank.hp()));
        g.vehicles.add(new Vehicle(11, truck.cardId(), truck.hp()));
        g.formed = true;
        s.player(0).groups.add(g);
        ResourceSpec fuel = catalog.all().stream().filter(c -> c instanceof ResourceSpec r && r.kind() == ResourceKind.FUEL).map(c -> (ResourceSpec) c).findFirst().orElseThrow();
        for (int i = 0; i < 5; i++) s.player(0).depot.add(new ResourceStack(100 + i, fuel.cardId(), ResourceKind.FUEL, null, fuel.amount()));
        return new Object[]{engine, s};
    }

    private void convoy(Object[] world, int cards) {
        GameEngine engine = (GameEngine) world[0];
        GameState s = (GameState) world[1];
        engine.apply(s, 0, new Convoy(1, java.util.stream.LongStream.range(100, 100 + cards).boxed().toList()));
    }

    @Test
    void aRareTruckConvoysTwoCardsAndALegendaryOneThree() {
        VehicleSpec rare = resupply().stream().filter(v -> v.level() == CardLevel.RARE).findFirst().orElseThrow();
        Object[] w1 = setup(rare);
        assertThatThrownBy(() -> convoy(w1, 3)).isInstanceOf(RuleViolationException.class).hasMessageContaining("2 card");
        convoy(w1, 2);
        assertThat(((GameState) w1[1]).player(0).groups.get(0).pool).hasSize(2);
        assertThat(((GameState) w1[1]).player(0).depot).hasSize(3);

        VehicleSpec legendary = resupply().stream().filter(v -> v.level() == CardLevel.LEGENDARY).findFirst().orElseThrow();
        Object[] w2 = setup(legendary);
        assertThatThrownBy(() -> convoy(w2, 4)).isInstanceOf(RuleViolationException.class).hasMessageContaining("3 card");
        convoy(w2, 3);
        assertThat(((GameState) w2[1]).player(0).groups.get(0).pool).hasSize(3);
    }
}
