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

## What is not built yet
- A `CardCatalog` backed by the database (tests use a hand-built catalog) and saving a game's state.
- Server endpoints and the per-player view that hides the opponent's hand and face-down cards.
  The old `GameService` / `GameController` still use the previous rules and will be replaced by these.
- Special item cards, Resupply abilities beyond the convoy, and Supply cards in the card library.

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
