package com.example.valtrak.Gameplay.Engine;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.valtrak.Gameplay.Engine.TestWorld.*;
import static org.assertj.core.api.Assertions.assertThat;

/** The server saves the whole game as JSON after every move, so it must come back exactly as it went in. */
class GameStateJsonTest {

    private final TestWorld w = new TestWorld();
    private final JsonMapper json = JsonMapper.builder().build();

    private List<Long> deck() {
        List<Long> deck = new ArrayList<>();
        long[] ids = {TANK_COMMON, TANK_UNCOMMON, TANK_RARE, TANK_RARE_MBT, TANK_EPIC, TANK_LEGENDARY, TANK_LEGENDARY_HEAVY,
                TANK_COMMANDER, ANTI_AIR, RECON, SPECIALIST, RESUPPLY, AIR, APFSDS_5, HEAT_5, NATO_10, FUEL_5, FUEL_10, SUPPLY_1, SUPPLY_3};
        for (long id : ids) for (int i = 0; i < 3; i++) deck.add(id);
        return deck;
    }

    @Test
    void aGameSurvivesBeingSavedAndLoadedAfterEveryMove() {
        Random rnd = new Random(11);
        GameState s = w.engine.newGame(deck(), deck(), rnd);
        for (int step = 0; step < 400 && s.phase != GameState.Phase.FINISHED; step++) {
            int player = s.phase == GameState.Phase.SETUP ? (s.player(0).placedStartingTank ? 1 : 0) : s.activePlayer;
            List<Action> legal = w.engine.legalActions(s, player);
            w.engine.apply(s, player, legal.get(rnd.nextInt(legal.size())));

            String saved = json.writeValueAsString(s);
            GameState loaded = json.readValue(saved, GameState.class);
            assertThat(json.writeValueAsString(loaded)).as("step " + step).isEqualTo(saved);
        }
    }

    @Test
    void aLoadedGameKeepsPlaying() {
        GameState s = w.engine.newGame(deck(), deck(), new Random(5));
        GameState loaded = json.readValue(json.writeValueAsString(s), GameState.class);
        long tank = loaded.player(0).hand.stream()
                .filter(id -> w.catalog.find(id) instanceof VehicleSpec v && v.isTank()).findFirst().orElseThrow();
        w.engine.apply(loaded, 0, new Action.PlaceStartingTank(tank));
        assertThat(loaded.player(0).groups).hasSize(1);
    }
}
