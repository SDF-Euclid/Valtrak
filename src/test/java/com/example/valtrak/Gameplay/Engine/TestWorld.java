package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.AbilityType;
import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon;

import java.util.List;

/**
 * A small hand-built card catalog and helpers to set up game situations directly,
 * so each test only describes the situation it cares about.
 */
public final class TestWorld {

    // tanks
    public static final long TANK_COMMON = 1, TANK_UNCOMMON = 2, TANK_RARE = 3, TANK_RARE_MBT = 4,
            TANK_EPIC = 5, TANK_LEGENDARY = 6, TANK_LEGENDARY_HEAVY = 7, TANK_COMMANDER = 8;
    // other vehicles
    public static final long ANTI_AIR = 10, RECON = 11, SPECIALIST = 12, RESUPPLY = 13, AIR = 14,
            UAV = 15, SCOUT = 16;
    // resources
    public static final long APFSDS_5 = 20, HEAT_5 = 21, NATO_10 = 22, FUEL_5 = 23, FUEL_1 = 24, FUEL_10 = 25,
            SUPPLY_1 = 26, SUPPLY_3 = 27, REPAIR_25 = 28, REPAIR_FULL = 29;
    // item cards
    public static final long ERA_20 = 40, ERA_50 = 41, ARTILLERY_1 = 42, ARTILLERY_2 = 43, ARTILLERY_BLIND = 44,
            SEARCH_RESOURCES_2 = 45, SEARCH_TANK_1 = 46, SEARCH_SUPPORT_2 = 47, DRAW_1 = 48, DRAW_3 = 49,
            SMOKE_1 = 50, SMOKE_2 = 51, JAMMER_2 = 52, JAMMER_1 = 53, CAMO_1 = 54, CAMO_3 = 55, SABOTAGE_1 = 56, SABOTAGE_2 = 57,
            RECYCLE_1 = 58, RECYCLE_2 = 59, RAPID_1 = 60, RAPID_2 = 61, SABOTAGE_3 = 62;

    /** The tests use the plain numbers on the cards (5 chips, damage x1); the game's own defaults are tested in GameRulesTest. */
    public final GameRules rules = untuned();
    public final MapCardCatalog catalog = buildCatalog();
    public final GameEngine engine = new GameEngine(rules, catalog);
    public final GameState s = playingState();

    // ── building the world ───────────────────────────────────────────────────

