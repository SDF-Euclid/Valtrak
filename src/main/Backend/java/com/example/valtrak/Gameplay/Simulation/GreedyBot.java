package com.example.valtrak.Gameplay.Simulation;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Gameplay.Engine.*;
import com.example.valtrak.Gameplay.Engine.Action.*;

import java.util.*;
import java.util.random.RandomGenerator;

/**
 * A simple strategy bot. Each turn it: plays the resource card it needs most, deploys tanks, repairs, reveals its
 * strike group when it can afford an attack, then attacks the enemy Leader it can hurt most.
 * <p>
 * {@code aggressive} bots reveal as soon as their group is ready, even if there is nobody to shoot at.
 * Cautious bots only reveal when they can attack straight away, because a revealed group can be attacked.
 */
public final class GreedyBot implements Bot {

    private final boolean aggressive;
    private final int maxGroupsBeforeWaiting;

    public GreedyBot(boolean aggressive) {
        this(aggressive, 2);
    }

    /** @param maxGroupsBeforeWaiting the bot stops starting new lone groups at this many and waits to add to its main group */
    public GreedyBot(boolean aggressive, int maxGroupsBeforeWaiting) {
        this.aggressive = aggressive;
        this.maxGroupsBeforeWaiting = maxGroupsBeforeWaiting;
    }

    @Override
    public String name() {
        return aggressive ? "aggressive" : "cautious";
    }

    @Override
    public Action choose(GameEngine engine, GameState s, int player, RandomGenerator rng) {
        PlayerState me = s.player(player);
        PlayerState opp = s.player(1 - player);
        CardCatalog cat = engine.catalog();

        if (s.phase == GameState.Phase.SETUP) return placeStartingTank(me, cat);

        Action a;
        if ((a = designate(engine, s, player, me, cat)) != null) return a;
        if ((a = deploy(engine, s, player, me, cat)) != null) return a;
        if ((a = repair(engine, s, player, me, cat)) != null) return a;
        if ((a = items(engine, s, player, me, opp, cat, rng)) != null) return a;
        if ((a = scout(engine, s, player, me, opp, cat)) != null) return a;
        if ((a = reveal(engine, s, player, me, opp, cat)) != null) return a;
        if ((a = attack(engine, s, player, me, opp, cat)) != null) return a;
        return new EndTurn();
    }

    // ── setup ────────────────────────────────────────────────────────────────

    private Action placeStartingTank(PlayerState me, CardCatalog cat) {
        return me.hand.stream()
                .filter(id -> cat.find(id) instanceof VehicleSpec v && v.isTank())
                .max(Comparator.comparingInt(id -> cat.vehicle(id).level().ordinal()))
                .<Action>map(PlaceStartingTank::new).orElseThrow();
    }

    // ── economy ──────────────────────────────────────────────────────────────

    private Action designate(GameEngine engine, GameState s, int player, PlayerState me, CardCatalog cat) {
        if (me.designationsLeft <= 0) return null;
        StrikeGroup main = mainGroup(me, cat);
        Pool pool = main == null ? new Pool() : Pool.of(main);
        Need need = main == null ? new Need() : needOf(main, cat);
        boolean wantsSupply = main != null && !main.formed && main.vehicles.size() == 1
                && me.hand.stream().anyMatch(id -> cat.find(id) instanceof VehicleSpec v && !v.isTank());
        boolean damaged = me.groups.stream().flatMap(g -> g.vehicles.stream()).anyMatch(v -> v.hp < v.maxHp);
        int supplyHeld = me.depot.stream().filter(x -> x.kind == ResourceKind.SUPPLY).mapToInt(x -> x.remaining).sum();

        Action best = null;
        double bestScore = 0;
        for (long id : new LinkedHashSet<>(me.hand)) {
            if (!(cat.find(id) instanceof ResourceSpec rs)) continue;
            double score;
            Long target = null;
            switch (rs.kind()) {
                case AMMO -> {
                    if (main == null || !need.ammoTypes.contains(rs.ammunition())) { score = 0; break; }
                    score = pool.ammo.getOrDefault(rs.ammunition(), 0) < need.ammoPerType.getOrDefault(rs.ammunition(), 1)
                            ? 100 + rs.amount() : 10 + rs.amount();
                    target = main.id;
                }
                case FUEL -> {
                    if (main == null) { score = 0; break; }
                    score = pool.fuel < need.fuel ? 90 + rs.amount() : 5 + rs.amount();
                    target = main.id;
                }
                case SUPPLY -> score = (wantsSupply && supplyHeld < 3) ? 95 : 20;
                case REPAIR -> score = damaged ? 50 : 1;
                default -> score = 0;
            }
            if (score > bestScore) {
                Action candidate = new Designate(id, target);
                if (engine.isLegal(s, player, candidate)) {
                    best = candidate;
                    bestScore = score;
                }
            }
        }
        return best;
    }

