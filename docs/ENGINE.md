# Valtrak rules engine

The engine implements `docs/RULEBOOK.md`. It lives in `Gameplay/Engine` and has no database, web or Spring code,
so it can be unit tested, saved as JSON, and run in bot-vs-bot simulations.

## How to use it
```java
GameEngine engine = new GameEngine(GameRules.defaults(), catalog);   // catalog: CardCatalog (card id -> VehicleSpec / ResourceSpec)
GameState state = engine.newGame(deck0, deck1, new Random());       // shuffles, deals, mulligans, picks who goes first
engine.apply(state, 0, new Action.PlaceStartingTank(tankCardId));   // throws RuleViolationException if the move is illegal
List<Action> options = engine.legalActions(state, player);          // everything the player may do right now
```
- `GameRules` holds every number from the rulebook. Change one there and nothing else needs touching.
- `apply` checks all of an action's rules before changing anything, so a rejected move leaves the state untouched
  (a test enforces this).
- `GameState` is plain data (public fields), so it serialises to JSON for saving and for the future server API.

## How the server uses it
- `DbCardCatalog` gives the engine its card data from the database (read once, on first use).
- `MatchService` runs challenges and moves, saving the whole `GameState` as JSON after every move;
  `GameViewBuilder` decides what each player may see. See `docs/API.md`.

## What is not built yet
- Resupply and anti-air vehicles (not in the card library yet, so the convoy can't be tried in a real game), and the other item ideas in the rulebook (Smoke Screen, Jammer, ...).
- The game screen, and pushing updates to the other player.
- The old `CombatService` (and its tests) are no longer used by the game; `DamageCalculator` replaces it.

## Item cards
`ItemSpec` (effect `ERA`, `ARTILLERY`, `SEARCH` or `DRAW`, plus `power`, `count` and a `SearchKind`) is the engine's view of an item card, and
`Action.PlayItem(cardId, targetIds, cardIds)` plays one. `GameEngine.playItem` checks everything first (each effect has a `prepare...` method that
validates and returns what to do), then pays the optional Supply cost (`GameRules.itemSupply`, all 0 today), moves the card to the discard pile
(ERA stays on its vehicle as `Vehicle.eraCardId`), and runs the effect.
- **ERA** reduces damage from CHEMICAL ammo by `power` percent (minimum 1). It goes to the discard pile when the vehicle is destroyed and to the
  hand with a non-tank survivor of a destroyed group.
- **Artillery** is true damage (scaled by `damagePercent`). Cards at or above `GameRules.artilleryBlindFrom` (Legendary) can also pick face-down
  vehicles, which are turned face up. A target that has left the field, or gone face down, since an earlier hit in the same play is skipped.
- **Search** shuffles with `GameState.rngSeed` (a seed stored in the state and never sent to players), so a saved game replays the same way.
- **Draw** is refused if the deck has fewer than `count` cards.
- **Per-turn limits:** `GameRules.itemLimitPerTurn` (Artillery 1, Sabotage 1); uses are counted in `PlayerState.itemUses` and cleared when the turn starts.
- **Smoke** sets `Vehicle.smoked` (can't be targeted by attacks or Artillery, and can't attack); it is cleared when its owner's next turn starts.
- **Jammer** sets `StrikeGroup.jammerCardId`: `UseAbility` can't target vehicles in that group. At the start of its owner's turn the group's pool pays the
  upkeep Fuel, or the Jammer is discarded (also discarded with its group).
- **Camouflage** sets `Vehicle.camoCardId`: `retreatCost` takes its `power` off the retreat Fuel (a Leader's Camouflage also makes a group retreat cheaper).
- **Sabotage** and the Search shuffle use `GameState.rngSeed`. **Recycle** and **Rapid Deployment** check their picks against the discard pile / hand;
  Rapid Deployment tries the slot rules on a copy of the group, one vehicle at a time.

To add an item effect: add it to `ItemEffect`, write a `prepare...` method, add it to `candidates` (for bots), to `SpecialItemEffect` and to
`DbCardCatalog.itemSpec` / `EnumCardCatalog`, then add cards to `SpecialItem`.

## Abilities
A vehicle card can have an `AbilitySpec` (type, power, Fuel cost). `Action.UseAbility` runs it: once per turn, face up only, Fuel from the
group's pool. Today there is one type, `REVEAL_ENEMY` (UAV teams, Recon vehicles). To add another: add a value to `AbilityType`,
handle it in `GameEngine.useAbility`, add tests, then give cards the new ability in the card library.

## Hidden information
`GameViewBuilder` shows an opponent's group with its face-up vehicles first and the face-down ones after, in a fixed scrambled order, so the
Leader (the first vehicle in the real list) can't be picked out. ERA is hidden while its vehicle is face down.

## Experiment rules (off by default)
`GameRules.stalemateRounds` (reveal everything after N passive rounds, off by default) exists so the simulator can measure a candidate change.
`GameRules.damagePercent` is the damage scale (400 = x4) and `winChips` is 3: together they give 13-15 turns per player (see `docs/SIMULATION.md`).
The tests use `TestWorld.rules` (5 chips, x1) so their numbers stay simple; `GameRulesTest`-style checks of the real defaults are in `MoreItemCardsTest`.

## Choices the engine makes where the rulebook is silent
- Resources are spent smallest card first, so big crates are kept for later.
- When a Leader dies, the pool cards the owner loses are the ones with the least left on them.
  (The rulebook says the owner chooses; a choice action can be added later.)
- Status effects hit a vehicle during the opponent's turn and wear off at the end of its owner's next turn.
  (The old code cleared them before they could matter.)
- If an earlier attack in the same Combined Assault destroys a target, later attacks on it are wasted (and still paid for).
- The rule "returned vehicles can't be played that turn" is not tracked: they return during the opponent's turn.

## Balance notes
- In the existing damage table, kinetic damage gets *better* against heavier armor brackets, so a breach (which lowers
  armor) can lower kinetic damage by dropping the target into a lighter bracket. `DamageMatchups` is the one place to change it.