    public static MapCardCatalog buildCatalog() {
        MapCardCatalog c = new MapCardCatalog();
        c.add(tank(TANK_COMMON, "Light Common", CardLevel.COMMON, VehicleClass.LIGHT_TANK, 100, 40));
        c.add(tank(TANK_UNCOMMON, "Light Uncommon", CardLevel.UNCOMMON, VehicleClass.LIGHT_TANK, 120, 45));
        c.add(tank(TANK_RARE, "Medium Rare", CardLevel.RARE, VehicleClass.MEDIUM_TANK, 180, 70));
        c.add(tank(TANK_RARE_MBT, "MBT Rare", CardLevel.RARE, VehicleClass.MAIN_BATTLE_TANK, 200, 85));
        c.add(tank(TANK_EPIC, "MBT Epic", CardLevel.EPIC, VehicleClass.MAIN_BATTLE_TANK, 250, 90));
        c.add(tank(TANK_LEGENDARY, "MBT Legendary", CardLevel.LEGENDARY, VehicleClass.MAIN_BATTLE_TANK, 300, 100));
        c.add(tank(TANK_LEGENDARY_HEAVY, "Heavy Legendary", CardLevel.LEGENDARY, VehicleClass.HEAVY_TANK, 320, 110));
        c.add(tank(TANK_COMMANDER, "MBT Commander", CardLevel.COMMANDER, VehicleClass.MAIN_BATTLE_TANK, 350, 120));
        c.add(armed(ANTI_AIR, "Anti-Air", CardLevel.COMMON, VehicleClass.ANTI_AIR, 80, 30));
        c.add(armed(RECON, "Recon", CardLevel.UNCOMMON, VehicleClass.RECON, 70, 25));
        c.add(new VehicleSpec(SPECIALIST, "Specialist", CardLevel.UNCOMMON, VehicleClass.SPECIALIST, 60, 20, List.of()));
        c.add(new VehicleSpec(RESUPPLY, "Resupply", CardLevel.RARE, VehicleClass.SUPPLY, 90, 30, List.of()));
        c.add(new VehicleSpec(UAV, "UAV Team", CardLevel.RARE, VehicleClass.SPECIALIST, 40, 10, List.of(),
                new AbilitySpec(AbilityType.REVEAL_ENEMY, 2, 1)));
        c.add(new VehicleSpec(SCOUT, "Scout", CardLevel.COMMON, VehicleClass.RECON, 70, 25, List.of(
                new AttackSpec(AttackSlot.ATTACK_1, "MG", Weapon.BROWNING_50CAL, 12, 1, 0, SpecialEffect.NONE)),
                new AbilitySpec(AbilityType.REVEAL_ENEMY, 1, 1)));
        c.add(armed(AIR, "Air", CardLevel.RARE, VehicleClass.CLOSE_AIR_SUPPORT, 90, 20));

        c.add(new ResourceSpec(APFSDS_5, "5x Sabot", CardLevel.UNCOMMON, ResourceKind.AMMO, Ammunition.APFSDS_120MM, 5));
        c.add(new ResourceSpec(HEAT_5, "5x HEAT", CardLevel.UNCOMMON, ResourceKind.AMMO, Ammunition.HEAT_120MM, 5));
        c.add(new ResourceSpec(NATO_10, "10x .50", CardLevel.RARE, ResourceKind.AMMO, Ammunition.NATO_127x99MM, 10));
        c.add(new ResourceSpec(FUEL_5, "5x Fuel", CardLevel.UNCOMMON, ResourceKind.FUEL, null, 5));
        c.add(new ResourceSpec(FUEL_1, "1x Fuel", CardLevel.COMMON, ResourceKind.FUEL, null, 1));
        c.add(new ResourceSpec(FUEL_10, "10x Fuel", CardLevel.RARE, ResourceKind.FUEL, null, 10));
        c.add(new ResourceSpec(SUPPLY_1, "1x Supply", CardLevel.COMMON, ResourceKind.SUPPLY, null, 1));
        c.add(new ResourceSpec(SUPPLY_3, "3x Supply", CardLevel.RARE, ResourceKind.SUPPLY, null, 3));
        c.add(new ResourceSpec(REPAIR_25, "Repair 25", CardLevel.COMMON, ResourceKind.REPAIR, null, 25));
        c.add(new ResourceSpec(REPAIR_FULL, "Full Repairs", CardLevel.LEGENDARY, ResourceKind.REPAIR, null, 999));
        c.add(new ItemSpec(ERA_20, "Light ERA", CardLevel.COMMON, ItemEffect.ERA, 20, 0, null));
        c.add(new ItemSpec(ERA_50, "Advanced ERA", CardLevel.LEGENDARY, ItemEffect.ERA, 50, 0, null));
        c.add(new ItemSpec(ARTILLERY_1, "Mortar", CardLevel.COMMON, ItemEffect.ARTILLERY, 20, 1, null));
        c.add(new ItemSpec(ARTILLERY_2, "Rocket Salvo", CardLevel.RARE, ItemEffect.ARTILLERY, 30, 2, null));
        c.add(new ItemSpec(ARTILLERY_BLIND, "Bombardment", CardLevel.LEGENDARY, ItemEffect.ARTILLERY, 40, 3, null));
        c.add(new ItemSpec(SEARCH_RESOURCES_2, "Logistics Request", CardLevel.RARE, ItemEffect.SEARCH, 0, 2, SearchKind.RESOURCE));
        c.add(new ItemSpec(SEARCH_TANK_1, "Armor Requisition", CardLevel.COMMON, ItemEffect.SEARCH, 0, 1, SearchKind.TANK));
        c.add(new ItemSpec(SEARCH_SUPPORT_2, "Specialist Call-Up", CardLevel.RARE, ItemEffect.SEARCH, 0, 2, SearchKind.SUPPORT));
        c.add(new ItemSpec(DRAW_1, "Field Report", CardLevel.COMMON, ItemEffect.DRAW, 0, 1, null));
        c.add(new ItemSpec(DRAW_3, "Total Mobilization", CardLevel.LEGENDARY, ItemEffect.DRAW, 0, 3, null));
        c.add(new ItemSpec(SMOKE_1, "Smoke Grenades", CardLevel.COMMON, ItemEffect.SMOKE, 0, 1, null));
        c.add(new ItemSpec(SMOKE_2, "Smoke Screen", CardLevel.RARE, ItemEffect.SMOKE, 0, 2, null));
        c.add(new ItemSpec(JAMMER_2, "Portable Jammer", CardLevel.COMMON, ItemEffect.JAMMER, 2, 0, null));
        c.add(new ItemSpec(JAMMER_1, "Wide-Band Jammer", CardLevel.RARE, ItemEffect.JAMMER, 1, 0, null));
        c.add(new ItemSpec(CAMO_1, "Camo Netting", CardLevel.COMMON, ItemEffect.CAMO, 1, 0, null));
        c.add(new ItemSpec(CAMO_3, "Thermal Camo", CardLevel.LEGENDARY, ItemEffect.CAMO, 3, 0, null));
        c.add(new ItemSpec(SABOTAGE_1, "Sabotage", CardLevel.COMMON, ItemEffect.SABOTAGE, 0, 1, null));
        c.add(new ItemSpec(SABOTAGE_2, "Cyber Intrusion", CardLevel.RARE, ItemEffect.SABOTAGE, 0, 2, null));
        c.add(new ItemSpec(SABOTAGE_3, "Sabotage (3)", CardLevel.RARE, ItemEffect.SABOTAGE, 0, 3, null));
        c.add(new ItemSpec(RECYCLE_1, "Salvage", CardLevel.COMMON, ItemEffect.RECYCLE, 0, 1, null));
        c.add(new ItemSpec(RECYCLE_2, "Field Recovery", CardLevel.RARE, ItemEffect.RECYCLE, 0, 2, null));
        c.add(new ItemSpec(RAPID_1, "Rapid Deployment", CardLevel.COMMON, ItemEffect.RAPID_DEPLOY, 0, 1, null));
        c.add(new ItemSpec(RAPID_2, "Forced March", CardLevel.RARE, ItemEffect.RAPID_DEPLOY, 0, 2, null));
        return c;
    }