    private Action deploy(GameEngine engine, GameState s, int player, PlayerState me, CardCatalog cat) {
        List<Long> vehicles = me.hand.stream().filter(id -> cat.find(id) instanceof VehicleSpec).distinct()
                .sorted(Comparator.comparingInt((Long id) -> -cat.vehicle(id).level().ordinal())).toList();
        StrikeGroup main = mainGroup(me, cat);
        for (long id : vehicles) {
            if (main != null) {
                Action join = new Deploy(id, main.id);
                if (engine.isLegal(s, player, join)) return join;
            }
            if (cat.vehicle(id).isTank() && me.groups.size() < maxGroupsBeforeWaiting) {
                Action alone = new Deploy(id, null);
                if (engine.isLegal(s, player, alone)) return alone;
            }
        }
        return null;
    }

    private Action repair(GameEngine engine, GameState s, int player, PlayerState me, CardCatalog cat) {
        Vehicle worst = me.groups.stream().flatMap(g -> g.vehicles.stream())
                .filter(v -> v.hp < v.maxHp)
                .min(Comparator.comparingDouble(v -> (double) v.hp / v.maxHp)).orElse(null);
        if (worst == null) return null;
        for (ResourceStack stack : me.depot) {
            if (stack.kind != ResourceKind.REPAIR) continue;
            Action a = new Repair(stack.id, worst.id);
            if (engine.isLegal(s, player, a)) return a;
        }
        return null;
    }

    // ── item cards ───────────────────────────────────────────────────────────

    /** Plays the first item card that is worth playing: draw and search for cards, ERA on the best tank, Artillery at what it can hit. */
    private Action items(GameEngine engine, GameState s, int player, PlayerState me, PlayerState opp, CardCatalog cat, RandomGenerator rng) {
        for (long id : new LinkedHashSet<>(me.hand)) {
            if (!(cat.find(id) instanceof ItemSpec item)) continue;
            Action a = switch (item.effect()) {
                case DRAW -> me.deck.size() > 12 ? new PlayItem(id, List.of(), List.of()) : null;
                case SEARCH -> me.deck.size() > 12 ? new PlayItem(id, List.of(), searchPicks(item, me, cat)) : null;
                case ERA -> eraTarget(me, cat).<Action>map(v -> new PlayItem(id, List.of(v.id), List.of())).orElse(null);
                case ARTILLERY -> {
                    List<Long> targets = artilleryTargets(engine, item, opp, cat, rng);
                    yield targets.isEmpty() ? null : new PlayItem(id, targets, List.of());
                }
            };
            if (a != null && engine.isLegal(s, player, a)) return a;
        }
        return null;
    }

    /** The best cards of the searched kind in the deck: Ammo for the main group's weapons first, scouts before other support vehicles, strongest tanks. */
    private List<Long> searchPicks(ItemSpec item, PlayerState me, CardCatalog cat) {
        StrikeGroup main = mainGroup(me, cat);
        Need need = main == null ? new Need() : needOf(main, cat);
        List<Long> found = new ArrayList<>();
        for (long id : me.deck) {
            CardSpec spec = cat.find(id);
            boolean fits = switch (item.searchKind()) {
                case RESOURCE -> spec instanceof ResourceSpec;
                case TANK -> spec instanceof VehicleSpec v && v.isTank();
                case SUPPORT -> spec instanceof VehicleSpec v && !v.isTank();
            };
            if (fits) found.add(id);
        }
        found.sort(Comparator.comparingDouble((Long id) -> -searchScore(cat.find(id), need)));
        return found.stream().limit(item.count()).toList();
    }

