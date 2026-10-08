package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Gameplay.Engine.Action.*;

import java.util.*;
import java.util.random.RandomGenerator;

/**
 * The Valtrak rules (see docs/RULEBOOK.md). It knows nothing about databases or the web:
 * give it a {@link GameState}, a player and an {@link Action}, and it either changes the state
 * or throws a {@link RuleViolationException} saying why the move isn't allowed.
 * Every action checks all of its rules before it changes anything, so a rejected move leaves the state untouched.
 */
public final class GameEngine {

    private final GameRules rules;
    private final CardCatalog catalog;

    public GameEngine(GameRules rules, CardCatalog catalog) {
        this.rules = rules;
        this.catalog = catalog;
    }

    public CardCatalog catalog() {
        return catalog;
    }

    public GameRules rules() {
        return rules;
    }

    // ── Decks and setup ──────────────────────────────────────────────────────

    /** @return a list of problems; empty means the deck is allowed */
    public List<String> validateDeck(List<Long> deck) {
        List<String> problems = new ArrayList<>();
        if (deck == null || deck.size() < rules.minDeckSize) {
            problems.add("A deck needs at least " + rules.minDeckSize + " cards.");
        }
        if (deck != null && deck.size() > rules.maxDeckSize) {
            problems.add("A deck can have at most " + rules.maxDeckSize + " cards.");
        }
        if (deck == null) return problems;
        Map<Long, Integer> copies = new HashMap<>();
        int tanks = 0;
        for (long id : deck) {
            CardSpec spec = catalog.find(id);
            if (spec == null) {
                problems.add("The deck contains an unknown card (" + id + ").");
                continue;
            }
            if (copies.merge(id, 1, Integer::sum) == rules.maxCopies + 1) {
                problems.add("A deck can have at most " + rules.maxCopies + " copies of " + spec.name() + ".");
            }
            if (spec instanceof VehicleSpec v && v.isTank()) tanks++;
        }
        if (tanks < rules.minTanksInDeck) {
            problems.add("A deck needs at least " + rules.minTanksInDeck + " tanks (it has " + tanks + ").");
        }
        return problems;
    }

    /**
     * Shuffles the decks, deals opening hands (mulligan if there is no tank; the opponent draws an extra card
     * per mulligan, up to the cap) and picks who goes first. The game then waits in SETUP for each player's
     * {@link PlaceStartingTank}.
     */
    public GameState newGame(List<Long> deck0, List<Long> deck1, RandomGenerator rng) {
        for (int i = 0; i < 2; i++) {
            List<String> problems = validateDeck(i == 0 ? deck0 : deck1);
            if (!problems.isEmpty()) {
                throw new RuleViolationException("Player " + (i + 1) + "'s deck isn't allowed: " + String.join(" ", problems));
            }
        }
        GameState s = new GameState();
        for (int i = 0; i < 2; i++) {
            PlayerState p = new PlayerState(i);
            p.deck.addAll(i == 0 ? deck0 : deck1);
            Collections.shuffle(p.deck, rng);
            draw(p, rules.startingHandSize);
            int guard = 0;
            while (!hasTank(p.hand)) {
                if (++guard > 200) throw new RuleViolationException("Couldn't deal a hand with a tank.");
                p.mulligans++;
                p.deck.addAll(p.hand);
                p.hand.clear();
                Collections.shuffle(p.deck, rng);
                draw(p, rules.startingHandSize);
            }
            s.players.add(p);
        }
        for (int i = 0; i < 2; i++) {
            draw(s.player(i), Math.min(rules.mulliganExtraDrawCap, s.player(1 - i).mulligans));
        }
        s.rngSeed = rng.nextLong();
        s.firstPlayer = rng.nextInt(2);
        s.activePlayer = s.firstPlayer;
        s.phase = GameState.Phase.SETUP;
        return s;
    }

    // ── Applying actions ─────────────────────────────────────────────────────

    /** Does the move to {@code s}, or throws {@link RuleViolationException} without changing anything. */
    public ActionResult apply(GameState s, int player, Action action) {
        return applyInPlace(s, player, action);
    }

    /** A player gives up. Allowed at any time, even when it isn't their turn. */
    public ActionResult resign(GameState s, int player) {
        if (s.phase == GameState.Phase.FINISHED) throw violation("The game is over.");
        ActionResult r = new ActionResult();
        finish(s, 1 - player, "Player " + (player + 1) + " resigned.", r);
        return r;
    }

    /** True if the action would be accepted right now. */
    public boolean isLegal(GameState s, int player, Action action) {
        try {
            applyInPlace(s.copy(), player, action);
            return true;
        } catch (RuleViolationException e) {
            return false;
        }
    }

    private ActionResult applyInPlace(GameState s, int player, Action action) {
        if (s.phase == GameState.Phase.FINISHED) throw violation("The game is over.");
        if (player < 0 || player > 1) throw violation("Unknown player.");
        ActionResult r = new ActionResult();
        PlayerState me = s.player(player);
        PlayerState opp = s.player(1 - player);

        if (s.phase == GameState.Phase.SETUP) {
            if (!(action instanceof PlaceStartingTank place)) {
                throw violation("The game is still being set up: place your starting tank.");
            }
            placeStartingTank(s, me, place, r);
            return r;
        }
        if (s.activePlayer != player) throw violation("It's not your turn.");

        switch (action) {
            case PlaceStartingTank a -> throw violation("The game has already started.");
            case Designate a -> designate(s, me, a, r);
            case Deploy a -> deploy(s, me, a, r);
            case Convoy a -> convoy(me, a, r);
            case Repair a -> repair(me, a, r);
            case Reveal a -> reveal(me, a, r);
            case RevealGroup a -> revealGroup(me, a, r);
            case Retreat a -> retreat(me, a, r);
            case RetreatGroup a -> retreatGroup(me, a, r);
            case Move a -> move(s, me, a, r);
            case UseAbility a -> useAbility(me, opp, a, r);
            case PlayItem a -> playItem(s, me, opp, a, r);
            case SetJammer a -> setJammer(me, a, r);
            case Attack a -> attack(s, me, opp, a, r);
            case EndTurn a -> {
                r.say("Player " + (player + 1) + " ends their turn.");
                endTurn(s, r, false);
            }
        }
        return r;
    }

    // ── Setup action ─────────────────────────────────────────────────────────

    private void placeStartingTank(GameState s, PlayerState me, PlaceStartingTank a, ActionResult r) {
        if (me.placedStartingTank) throw violation("You have already placed your starting tank.");
        requireInHand(me, a.cardId());
        VehicleSpec spec = catalog.vehicle(a.cardId());
        if (!spec.isTank()) throw violation("Your starting vehicle must be a tank.");
        me.hand.remove(Long.valueOf(a.cardId()));
        newGroupOf(s, me, spec);
        me.placedStartingTank = true;
        r.say("Player " + (me.index + 1) + " places a vehicle face down.");
        if (s.players.stream().allMatch(p -> p.placedStartingTank)) {
            s.phase = GameState.Phase.PLAYING;
            s.activePlayer = s.firstPlayer;
            startTurn(s, r);
        }
    }

    // ── Main-step actions ────────────────────────────────────────────────────