    /** MG (ATTACK_1: 1 ammo, suppresses) and a cannon (ATTACK_2: 2 ammo, 1 fuel). */
    private static VehicleSpec tank(long id, String name, CardLevel level, VehicleClass vc, int hp, int armor) {
        return new VehicleSpec(id, name, level, vc, hp, armor, List.of(
                new AttackSpec(AttackSlot.ATTACK_1, "MG", Weapon.BROWNING_50CAL, 15, 1, 0, SpecialEffect.SUPPRESSION),
                new AttackSpec(AttackSlot.ATTACK_2, "Cannon", Weapon.SMOOTHBORE_CANNON_120MM, 40, 2, 1, SpecialEffect.NONE)));
    }

    private static VehicleSpec armed(long id, String name, CardLevel level, VehicleClass vc, int hp, int armor) {
        return new VehicleSpec(id, name, level, vc, hp, armor, List.of(
                new AttackSpec(AttackSlot.ATTACK_1, "MG", Weapon.BROWNING_50CAL, 15, 1, 0, SpecialEffect.NONE)));
    }

    private static GameRules untuned() {
        GameRules r = GameRules.defaults();
        r.winChips = 5;
        r.damagePercent = 100;
        return r;
    }

    private static GameState playingState() {
        GameState s = new GameState();
        s.phase = GameState.Phase.PLAYING;
        s.activePlayer = 0;
        s.firstPlayer = 0;
        s.nextId = 1000;
        for (int i = 0; i < 2; i++) {
            PlayerState p = new PlayerState(i);
            p.turnsTaken = 2;
            p.designationsLeft = 1;
            p.placedStartingTank = true;
            s.players.add(p);
        }
        return s;
    }

    // ── situations ───────────────────────────────────────────────────────────

    public PlayerState p(int i) { return s.player(i); }

    public void hand(int player, long... cardIds) {
        for (long id : cardIds) p(player).hand.add(id);
    }

    /** A new group of one: the tank, face up or down. */
    public StrikeGroup group(int player, long tankCardId, boolean faceUp) {
        StrikeGroup g = new StrikeGroup(s.nextId++);
        Vehicle v = vehicle(tankCardId);
        v.faceUp = faceUp;
        g.vehicles.add(v);
        p(player).groups.add(g);
        return g;
    }

    /** Adds a vehicle to a group and marks the group as formed. */
    public Vehicle add(StrikeGroup g, long cardId, boolean faceUp) {
        Vehicle v = vehicle(cardId);
        v.faceUp = faceUp;
        g.vehicles.add(v);
        g.formed = true;
        return v;
    }

    public ResourceStack pool(StrikeGroup g, long cardId) {
        ResourceStack r = stack(cardId);
        g.pool.add(r);
        return r;
    }

    public ResourceStack depot(int player, long cardId) {
        ResourceStack r = stack(cardId);
        p(player).depot.add(r);
        return r;
    }

    public Vehicle vehicle(long cardId) {
        VehicleSpec spec = catalog.vehicle(cardId);
        return new Vehicle(s.nextId++, cardId, spec.hp());
    }

    private ResourceStack stack(long cardId) {
        ResourceSpec spec = catalog.resource(cardId);
        return new ResourceStack(s.nextId++, cardId, spec.kind(), spec.ammunition(), spec.amount());
    }

    // ── doing things ─────────────────────────────────────────────────────────

    public ActionResult act(int player, Action action) {
        return engine.apply(s, player, action);
    }

    public static Action.Attack skirmish(StrikeGroup g, Vehicle attacker, AttackSlot slot, Ammunition ammo, Vehicle target) {
        return new Action.Attack(g.id, List.of(new AttackChoice(attacker.id, slot, ammo, target.id)));
    }

    public int fuelIn(StrikeGroup g) {
        return g.pool.stream().filter(x -> x.kind == ResourceKind.FUEL).mapToInt(x -> x.remaining).sum();
    }

    public int ammoIn(StrikeGroup g, Ammunition a) {
        return g.pool.stream().filter(x -> x.kind == ResourceKind.AMMO && x.ammunition == a).mapToInt(x -> x.remaining).sum();
    }
}