    private static double searchScore(CardSpec spec, Need need) {
        if (spec instanceof ResourceSpec r) {
            return switch (r.kind()) {
                case AMMO -> need.ammoTypes.contains(r.ammunition()) ? 30 + r.amount() : 5;
                case FUEL -> 20 + r.amount();
                case SUPPLY -> 10;
                case REPAIR -> 2;
            };
        }
        VehicleSpec v = (VehicleSpec) spec;
        return (v.ability() != null ? 10 : 0) + v.level().ordinal();
    }

    /** The highest-rarity tank without ERA (the Leader of a group is the one worth protecting). */
    private java.util.Optional<Vehicle> eraTarget(PlayerState me, CardCatalog cat) {
        return me.groups.stream().flatMap(g -> g.vehicles.stream())
                .filter(v -> v.eraCardId == 0 && cat.vehicle(v.cardId).isTank())
                .max(Comparator.comparingInt(v -> cat.vehicle(v.cardId).level().ordinal()));
    }

    /** Face-up Leaders first (killing one takes the chip), then the weakest; a Legendary card also fires at random face-down vehicles. */
    private List<Long> artilleryTargets(GameEngine engine, ItemSpec item, PlayerState opp, CardCatalog cat, RandomGenerator rng) {
        Set<Vehicle> leaders = new HashSet<>();
        for (StrikeGroup g : opp.groups) leaders.add(g.leader());
        List<Vehicle> up = opp.groups.stream().flatMap(g -> g.vehicles.stream()).filter(v -> v.faceUp)
                .sorted(Comparator.comparingInt((Vehicle v) -> leaders.contains(v) ? 0 : 1).thenComparingInt(v -> v.hp)).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        List<Long> out = new ArrayList<>(up.stream().limit(item.count()).map(v -> v.id).toList());
        if (out.size() < item.count() && item.level().compareTo(engine.rules().artilleryBlindFrom) >= 0) {
            List<Vehicle> down = new ArrayList<>(opp.groups.stream().flatMap(g -> g.vehicles.stream()).filter(v -> !v.faceUp).toList());
            Collections.shuffle(down, new java.util.Random(rng.nextLong()));
            down.stream().limit(item.count() - out.size()).forEach(v -> out.add(v.id));
        }
        return out;
    }

    // ── fighting ─────────────────────────────────────────────────────────────

    /** When a group is ready to attack but an enemy Leader is hidden, bring out a UAV / Recon vehicle and reveal enemy Leaders. */
    private Action scout(GameEngine engine, GameState s, int player, PlayerState me, PlayerState opp, CardCatalog cat) {
        if (s.firstPlayer == player && me.turnsTaken <= 1) return null;          // can't attack this turn anyway
        List<Vehicle> hiddenLeaders = new ArrayList<>();
        List<Vehicle> hiddenOthers = new ArrayList<>();
        for (StrikeGroup g : opp.groups) {
            for (Vehicle v : g.vehicles) {
                if (v.faceUp) continue;
                if (v == g.leader()) hiddenLeaders.add(v); else hiddenOthers.add(v);
            }
        }
        if (hiddenLeaders.isEmpty()) return null;
        hiddenLeaders.sort(Comparator.comparingInt((Vehicle v) -> -cat.vehicle(v.cardId).level().ordinal()));
        for (StrikeGroup g : me.groups) {
            if (!canAffordAnAttack(g, cat)) continue;
            for (Vehicle v : g.vehicles) {
                AbilitySpec ability = cat.vehicle(v.cardId).ability();
                if (ability == null || v.abilityUsed || Pool.of(g).fuel < ability.fuelCost()) continue;
                if (!v.faceUp) {
                    Action reveal = new Reveal(List.of(v.id));
                    if (engine.isLegal(s, player, reveal)) return reveal;
                    continue;
                }
                List<Vehicle> order = new ArrayList<>(hiddenLeaders);
                order.addAll(hiddenOthers);
                Action use = new UseAbility(v.id, order.stream().limit(ability.power()).map(x -> x.id).toList());
                if (engine.isLegal(s, player, use)) return use;
            }
        }
        return null;
    }