    private void designate(GameState s, PlayerState me, Designate a, ActionResult r) {
        if (me.designationsLeft <= 0) throw violation("You have already played a resource card this turn.");
        requireInHand(me, a.cardId());
        ResourceSpec spec = catalog.resource(a.cardId());
        List<ResourceStack> destination;
        if (a.groupId() == null) {
            destination = me.depot;
        } else {
            if (spec.kind() != ResourceKind.AMMO && spec.kind() != ResourceKind.FUEL) {
                throw violation("Only Ammo and Fuel can go into a strike group's pool.");
            }
            destination = group(me, a.groupId()).pool;
        }
        me.hand.remove(Long.valueOf(a.cardId()));
        destination.add(new ResourceStack(s.nextId++, spec.cardId(), spec.kind(), spec.ammunition(), spec.amount()));
        me.designationsLeft--;
        r.say("Player " + (me.index + 1) + " plays " + spec.name() + (a.groupId() == null ? " to the Depot." : " to a pool."));
    }

    private void deploy(GameState s, PlayerState me, Deploy a, ActionResult r) {
        requireInHand(me, a.cardId());
        VehicleSpec spec = catalog.vehicle(a.cardId());
        if (a.groupId() == null) {
            requireCanStandAlone(me, spec);
            me.hand.remove(Long.valueOf(a.cardId()));
            newGroupOf(s, me, spec);
            r.say("Player " + (me.index + 1) + " deploys a tank face down.");
            return;
        }
        StrikeGroup g = group(me, a.groupId());
        requireRoom(g, spec);
        int cost = formationCost(g, spec);
        requireSupply(me, cost);
        me.hand.remove(Long.valueOf(a.cardId()));
        spend(me, me.depot, ResourceKind.SUPPLY, null, cost);
        g.vehicles.add(newVehicle(s, spec));
        if (g.vehicles.size() >= 2) g.formed = true;
        electLeader(g);
        r.say("Player " + (me.index + 1) + " deploys a vehicle into a strike group" + (cost > 0 ? " (formation cost " + cost + " Supply)." : "."));
    }

    private void convoy(PlayerState me, Convoy a, ActionResult r) {
        StrikeGroup g = group(me, a.groupId());
        Vehicle resupply = g.vehicles.stream()
                .filter(v -> catalog.vehicle(v.cardId).isResupply()).findFirst()
                .orElseThrow(() -> violation("That strike group has no Resupply vehicle."));
        int capacity = rules.convoyCapacity(catalog.vehicle(resupply.cardId).level());
        List<Long> ids = a.resourceIds() == null ? List.of() : a.resourceIds();
        if (ids.isEmpty()) throw violation("Choose at least one resource card to move.");
        if (new HashSet<>(ids).size() != ids.size()) throw violation("A resource card can only be moved once.");
        if (g.convoyMoved + ids.size() > capacity) {
            throw violation("This convoy can move " + capacity + " card(s) per turn; " + g.convoyMoved + " already moved.");
        }
        List<ResourceStack> moving = new ArrayList<>();
        for (long id : ids) {
            ResourceStack stack = me.depot.stream().filter(x -> x.id == id).findFirst()
                    .orElseThrow(() -> violation("Resource card " + id + " is not in your Depot."));
            if (stack.kind != ResourceKind.AMMO && stack.kind != ResourceKind.FUEL) {
                throw violation("Only Ammo and Fuel can be moved into a pool.");
            }
            moving.add(stack);
        }
        me.depot.removeAll(moving);
        g.pool.addAll(moving);
        g.convoyMoved += moving.size();
        r.say("The convoy moves " + moving.size() + " resource card(s) into the pool.");
    }

    private void repair(PlayerState me, Repair a, ActionResult r) {
        ResourceStack card = me.depot.stream().filter(x -> x.id == a.resourceId()).findFirst()
                .orElseThrow(() -> violation("That Repair card is not in your Depot."));
        if (card.kind != ResourceKind.REPAIR) throw violation("That is not a Repair card.");
        Vehicle v = findVehicle(me, a.vehicleId()).vehicle;
        ResourceSpec spec = catalog.resource(card.cardId);
        boolean full = spec.amount() >= rules.fullRepairThreshold;
        if (v.hp >= v.maxHp && v.breachStacks == 0) throw violation("That vehicle doesn't need repairs.");
        int healed = full ? v.maxHp - v.hp : Math.min(spec.amount(), v.maxHp - v.hp);
        v.hp += healed;
        if (full) v.breachStacks = 0;
        me.depot.remove(card);
        me.discard.add(card.cardId);
        r.say("Repairs restore " + healed + " HP" + (full ? " and remove BREACH." : "."));
    }

    private void reveal(PlayerState me, Reveal a, ActionResult r) {
        List<Long> ids = a.vehicleIds() == null ? List.of() : a.vehicleIds();
        if (ids.isEmpty()) throw violation("Choose a vehicle to reveal.");
        List<Vehicle> toFlip = new ArrayList<>();
        for (long id : ids) {
            Vehicle v = findVehicle(me, id).vehicle;
            if (catalog.vehicle(v.cardId).isResupply()) throw violation("A Resupply vehicle never needs to be revealed.");
            if (v.faceUp) throw violation("That vehicle is already face up.");
            if (!toFlip.contains(v)) toFlip.add(v);
        }
        toFlip.forEach(v -> v.faceUp = true);
        r.say("Player " + (me.index + 1) + " reveals " + toFlip.size() + " vehicle(s).");
    }

    private void revealGroup(PlayerState me, RevealGroup a, ActionResult r) {
        StrikeGroup g = group(me, a.groupId());
        List<Vehicle> toFlip = g.vehicles.stream()
                .filter(v -> !v.faceUp && !catalog.vehicle(v.cardId).isResupply()).toList();
        if (toFlip.isEmpty()) throw violation("Everything in that group is already face up.");
        toFlip.forEach(v -> v.faceUp = true);
        r.say("Player " + (me.index + 1) + " reveals a strike group.");
    }

    private void retreat(PlayerState me, Retreat a, ActionResult r) {
        Located loc = findVehicle(me, a.vehicleId());
        if (!loc.vehicle.faceUp) throw violation("That vehicle is already face down.");
        int cost = retreatCost(loc.vehicle);
        requireFuel(loc.group, cost);
        spend(me, loc.group.pool, ResourceKind.FUEL, null, cost);
        loc.vehicle.faceUp = false;
        r.say("A vehicle retreats face down (" + cost + " Fuel).");
    }

    private void retreatGroup(PlayerState me, RetreatGroup a, ActionResult r) {
        StrikeGroup g = group(me, a.groupId());
        if (g.vehicles.stream().noneMatch(v -> v.faceUp)) throw violation("Everything in that group is already face down.");
        int cost = retreatCost(g.leader()) * rules.groupRetreatMultiplier;
        requireFuel(g, cost);
        spend(me, g.pool, ResourceKind.FUEL, null, cost);
        g.vehicles.forEach(v -> v.faceUp = false);
        r.say("A strike group retreats face down (" + cost + " Fuel).");
    }