    private Action reveal(GameEngine engine, GameState s, int player, PlayerState me, PlayerState opp, CardCatalog cat) {
        boolean targetsExist = !targets(opp, cat).isEmpty();
        boolean attackBanned = s.firstPlayer == player && me.turnsTaken <= 1;
        if (!aggressive && (!targetsExist || attackBanned)) return null;
        for (StrikeGroup g : me.groups) {
            boolean hasHidden = g.vehicles.stream().anyMatch(v -> !v.faceUp && !cat.vehicle(v.cardId).isResupply());
            if (!hasHidden || !canAffordAnAttack(g, cat)) continue;
            Action a = new RevealGroup(g.id);
            if (engine.isLegal(s, player, a)) return a;
        }
        return null;
    }

    private Action attack(GameEngine engine, GameState s, int player, PlayerState me, PlayerState opp, CardCatalog cat) {
        List<Vehicle> targets = targets(opp, cat);
        if (targets.isEmpty()) return null;
        Vehicle target = pickTarget(opp, targets);
        VehicleSpec targetSpec = cat.vehicle(target.cardId);
        boolean targetIsLeader = opp.groups.stream().anyMatch(g -> g.leader() == target);

        Attack best = null;
        double bestScore = -1;
        for (StrikeGroup g : me.groups) {
            Plan plan = plan(g, target, targetSpec, cat);
            if (plan == null) continue;
            double score = plan.damage + (plan.damage >= target.hp ? (targetIsLeader ? 1000 : 100) : 0) + (targetIsLeader ? 50 : 0);
            Attack candidate = new Attack(g.id, plan.choices);
            if (score > bestScore && engine.isLegal(s, player, candidate)) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    /** Enemy vehicles that can be shot right now. */
    private List<Vehicle> targets(PlayerState opp, CardCatalog cat) {
        List<Vehicle> out = new ArrayList<>();
        for (StrikeGroup g : opp.groups) {
            for (Vehicle v : g.vehicles) if (v.faceUp) out.add(v);
        }
        return out;
    }

    /** A face-up Leader if there is one (killing it takes the chip), the weakest one first; otherwise the weakest vehicle. */
    private Vehicle pickTarget(PlayerState opp, List<Vehicle> targets) {
        Set<Vehicle> leaders = new HashSet<>();
        for (StrikeGroup g : opp.groups) leaders.add(g.leader());
        return targets.stream()
                .min(Comparator.comparingInt((Vehicle v) -> leaders.contains(v) ? 0 : 1).thenComparingInt(v -> v.hp))
                .orElseThrow();
    }

    private record Plan(List<AttackChoice> choices, double damage) {}

    /** Every able, face-up vehicle in the group fires its best affordable attack at the target. */
    private Plan plan(StrikeGroup g, Vehicle target, VehicleSpec targetSpec, CardCatalog cat) {
        Pool pool = Pool.of(g);
        List<Vehicle> attackers = new ArrayList<>();
        for (Vehicle v : g.vehicles) {
            VehicleSpec spec = cat.vehicle(v.cardId);
            if (v.faceUp && !v.stunned && !v.disabled && !spec.isResupply() && !spec.attacks().isEmpty()) attackers.add(v);
        }
        attackers.sort(Comparator.comparingInt((Vehicle v) -> -cat.vehicle(v.cardId).attacks().stream()
                .mapToInt(AttackSpec::baseDamage).max().orElse(0)));

        List<AttackChoice> choices = new ArrayList<>();
        List<Double> damages = new ArrayList<>();
        double total = 0;
        for (Vehicle v : attackers) {
            if (total >= target.hp) break;                       // enough already; keep the rest in the pool
            Choice c = bestChoice(cat.vehicle(v.cardId), v, pool, target, targetSpec);
            if (c == null) continue;
            pool.spend(c.ammo, c.attack.ammoCost(), c.attack.fuelCost());
            choices.add(new AttackChoice(v.id, c.attack.slot(), c.ammo, target.id));
            damages.add((double) c.damage);
            total += c.damage;
        }
        if (choices.isEmpty()) return null;
        if (choices.size() >= 2 && !g.leader().faceUp) {         // a Combined Assault needs the Leader face up
            int best = 0;
            for (int i = 1; i < damages.size(); i++) if (damages.get(i) > damages.get(best)) best = i;
            return new Plan(List.of(choices.get(best)), damages.get(best));
        }
        return new Plan(choices, total);
    }

    private record Choice(AttackSpec attack, Ammunition ammo, int damage) {}

    private Choice bestChoice(VehicleSpec spec, Vehicle v, Pool pool, Vehicle target, VehicleSpec targetSpec) {
        Choice best = null;
        for (AttackSpec at : spec.attacks()) {
            if (v.suppressed && at.slot() == AttackSlot.ATTACK_1) continue;
            for (Ammunition am : at.weapon().getCompatibleAmmunition()) {
                if (pool.ammo.getOrDefault(am, 0) < at.ammoCost() || pool.fuel < at.fuelCost()) continue;
                int dmg = DamageCalculator.calculate(at.baseDamage(), at.effect(), am, targetSpec.armor(), target.breachStacks).damage();
                if (best == null || dmg > best.damage) best = new Choice(at, am, dmg);
            }
        }
        return best;
    }

    // ── bookkeeping ──────────────────────────────────────────────────────────

    /** The group the bot is building up: the biggest, then the one with the best Leader. */
    private StrikeGroup mainGroup(PlayerState me, CardCatalog cat) {
        return me.groups.stream()
                .max(Comparator.comparingInt((StrikeGroup g) -> g.vehicles.size())
                        .thenComparingInt(g -> cat.vehicle(g.leader().cardId).level().ordinal()))
                .orElse(null);
    }

    private boolean canAffordAnAttack(StrikeGroup g, CardCatalog cat) {
        Pool pool = Pool.of(g);
        for (Vehicle v : g.vehicles) {
            VehicleSpec spec = cat.vehicle(v.cardId);
            if (spec.isResupply()) continue;
            for (AttackSpec at : spec.attacks()) {
                for (Ammunition am : at.weapon().getCompatibleAmmunition()) {
                    if (pool.ammo.getOrDefault(am, 0) >= at.ammoCost() && pool.fuel >= at.fuelCost()) return true;
                }
            }
        }
        return false;
    }

    /** What a group would need in its pool to fire every vehicle's strongest attack. */
    private static final class Need {
        final Set<Ammunition> ammoTypes = EnumSet.noneOf(Ammunition.class);
        final Map<Ammunition, Integer> ammoPerType = new EnumMap<>(Ammunition.class);
        int fuel;
    }

    private Need needOf(StrikeGroup g, CardCatalog cat) {
        Need need = new Need();
        for (Vehicle v : g.vehicles) {
            VehicleSpec spec = cat.vehicle(v.cardId);
            for (AttackSpec at : spec.attacks()) need.ammoTypes.addAll(at.weapon().getCompatibleAmmunition());
            spec.attacks().stream().max(Comparator.comparingInt(AttackSpec::baseDamage)).ifPresent(at -> {
                need.fuel += at.fuelCost();
                for (Ammunition am : at.weapon().getCompatibleAmmunition()) need.ammoPerType.merge(am, at.ammoCost(), Integer::sum);
            });
        }
        return need;
    }

    private static final class Pool {
        final Map<Ammunition, Integer> ammo = new EnumMap<>(Ammunition.class);
        int fuel;

        static Pool of(StrikeGroup g) {
            Pool p = new Pool();
            for (ResourceStack r : g.pool) {
                if (r.kind == ResourceKind.AMMO) p.ammo.merge(r.ammunition, r.remaining, Integer::sum);
                else if (r.kind == ResourceKind.FUEL) p.fuel += r.remaining;
            }
            return p;
        }

        void spend(Ammunition am, int ammoCost, int fuelCost) {
            ammo.merge(am, -ammoCost, Integer::sum);
            fuel -= fuelCost;
        }
    }
}