    private void move(GameState s, PlayerState me, Move a, ActionResult r) {
        Located loc = findVehicle(me, a.vehicleId());
        if (loc.group.leader() == loc.vehicle) throw violation("A Leader can't leave its strike group.");
        VehicleSpec spec = catalog.vehicle(loc.vehicle.cardId);
        StrikeGroup dest = null;
        if (a.toGroupId() == null) {
            requireCanStandAlone(me, spec);
        } else {
            dest = group(me, a.toGroupId());
            if (dest == loc.group) throw violation("That vehicle is already in that group.");
            requireRoom(dest, spec);
        }
        int fuel = rules.moveFuel(spec.level());
        requireFuel(loc.group, fuel);
        int supply = dest == null ? 0 : formationCost(dest, spec);
        requireSupply(me, supply);

        spend(me, loc.group.pool, ResourceKind.FUEL, null, fuel);
        spend(me, me.depot, ResourceKind.SUPPLY, null, supply);
        loc.group.vehicles.remove(loc.vehicle);
        if (dest == null) {
            StrikeGroup g = new StrikeGroup(s.nextId++);
            g.vehicles.add(loc.vehicle);
            me.groups.add(g);
        } else {
            dest.vehicles.add(loc.vehicle);
            if (dest.vehicles.size() >= 2) dest.formed = true;
            electLeader(dest);
        }
        r.say("A vehicle moves to another strike group (" + fuel + " Fuel).");
    }

    // ── Abilities ────────────────────────────────────────────────────────────

    private void useAbility(PlayerState me, PlayerState opp, UseAbility a, ActionResult r) {
        Located loc = findVehicle(me, a.vehicleId());
        VehicleSpec spec = catalog.vehicle(loc.vehicle.cardId);
        AbilitySpec ability = spec.ability();
        if (ability == null) throw violation(spec.name() + " has no ability.");
        if (!loc.vehicle.faceUp) throw violation(spec.name() + " has to be face up to use its ability.");
        if (loc.vehicle.stunned || loc.vehicle.disabled) throw violation(spec.name() + " can't use its ability this turn.");
        if (loc.vehicle.abilityUsed) throw violation(spec.name() + " has already used its ability this turn.");
        List<Long> ids = a.targetVehicleIds() == null ? List.of() : a.targetVehicleIds();
        if (ids.isEmpty()) throw violation("Choose which enemy vehicles to reveal.");
        if (new HashSet<>(ids).size() != ids.size()) throw violation("Each target can only be chosen once.");
        if (ids.size() > ability.power()) {
            throw violation(spec.name() + " can reveal at most " + ability.power() + " vehicle(s).");
        }
        List<Vehicle> targets = new ArrayList<>();
        for (long id : ids) {
            Located enemy = opp.groups.stream().flatMap(g -> g.vehicles.stream().map(v -> new Located(g, v)))
                    .filter(l -> l.vehicle.id == id).findFirst()
                    .orElseThrow(() -> violation("Enemy vehicle " + id + " was not found."));
            Vehicle t = enemy.vehicle;
            if (t.faceUp) throw violation("Enemy vehicle " + id + " is already face up.");
            if (enemy.group.jammed()) throw violation("Enemy vehicle " + id + " is in a jammed strike group.");
            targets.add(t);
        }
        requireFuel(loc.group, ability.fuelCost());
        spend(me, loc.group.pool, ResourceKind.FUEL, null, ability.fuelCost());
        targets.forEach(t -> t.faceUp = true);
        loc.vehicle.abilityUsed = true;
        r.say(spec.name() + " reveals " + targets.size() + " enemy vehicle(s).");
    }

    // ── Item cards ───────────────────────────────────────────────────────────

    private void playItem(GameState s, PlayerState me, PlayerState opp, PlayItem a, ActionResult r) {
        requireInHand(me, a.cardId());
        ItemSpec spec = catalog.item(a.cardId());
        List<Long> targets = a.targetIds() == null ? List.of() : a.targetIds();
        List<Long> chosen = a.cardIds() == null ? List.of() : a.cardIds();

        // check everything first; each check returns what to do once the card is paid for
        Runnable effect = switch (spec.effect()) {
            case ERA -> prepareEra(me, spec, targets, r);
            case ARTILLERY -> prepareArtillery(s, me, opp, spec, targets, r);
            case SEARCH -> prepareSearch(s, me, spec, chosen, r);
            case DRAW -> prepareDraw(me, spec, r);
            case SMOKE -> prepareSmoke(me, spec, targets, r);
            case JAMMER -> prepareJammer(s, me, spec, targets, r);
            case CAMO -> prepareCamo(me, spec, targets, r);
            case SABOTAGE -> prepareSabotage(s, me, opp, spec, r);
            case RECYCLE -> prepareRecycle(me, spec, chosen, r);
            case RAPID_DEPLOY -> prepareRapidDeploy(s, me, spec, targets, chosen, r);
            case AIRDROP -> prepareAirdrop(me, spec, targets, chosen, r);
        };
        int limit = rules.itemLimit(spec.effect());
        if (limit > 0 && me.itemUses.getOrDefault(spec.effect(), 0) >= limit) {
            throw violation("You can only play " + limit + " " + kindOfItem(spec) + " card" + (limit == 1 ? "" : "s") + " per turn.");
        }
        int supply = rules.itemSupply(spec.level());
        requireSupply(me, supply, "Playing " + spec.name());

        me.hand.remove(Long.valueOf(a.cardId()));
        spend(me, me.depot, ResourceKind.SUPPLY, null, supply);
        boolean attached = spec.effect() == ItemEffect.ERA || spec.effect() == ItemEffect.CAMO || spec.effect() == ItemEffect.JAMMER;
        if (!attached) me.discard.add(a.cardId());   // attached cards stay on the field
        if (limit > 0) me.itemUses.merge(spec.effect(), 1, Integer::sum);
        effect.run();
    }

    private Runnable prepareEra(PlayerState me, ItemSpec spec, List<Long> targets, ActionResult r) {
        if (targets.size() != 1) throw violation(spec.name() + " attaches to exactly one of your vehicles.");
        Vehicle v = findVehicle(me, targets.get(0)).vehicle;
        return () -> {
            if (v.eraCardId != 0) me.discard.add(v.eraCardId);
            v.eraCardId = spec.cardId();
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + " (-" + spec.power() + "% chemical damage).");
        };
    }

    private static String kindOfItem(ItemSpec spec) {
        return switch (spec.effect()) {
            case ARTILLERY -> "Artillery";
            case SABOTAGE -> "Sabotage";
            case AIRDROP -> "Airdrop";
            default -> spec.name();
        };
    }

    private Runnable prepareSmoke(PlayerState me, ItemSpec spec, List<Long> targets, ActionResult r) {
        if (targets.isEmpty()) throw violation("Choose which of your vehicles " + spec.name() + " covers.");
        if (new HashSet<>(targets).size() != targets.size()) throw violation("Each vehicle can only be chosen once.");
        if (targets.size() > spec.count()) throw violation(spec.name() + " covers at most " + spec.count() + " vehicle(s).");
        List<Vehicle> covered = targets.stream().map(id -> findVehicle(me, id).vehicle).toList();
        return () -> {
            covered.forEach(v -> v.smoked = true);
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + " over " + covered.size() + " vehicle(s).");
        };
    }

    private Runnable prepareJammer(GameState s, PlayerState me, ItemSpec spec, List<Long> targets, ActionResult r) {
        if (targets.size() != 1) throw violation(spec.name() + " attaches to exactly one of your strike groups.");
        StrikeGroup g = group(me, targets.get(0));
        return () -> {
            if (g.jammerCardId != 0) me.discard.add(g.jammerCardId);
            g.jammerCardId = spec.cardId();
            g.jammerId = s.nextId++;
            g.jammerHp = spec.count();
            g.jammerMaxHp = spec.count();
            g.jammerOn = false;                          // it starts switched off
            r.say("Player " + (me.index + 1) + " attaches " + spec.name() + " to a strike group.");
        };
    }

    private void setJammer(PlayerState me, SetJammer a, ActionResult r) {
        StrikeGroup g = group(me, a.groupId());
        if (g.jammerCardId == 0) throw violation("That strike group has no Jammer.");
        if (g.jammerOn == a.on()) throw violation("That Jammer is already " + (a.on() ? "on." : "off."));
        g.jammerOn = a.on();
        r.say("Player " + (me.index + 1) + (a.on() ? " switches a Jammer on." : " switches a Jammer off."));
    }

    /** The Jammer is destroyed: the jamming ends (the group is not revealed, but abilities can reveal it again). */
    private void destroyJammer(PlayerState owner, StrikeGroup g, ActionResult r) {
        owner.discard.add(g.jammerCardId);
        g.jammerCardId = 0;
        g.jammerId = 0;
        g.jammerHp = 0;
        g.jammerMaxHp = 0;
        g.jammerOn = false;
        r.say("A Jammer is destroyed.");
    }

    private Runnable prepareCamo(PlayerState me, ItemSpec spec, List<Long> targets, ActionResult r) {
        if (targets.size() != 1) throw violation(spec.name() + " attaches to exactly one of your vehicles.");
        Vehicle v = findVehicle(me, targets.get(0)).vehicle;
        return () -> {
            if (v.camoCardId != 0) me.discard.add(v.camoCardId);
            v.camoCardId = spec.cardId();
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + " (retreating costs " + spec.power() + " less Fuel).");
        };
    }

    private Runnable prepareSabotage(GameState s, PlayerState me, PlayerState opp, ItemSpec spec, ActionResult r) {
        if (opp.hand.isEmpty()) throw violation("Your opponent has no cards in hand.");
        return () -> {
            Random rnd = nextRandom(s);
            List<String> names = new ArrayList<>();
            for (int i = 0; i < spec.count() && !opp.hand.isEmpty(); i++) {
                long gone = opp.hand.remove(rnd.nextInt(opp.hand.size()));
                opp.discard.add(gone);
                names.add(catalog.spec(gone).name());
            }
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + ": player " + (opp.index + 1) + " discards " + String.join(", ", names) + ".");
        };
    }

    private Runnable prepareRecycle(PlayerState me, ItemSpec spec, List<Long> chosen, ActionResult r) {
        if (chosen.size() > spec.count()) throw violation(spec.name() + " returns at most " + spec.count() + " card(s).");
        Map<Long, Integer> wanted = new HashMap<>();
        for (long id : chosen) {
            if (!(catalog.find(id) instanceof ResourceSpec)) throw violation(spec.name() + " only returns resource cards.");
            if (wanted.merge(id, 1, Integer::sum) > Collections.frequency(me.discard, id)) {
                throw violation("Your discard pile doesn't have that many copies of card " + id + ".");
            }
        }
        return () -> {
            List<String> names = new ArrayList<>();
            for (long id : chosen) {
                me.discard.remove(Long.valueOf(id));
                me.hand.add(id);
                names.add(catalog.spec(id).name());
            }
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + " and returns " + (names.isEmpty() ? "nothing." : String.join(", ", names) + "."));
        };
    }

    /** {@code chosen} are the ids of resource cards in your Depot (as shown in the game view), not card ids. */
    private Runnable prepareAirdrop(PlayerState me, ItemSpec spec, List<Long> targets, List<Long> chosen, ActionResult r) {
        if (targets.size() != 1) throw violation(spec.name() + " drops into exactly one of your strike groups.");
        StrikeGroup g = group(me, targets.get(0));
        if (chosen.isEmpty()) throw violation("Choose which resource cards from your Depot to drop.");
        if (chosen.size() > spec.count()) throw violation(spec.name() + " drops at most " + spec.count() + " card(s).");
        if (new HashSet<>(chosen).size() != chosen.size()) throw violation("A resource card can only be moved once.");
        List<ResourceStack> moving = new ArrayList<>();
        for (long id : chosen) {
            ResourceStack stack = me.depot.stream().filter(x -> x.id == id).findFirst()
                    .orElseThrow(() -> violation("Resource card " + id + " is not in your Depot."));
            if (stack.kind != ResourceKind.AMMO && stack.kind != ResourceKind.FUEL) {
                throw violation("Only Ammo and Fuel can be moved into a pool.");
            }
            moving.add(stack);
        }
        return () -> {
            me.depot.removeAll(moving);
            g.pool.addAll(moving);
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + " and drops " + moving.size() + " resource card(s) into a pool.");
        };
    }

    private Runnable prepareRapidDeploy(GameState s, PlayerState me, ItemSpec spec, List<Long> targets, List<Long> chosen, ActionResult r) {
        if (targets.size() != 1) throw violation(spec.name() + " deploys into exactly one of your strike groups.");
        StrikeGroup g = group(me, targets.get(0));
        if (chosen.isEmpty()) throw violation("Choose which vehicles from your hand to deploy.");
        if (chosen.size() > spec.count()) throw violation(spec.name() + " deploys at most " + spec.count() + " vehicle(s).");
        Map<Long, Integer> wanted = new HashMap<>();
        StrikeGroup sim = g.copy();                          // try the slot rules on a copy, one vehicle at a time
        for (long id : chosen) {
            requireInHand(me, id);
            if (wanted.merge(id, 1, Integer::sum) > Collections.frequency(me.hand, id)) {
                throw violation("You don't have that many copies of card " + id + " in your hand.");
            }
            VehicleSpec vs = catalog.vehicle(id);
            requireRoom(sim, vs);
            sim.vehicles.add(new Vehicle(-1, id, vs.hp()));
            electLeader(sim);
        }
        return () -> {
            for (long id : chosen) {
                me.hand.remove(Long.valueOf(id));
                g.vehicles.add(newVehicle(s, catalog.vehicle(id)));
            }
            if (g.vehicles.size() >= 2) g.formed = true;
            electLeader(g);
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + " and deploys " + chosen.size() + " vehicle(s) with no formation cost.");
        };
    }

    private boolean artilleryHitsHidden(ItemSpec spec) {
        return spec.level().compareTo(rules.artilleryBlindFrom) >= 0;
    }

    private Runnable prepareArtillery(GameState s, PlayerState me, PlayerState opp, ItemSpec spec, List<Long> targets, ActionResult r) {
        if (targets.isEmpty()) throw violation("Choose which enemy vehicles " + spec.name() + " hits.");
        if (new HashSet<>(targets).size() != targets.size()) throw violation("Each target can only be chosen once.");
        if (targets.size() > spec.count()) throw violation(spec.name() + " can hit at most " + spec.count() + " vehicle(s).");
        for (long id : targets) {
            Vehicle t = opp.groups.stream().flatMap(g -> g.vehicles.stream()).filter(v -> v.id == id).findFirst()
                    .orElseThrow(() -> violation("Enemy vehicle " + id + " was not found."));
            if (!t.faceUp && !artilleryHitsHidden(spec)) {
                throw violation(spec.name() + " can only hit face-up vehicles.");
            }
            if (t.faceUp && catalog.vehicle(t.cardId).air()) {
                throw violation(catalog.vehicle(t.cardId).name() + " is an aircraft: Artillery can't hit it.");
            }
            if (t.smoked) throw violation("Enemy vehicle " + id + " is hidden in smoke and can't be targeted.");
        }
        return () -> {
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + ".");
            for (long id : targets) {
                Vehicle t = opp.groups.stream().flatMap(g -> g.vehicles.stream()).filter(v -> v.id == id).findFirst().orElse(null);
                if (t == null || t.smoked || (!t.faceUp && !artilleryHitsHidden(spec))) continue;    // gone, or gone face down, since the first hit
                if (catalog.vehicle(t.cardId).air()) {                // a hidden aircraft: the shell finds nothing, and nothing is revealed
                    r.say(spec.name() + " finds nothing to hit at one of its targets.");
                    continue;
                }
                t.faceUp = true;
                int base = spec.power();
                SpecialEffect effect = SpecialEffect.NONE;
                String kind = "true damage";
                if (rules.artilleryDamageType != null) {            // worked out with the normal armor rules
                    DamageCalculator.Result dmg = DamageCalculator.calculate(base, SpecialEffect.NONE, rules.artilleryDamageType,
                            rules.artilleryCaliber, catalog.vehicle(t.cardId).armor(), t.breachStacks);
                    base = dmg.damage();
                    effect = dmg.effect();
                    kind = rules.artilleryDamageType.name().toLowerCase() + (dmg.trueDamage() ? ", full" : "");
                }
                int dealt = rules.damagePercent == 100 ? base : Math.max(1, Math.round(base * rules.damagePercent / 100f));
                t.hp -= dealt;
                applyEffect(t, effect);
                r.say(spec.name() + " hits " + catalog.vehicle(t.cardId).name() + " for " + dealt + " (" + kind
                        + (effect != SpecialEffect.NONE && effect != SpecialEffect.OVERPRESSURE ? ", " + effect : "") + ").");
                if (t.hp <= 0) destroy(s, opp, me, t, r);
                if (s.phase == GameState.Phase.FINISHED) return;
            }
        };
    }

    private Runnable prepareSearch(GameState s, PlayerState me, ItemSpec spec, List<Long> chosen, ActionResult r) {
        if (chosen.size() > spec.count()) throw violation(spec.name() + " finds at most " + spec.count() + " card(s).");
        Map<Long, Integer> wanted = new HashMap<>();
        for (long id : chosen) {
            if (!matchesKind(catalog.find(id), spec.searchKind())) {
                throw violation(spec.name() + " only finds " + kindName(spec.searchKind()) + ".");
            }
            if (wanted.merge(id, 1, Integer::sum) > Collections.frequency(me.deck, id)) {
                throw violation("Your deck doesn't have that many copies of card " + id + ".");
            }
        }
        return () -> {
            List<String> names = new ArrayList<>();
            for (long id : chosen) {
                me.deck.remove(Long.valueOf(id));
                me.hand.add(id);
                names.add(catalog.spec(id).name());
            }
            Collections.shuffle(me.deck, nextRandom(s));
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + " and finds "
                    + (names.isEmpty() ? "nothing." : String.join(", ", names) + "."));
        };
    }

    private Runnable prepareDraw(PlayerState me, ItemSpec spec, ActionResult r) {
        if (me.deck.size() < spec.count()) {
            throw violation(spec.name() + " needs at least " + spec.count() + " cards left in your deck.");
        }
        return () -> {
            draw(me, spec.count());
            r.say("Player " + (me.index + 1) + " plays " + spec.name() + " and draws " + spec.count() + ".");
        };
    }

    private static boolean matchesKind(CardSpec spec, SearchKind kind) {
        return switch (kind) {
            case RESOURCE -> spec instanceof ResourceSpec;
            case TANK -> spec instanceof VehicleSpec v && v.isTank();
            case SUPPORT -> spec instanceof VehicleSpec v && !v.isTank();
        };
    }

    private static String kindName(SearchKind kind) {
        return switch (kind) {
            case RESOURCE -> "resource cards";
            case TANK -> "tanks";
            case SUPPORT -> "support vehicles";
        };
    }

    /** Fuel to retreat a vehicle: by rarity, less any Camouflage (never below 0). */
    private int retreatCost(Vehicle v) {
        int base = rules.retreatFuel(catalog.vehicle(v.cardId).level());
        return v.camoCardId == 0 ? base : Math.max(0, base - catalog.item(v.camoCardId).power());
    }

    private static void discardAttachments(PlayerState owner, Vehicle v) {
        if (v.eraCardId != 0) owner.discard.add(v.eraCardId);
        if (v.camoCardId != 0) owner.discard.add(v.camoCardId);
    }

    private Random nextRandom(GameState s) {
        Random rnd = new Random(s.rngSeed);
        s.rngSeed = rnd.nextLong();
        return rnd;
    }

    private int eraPercent(Vehicle v) {
        return v.eraCardId == 0 ? 0 : catalog.item(v.eraCardId).power();
    }

    // ── Attacking ────────────────────────────────────────────────────────────

    /** {@code target} is the enemy vehicle hit, or null when {@code jammerOf} (the enemy group whose Jammer is being shot) is set. */
    private record Plan(Vehicle attacker, VehicleSpec spec, AttackSpec attack, Ammunition ammo, Vehicle target, StrikeGroup jammerOf) {}

    private void attack(GameState s, PlayerState me, PlayerState opp, Attack a, ActionResult r) {
        if (s.firstPlayer == me.index && me.turnsTaken <= 1) {
            throw violation("The first player can't attack on their first turn.");
        }
        StrikeGroup g = group(me, a.groupId());
        if (a.choices() == null || a.choices().isEmpty()) throw violation("Choose at least one vehicle to attack with.");

        Set<Long> used = new HashSet<>();
        List<Plan> plans = new ArrayList<>();
        for (AttackChoice c : a.choices()) {
            Vehicle attacker = g.vehicles.stream().filter(v -> v.id == c.vehicleId()).findFirst()
                    .orElseThrow(() -> violation("Vehicle " + c.vehicleId() + " is not in that strike group."));
            if (!used.add(attacker.id)) throw violation("A vehicle can only attack once per turn.");
            VehicleSpec spec = catalog.vehicle(attacker.cardId);
            if (spec.isResupply()) throw violation("A Resupply vehicle never attacks.");
            if (!attacker.faceUp) throw violation(spec.name() + " is face down and can't attack.");
            if (attacker.stunned) throw violation(spec.name() + " is stunned and can't attack this turn.");
            if (attacker.disabled) throw violation(spec.name() + " is disabled and can't attack this turn.");
            if (attacker.smoked) throw violation(spec.name() + " is in its own smoke and can't attack this turn.");
            AttackSpec attack = spec.attack(c.slot())
                    .orElseThrow(() -> violation(spec.name() + " has no " + c.slot() + " attack."));
            if (attacker.suppressed && c.slot() == AttackSlot.ATTACK_1) {
                throw violation(spec.name() + " is suppressed and can't use ATTACK_1 this turn.");
            }
            List<Ammunition> compatible = attack.weapon().getCompatibleAmmunition();
            Ammunition ammo = c.ammo();
            if (ammo == null) {
                if (compatible.size() != 1) throw violation("Choose which ammunition " + attack.name() + " fires.");
                ammo = compatible.get(0);
            } else if (!compatible.contains(ammo)) {
                throw violation(ammo + " doesn't fit the weapon used by " + attack.name() + ".");
            }
            Vehicle target = opp.groups.stream().flatMap(x -> x.vehicles.stream())
                    .filter(v -> v.id == c.targetVehicleId()).findFirst().orElse(null);
            if (target == null) {                               // not a vehicle: maybe a Jammer that is switched on
                StrikeGroup jammed = opp.groups.stream()
                        .filter(x -> x.jammerCardId != 0 && x.jammerOn && x.jammerId == c.targetVehicleId()).findFirst()
                        .orElseThrow(() -> violation("Target " + c.targetVehicleId() + " was not found."));
                plans.add(new Plan(attacker, spec, attack, ammo, null, jammed));
                continue;
            }
            if (!target.faceUp) throw violation("You can only attack face-up vehicles.");
            if (target.smoked) throw violation("That vehicle is hidden in smoke and can't be targeted.");
            plans.add(new Plan(attacker, spec, attack, ammo, target, null));
        }
        if (plans.size() >= 2 && !g.leader().faceUp) {
            throw violation("A Combined Assault needs the Leader to be face up.");
        }

        // Add up what the attacks cost and check the pool has it.
        Map<Ammunition, Integer> ammoNeeded = new EnumMap<>(Ammunition.class);
        int fuelNeeded = 0;
        for (Plan p : plans) {
            ammoNeeded.merge(p.ammo, p.attack.ammoCost(), Integer::sum);
            fuelNeeded += p.attack.fuelCost();
        }
        for (var e : ammoNeeded.entrySet()) {
            int have = available(g.pool, ResourceKind.AMMO, e.getKey());
            if (have < e.getValue()) {
                throw violation("Not enough " + e.getKey() + " in the pool: need " + e.getValue() + ", have " + have + ".");
            }
        }
        requireFuel(g, fuelNeeded);

        for (var e : ammoNeeded.entrySet()) spend(me, g.pool, ResourceKind.AMMO, e.getKey(), e.getValue());
        spend(me, g.pool, ResourceKind.FUEL, null, fuelNeeded);

        r.say("Player " + (me.index + 1) + (plans.size() >= 2 ? " launches a Combined Assault." : " makes a Skirmish attack."));
        for (Plan p : plans) {
            if (p.jammerOf != null) {
                shootJammer(opp, p, r);
                continue;
            }
            if (p.target.hp <= 0) {
                r.say(p.spec.name() + "'s target was already destroyed.");
                continue;
            }
            DamageCalculator.Result dmg = DamageCalculator.calculate(p.attack.baseDamage(), p.attack.effect(),
                    p.ammo, catalog.vehicle(p.target.cardId).armor(), p.target.breachStacks);
            int dealt = rules.damagePercent == 100 ? dmg.damage() : Math.max(1, Math.round(dmg.damage() * rules.damagePercent / 100f));
            int era = eraPercent(p.target);
            boolean eraHelped = era > 0 && p.ammo.getDamageType() == DamageType.CHEMICAL;
            if (eraHelped) dealt = Math.max(1, Math.round(dealt * (100 - era) / 100f));
            p.target.hp -= dealt;
            applyEffect(p.target, dmg.effect());
            r.say(p.spec.name() + " hits " + catalog.vehicle(p.target.cardId).name() + " for " + dealt
                    + (dmg.effect() != SpecialEffect.NONE ? " (" + dmg.effect() + ")" : "") + (eraHelped ? " (ERA -" + era + "%)" : "") + ".");
            if (p.target.hp <= 0) destroy(s, opp, me, p.target, r);
            if (s.phase == GameState.Phase.FINISHED) return;
        }
        endTurn(s, r, true);
    }

    /** An attack on a Jammer: it has no armor and takes the weapon's damage; at 0 HP it is destroyed. */
    private void shootJammer(PlayerState owner, Plan p, ActionResult r) {
        StrikeGroup g = p.jammerOf;
        if (g.jammerCardId == 0) {
            r.say(p.spec.name() + "'s target was already destroyed.");
            return;
        }
        DamageCalculator.Result dmg = DamageCalculator.calculate(p.attack.baseDamage(), p.attack.effect(), p.ammo, 0, 0);
        int dealt = rules.damagePercent == 100 ? dmg.damage() : Math.max(1, Math.round(dmg.damage() * rules.damagePercent / 100f));
        g.jammerHp -= dealt;
        r.say(p.spec.name() + " hits a Jammer for " + dealt + ".");
        if (g.jammerHp <= 0) destroyJammer(owner, g, r);
    }

    private static void applyEffect(Vehicle target, SpecialEffect effect) {
        switch (effect) {
            case SUPPRESSION -> target.suppressed = true;
            case DISABLE -> target.disabled = true;
            case STUN -> target.stunned = true;
            case BREACH -> target.breachStacks = Math.min(target.breachStacks + 1, 3);
            default -> { /* NONE, PIERCE, OVERPRESSURE: nothing lasting */ }
        }
    }

    // ── Destruction ──────────────────────────────────────────────────────────

    private void destroy(GameState s, PlayerState owner, PlayerState attacker, Vehicle target, ActionResult r) {
        Located loc = findVehicle(owner, target.id);
        StrikeGroup g = loc.group;
        if (g.leader() != target) {
            g.vehicles.remove(target);
            owner.discard.add(target.cardId);
            discardAttachments(owner, target);
            r.say(catalog.vehicle(target.cardId).name() + " is destroyed.");
            return;
        }

        // The Leader is destroyed: the whole strike group is.
        int size = g.vehicles.size();
        int chips = 1 + (size >= rules.bonusChipMinVehicles ? rules.bonusChips : 0);
        attacker.chips += chips;
        r.say("The strike group is destroyed! Player " + (attacker.index + 1) + " takes " + chips + " Territory Chip(s).");

        int lost = size == 1 ? g.pool.size() : (g.pool.size() + 1) / 2;
        List<ResourceStack> byValue = new ArrayList<>(g.pool);
        byValue.sort(Comparator.comparingInt((ResourceStack x) -> x.remaining)); // the owner keeps their fullest cards
        for (int i = 0; i < lost; i++) {
            ResourceStack gone = byValue.get(i);
            g.pool.remove(gone);
            owner.discard.add(gone.cardId);
        }
        owner.depot.addAll(g.pool);
        g.pool.clear();

        owner.discard.add(target.cardId);
        discardAttachments(owner, target);
        if (g.jammerCardId != 0) owner.discard.add(g.jammerCardId);
        owner.groups.remove(g);
        for (Vehicle survivor : g.vehicles) {
            if (survivor == target) continue;
            if (catalog.vehicle(survivor.cardId).isTank()) {
                survivor.faceUp = false;
                StrikeGroup lone = new StrikeGroup(s.nextId++);
                lone.vehicles.add(survivor);
                owner.groups.add(lone);
            } else {
                owner.hand.add(survivor.cardId);
                if (survivor.eraCardId != 0) owner.hand.add(survivor.eraCardId);
                if (survivor.camoCardId != 0) owner.hand.add(survivor.camoCardId);
            }
        }
        if (attacker.chips >= rules.winChips) {
            finish(s, attacker.index, "Player " + (attacker.index + 1) + " took " + attacker.chips + " Territory Chips.", r);
        }
    }

    // ── Turn flow ────────────────────────────────────────────────────────────

    private void endTurn(GameState s, ActionResult r, boolean attacked) {
        s.passesInARow = attacked ? 0 : s.passesInARow + 1;
        PlayerState ending = s.player(s.activePlayer);
        for (StrikeGroup g : ending.groups) {
            for (Vehicle v : g.vehicles) {
                v.stunned = false;
                v.suppressed = false;
                v.disabled = false;
            }
        }
        if (rules.stalemateRounds > 0 && s.passesInARow >= 2 * rules.stalemateRounds) {
            s.passesInARow = 0;
            for (PlayerState p : s.players) {
                for (StrikeGroup g : p.groups) {
                    for (Vehicle v : g.vehicles) if (!catalog.vehicle(v.cardId).isResupply()) v.faceUp = true;
                }
            }
            r.say("Stalemate: every vehicle is revealed.");
        }
        s.activePlayer = 1 - s.activePlayer;
        startTurn(s, r);
    }

    private void startTurn(GameState s, ActionResult r) {
        PlayerState p = s.player(s.activePlayer);
        s.turnCount++;
        p.turnsTaken++;
        p.designationsLeft = rules.designationsPerTurn;
        p.itemUses.clear();
        for (StrikeGroup g : p.groups) {
            g.convoyMoved = 0;
            g.vehicles.forEach(v -> {
                v.abilityUsed = false;
                v.smoked = false;                       // a Smoke Screen lasts until its owner's next turn starts
            });
            if (g.jammerCardId != 0 && g.jammerOn) {    // a Jammer that is on costs Fuel from the group's pool each turn, or switches itself off
                int upkeep = catalog.item(g.jammerCardId).power();
                if (available(g.pool, ResourceKind.FUEL, null) >= upkeep) {
                    spend(p, g.pool, ResourceKind.FUEL, null, upkeep);
                } else {
                    g.jammerOn = false;
                    r.say("A Jammer switches off: its group couldn't pay the upkeep.");
                }
            }
        }
        if (p.deck.isEmpty()) {
            finish(s, 1 - p.index, "Player " + (p.index + 1) + " had no card to draw.", r);
            return;
        }
        p.hand.add(p.deck.remove(0));
        if (rules.loseWithNoForces && p.groups.isEmpty() && !hasTank(p.hand)) {
            finish(s, 1 - p.index, "Player " + (p.index + 1) + " has no strike group on the field and no tank in hand to start one.", r);
        }
    }

    private void finish(GameState s, int winner, String reason, ActionResult r) {
        s.phase = GameState.Phase.FINISHED;
        s.winner = winner;
        s.endReason = reason;
        r.say("Game over: player " + (winner + 1) + " wins. " + reason);
    }

    // ── Legal actions (for bots and tests) ───────────────────────────────────

    /** Every action the player may take right now. Enough for a bot to play a full game. */
    public List<Action> legalActions(GameState s, int player) {
        List<Action> out = new ArrayList<>();
        for (Action candidate : candidates(s, player)) {
            if (isLegal(s, player, candidate)) out.add(candidate);
        }
        return out;
    }

    private List<Action> candidates(GameState s, int player) {
        List<Action> c = new ArrayList<>();
        PlayerState me = s.player(player);
        PlayerState opp = s.player(1 - player);
        if (s.phase == GameState.Phase.SETUP) {
            for (long id : new LinkedHashSet<>(me.hand)) c.add(new PlaceStartingTank(id));
            return c;
        }
        if (s.phase != GameState.Phase.PLAYING || s.activePlayer != player) return c;
        c.add(new EndTurn());

        for (long id : new LinkedHashSet<>(me.hand)) {
            CardSpec spec = catalog.find(id);
            if (spec instanceof ResourceSpec) {
                c.add(new Designate(id, null));
                for (StrikeGroup g : me.groups) c.add(new Designate(id, g.id));
            } else if (spec instanceof VehicleSpec) {
                c.add(new Deploy(id, null));
                for (StrikeGroup g : me.groups) c.add(new Deploy(id, g.id));
            } else if (spec instanceof ItemSpec item) {
                itemCandidates(c, me, opp, id, item);
            }
        }
        for (ResourceStack stack : me.depot) {
            if (stack.kind == ResourceKind.REPAIR) {
                for (StrikeGroup g : me.groups) for (Vehicle v : g.vehicles) c.add(new Repair(stack.id, v.id));
            }
        }
        for (StrikeGroup g : me.groups) {
            if (g.jammerCardId != 0) c.add(new SetJammer(g.id, !g.jammerOn));
            c.add(new RevealGroup(g.id));
            c.add(new RetreatGroup(g.id));
            for (ResourceStack stack : me.depot) c.add(new Convoy(g.id, List.of(stack.id)));
            for (Vehicle v : g.vehicles) {
                c.add(new Reveal(List.of(v.id)));
                c.add(new Retreat(v.id));
                c.add(new Move(v.id, null));
                for (StrikeGroup other : me.groups) if (other != g) c.add(new Move(v.id, other.id));
            }
        }
        List<Long> targetIds = new ArrayList<>(opp.groups.stream().flatMap(g -> g.vehicles.stream())
                .filter(v -> v.faceUp).map(v -> v.id).toList());
        opp.groups.stream().filter(g -> g.jammerCardId != 0 && g.jammerOn).forEach(g -> targetIds.add(g.jammerId));   // a Jammer that is on can be shot
        List<Vehicle> hidden = opp.groups.stream().flatMap(g -> g.vehicles.stream()).filter(v -> !v.faceUp).toList();
        for (StrikeGroup g : me.groups) {
            for (Vehicle v : g.vehicles) {
                AbilitySpec ab = catalog.vehicle(v.cardId).ability();
                if (ab == null || hidden.isEmpty()) continue;
                c.add(new UseAbility(v.id, hidden.stream().limit(ab.power()).map(x -> x.id).toList()));
                c.add(new UseAbility(v.id, List.of(hidden.get(hidden.size() - 1).id)));
            }
        }
        for (StrikeGroup g : me.groups) {
            List<AttackChoice> firstChoices = new ArrayList<>();
            for (Vehicle v : g.vehicles) {
                VehicleSpec spec = catalog.vehicle(v.cardId);
                boolean firstFound = false;
                for (AttackSpec at : spec.attacks()) {
                    for (Ammunition ammo : at.weapon().getCompatibleAmmunition()) {
                        for (long targetId : targetIds) {
                            AttackChoice choice = new AttackChoice(v.id, at.slot(), ammo, targetId);
                            c.add(new Attack(g.id, List.of(choice)));
                            if (!firstFound) {
                                firstChoices.add(choice);
                                firstFound = true;
                            }
                        }
                    }
                }
            }
            if (firstChoices.size() >= 2) c.add(new Attack(g.id, firstChoices));
        }
        return c;
    }

    private void itemCandidates(List<Action> c, PlayerState me, PlayerState opp, long id, ItemSpec item) {
        switch (item.effect()) {
            case DRAW -> c.add(new PlayItem(id, List.of(), List.of()));
            case SEARCH -> {
                List<Long> found = new ArrayList<>();
                for (long deckId : me.deck) {
                    if (found.size() >= item.count()) break;
                    if (matchesKind(catalog.find(deckId), item.searchKind())) found.add(deckId);
                }
                c.add(new PlayItem(id, List.of(), found));
            }
            case ERA, CAMO, SMOKE -> me.groups.forEach(g -> g.vehicles.forEach(v -> c.add(new PlayItem(id, List.of(v.id), List.of()))));
            case JAMMER -> me.groups.forEach(g -> c.add(new PlayItem(id, List.of(g.id), List.of())));
            case SABOTAGE -> c.add(new PlayItem(id, List.of(), List.of()));
            case RECYCLE -> c.add(new PlayItem(id, List.of(), me.discard.stream().filter(x -> catalog.find(x) instanceof ResourceSpec)
                    .limit(item.count()).toList()));
            case AIRDROP -> {
                List<Long> movable = me.depot.stream().filter(x -> x.kind == ResourceKind.AMMO || x.kind == ResourceKind.FUEL)
                        .map(x -> x.id).limit(item.count()).toList();
                if (!movable.isEmpty()) me.groups.forEach(g -> c.add(new PlayItem(id, List.of(g.id), movable)));
            }
            case RAPID_DEPLOY -> {
                for (StrikeGroup g : me.groups) {
                    for (long handId : new LinkedHashSet<>(me.hand)) {
                        if (catalog.find(handId) instanceof VehicleSpec) c.add(new PlayItem(id, List.of(g.id), List.of(handId)));
                    }
                }
            }
            case ARTILLERY -> {
                List<Long> hittable = opp.groups.stream().flatMap(g -> g.vehicles.stream())
                        .filter(v -> v.faceUp ? !catalog.vehicle(v.cardId).air() : artilleryHitsHidden(item)).map(v -> v.id).toList();
                hittable.forEach(t -> c.add(new PlayItem(id, List.of(t), List.of())));
                if (hittable.size() >= 2) c.add(new PlayItem(id, hittable.stream().limit(item.count()).toList(), List.of()));
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private record Located(StrikeGroup group, Vehicle vehicle) {}

    private Located findVehicle(PlayerState p, long vehicleId) {
        for (StrikeGroup g : p.groups) {
            for (Vehicle v : g.vehicles) if (v.id == vehicleId) return new Located(g, v);
        }
        throw violation("You don't have a vehicle " + vehicleId + " on the field.");
    }

    private StrikeGroup group(PlayerState p, long groupId) {
        return p.groups.stream().filter(g -> g.id == groupId).findFirst()
                .orElseThrow(() -> violation("You don't have a strike group " + groupId + "."));
    }

    private void requireInHand(PlayerState p, long cardId) {
        if (!p.hand.contains(cardId)) throw violation("Card " + cardId + " is not in your hand.");
    }

    private int groupLimit(PlayerState p) {
        return rules.baseGroupLimit + p.chips / rules.chipsPerExtraGroup;
    }

    private void requireCanStandAlone(PlayerState me, VehicleSpec spec) {
        if (!spec.isTank()) throw violation(spec.name() + " isn't a tank, so it has to join an existing strike group.");
        if (me.groups.size() >= groupLimit(me)) {
            throw violation("You already have " + me.groups.size() + " strike groups (your limit is " + groupLimit(me) + ").");
        }
    }

    /** Slot rules from the rulebook: Leader + up to 2 Line + 1 Specialist + 1 Resupply, 5 vehicles at most. */
    private void requireRoom(StrikeGroup g, VehicleSpec spec) {
        if (g.vehicles.size() >= rules.maxGroupSize) throw violation("That strike group is full.");
        int specialists = 0, resupply = 0, line = 0;
        for (int i = 1; i < g.vehicles.size(); i++) {
            VehicleSpec other = catalog.vehicle(g.vehicles.get(i).cardId);
            if (other.isSpecialist()) specialists++;
            else if (other.isResupply()) resupply++;
            else line++;
        }
        if (spec.isSpecialist() && specialists >= 1) throw violation("That group already has a Specialist.");
        if (spec.isResupply() && resupply >= 1) throw violation("That group already has a Resupply vehicle.");
        if (!spec.isSpecialist() && !spec.isResupply() && line >= rules.maxLineVehicles) {
            throw violation("That group's Line slots are full.");
        }
    }

    /**
     * Supply needed to turn a lone tank into a multi-vehicle group (0 once it has been formed). It is based on the
     * Leader the group will have, so adding a better tank costs more than adding a worse one.
     */
    private int formationCost(StrikeGroup g, VehicleSpec joining) {
        if (g.formed || g.vehicles.size() != 1) return 0;
        com.example.valtrak.Data.CardLibrary.CardLevel level = catalog.vehicle(g.leader().cardId).level();
        if (joining.isTank() && joining.level().compareTo(level) > 0) level = joining.level();
        return rules.formationSupply(level);
    }

    /**
     * The Leader is the highest-rarity tank in the group; on a tie, the one that has been in the group longest
     * (it stays first in the list). Moves that tank to the front.
     */
    private void electLeader(StrikeGroup g) {
        int best = 0;
        VehicleSpec bestSpec = catalog.vehicle(g.vehicles.get(0).cardId);
        for (int i = 1; i < g.vehicles.size(); i++) {
            VehicleSpec spec = catalog.vehicle(g.vehicles.get(i).cardId);
            if (spec.isTank() && spec.level().compareTo(bestSpec.level()) > 0) {
                best = i;
                bestSpec = spec;
            }
        }
        if (best != 0) g.vehicles.add(0, g.vehicles.remove(best));
    }

    private void requireSupply(PlayerState me, int amount) {
        requireSupply(me, amount, "Forming a strike group");
    }

    private void requireSupply(PlayerState me, int amount, String what) {
        int have = available(me.depot, ResourceKind.SUPPLY, null);
        if (have < amount) {
            throw violation(what + " needs " + amount + " Supply in your Depot (you have " + have + ").");
        }
    }

    private void requireFuel(StrikeGroup g, int amount) {
        int have = available(g.pool, ResourceKind.FUEL, null);
        if (have < amount) throw violation("Not enough Fuel in the pool: need " + amount + ", have " + have + ".");
    }

    private static int available(List<ResourceStack> stacks, ResourceKind kind, Ammunition ammo) {
        int total = 0;
        for (ResourceStack x : stacks) if (matches(x, kind, ammo)) total += x.remaining;
        return total;
    }

    private static boolean matches(ResourceStack x, ResourceKind kind, Ammunition ammo) {
        return x.kind == kind && (ammo == null || x.ammunition == ammo);
    }

    /** Spends {@code amount}, using up the smallest cards first. Cards with nothing left go to the discard pile. */
    private static void spend(PlayerState owner, List<ResourceStack> stacks, ResourceKind kind, Ammunition ammo, int amount) {
        if (amount <= 0) return;
        List<ResourceStack> candidates = new ArrayList<>();
        for (ResourceStack x : stacks) if (matches(x, kind, ammo)) candidates.add(x);
        candidates.sort(Comparator.comparingInt((ResourceStack x) -> x.remaining));
        int need = amount;
        for (ResourceStack x : candidates) {
            if (need == 0) break;
            int take = Math.min(x.remaining, need);
            x.remaining -= take;
            need -= take;
            if (x.remaining == 0) {
                stacks.remove(x);
                owner.discard.add(x.cardId);
            }
        }
        if (need > 0) throw new IllegalStateException("spend() called without checking availability");
    }

    private Vehicle newVehicle(GameState s, VehicleSpec spec) {
        return new Vehicle(s.nextId++, spec.cardId(), spec.hp());
    }

    private void newGroupOf(GameState s, PlayerState me, VehicleSpec spec) {
        StrikeGroup g = new StrikeGroup(s.nextId++);
        g.vehicles.add(newVehicle(s, spec));
        me.groups.add(g);
    }

    private boolean hasTank(List<Long> hand) {
        return hand.stream().anyMatch(id -> catalog.find(id) instanceof VehicleSpec v && v.isTank());
    }

    private static void draw(PlayerState p, int count) {
        for (int i = 0; i < count && !p.deck.isEmpty(); i++) p.hand.add(p.deck.remove(0));
    }

    private static RuleViolationException violation(String message) {
        return new RuleViolationException(message);
    }
}
