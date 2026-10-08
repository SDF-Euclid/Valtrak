# Simulating games with bots

`Gameplay/Simulation` plays thousands of bot-vs-bot games on the real rules engine so numbers can be tuned with data
instead of guesses. Run `SimulationMain` from your IDE (or `java -cp target/classes:<dependencies> ...SimulationMain`);
options are `key=value` program arguments:

| option | default | meaning |
|---|---|---|
| `games` | 200 | games per matchup |
| `chips` | 5 | chips needed to win |
| `deckSize` | 60 | cards per deck (60-100) |
| `damage` | 100 | every attack's damage in percent (an experiment knob) |
| `designations` | 1 | resource cards a player may play per turn |
| `stalemate` | 0 | experiment: after this many rounds where nobody attacks, every vehicle (except Resupply) is revealed. 0 = off, as in the rulebook |
| `fillAmmo` | false | add synthetic Ammo cards for weapons that have no Ammo card (not needed any more: every ammunition type has cards) |
| `matchups` | all | `all` or `aggressive` (just aggressive vs aggressive) |

**Bots.** `GreedyBot` plays the resource it needs most, deploys tanks into its main group, repairs, reveals when it can afford an attack,
then attacks the enemy Leader it can hurt most. *Aggressive* bots reveal as soon as they are ready; *cautious* bots only reveal when they can
attack right away. `RandomBot` is a baseline.

**Limits of these numbers.** The bots are simple, so treat results as *relative* (what changes when a rule changes), not as how humans will play.
The card library only has tanks, so there are no convoys, specialists or groups of 4+, and several weapons have no Ammo card yet
(`fillAmmo` adds synthetic ones so every tank can fire).

## Findings (rulebook v0.9, 100 games per cell, aggressive bots unless stated)

**1. Without a forced reveal the game stalls.** Only face-up vehicles can be attacked, and revealing exposes you, so cautious bots never
reveal and never fight. The game is decided by deck-out, and the **first player loses every time** (they draw first, so they run out first):
cautious vs cautious, rules as written: 0 attacks, 100% decided by deck-out, first player won 0%.
With the stalemate rule at 3 rounds the same bots fight (first attack around turn 9, 100% decided by chips with 100-card decks, first player won 48%).

**2. With 5 chips and 60-card decks, games are decided by deck-out, not chips.** About 4 groups are destroyed per game in total, but 5 chips
takes more: chips won by deck-out 100% (aggressive vs aggressive, stalemate off). With 100-card decks 98% end by chips.

**3. Games are long.** Average total turns (both players added together), 100-card decks, stalemate rule 3
(and, in brackets, how often a 60-card deck still runs out first):

| chips \ damage | 100% | 200% | 300% |
|---|---|---|---|
| 3 | 63 turns [29%] | 38 [3%] | 31 [0%] |
| 4 | 81 [62%] | 51 [7%] | 41 [0%] |
| 5 | 99 [94%] | 64 [20%] | 51 [4%] |

At 100% damage a 5-chip game lasts about 49 turns per player. Pokémon and Magic games last about 8-12 turns per player.
Doubling damage (or halving HP) cuts a 5-chip game to about 32 turns per player; there is a floor of roughly 20 turns per player
because groups must be built, resourced and revealed before they can hit anything.

**4. Turn order is fair once games are decided by fighting.** First player won 45-50% in every configuration that ended by chips.

## Round 2: all ammo cards added, aiming for 13-15 turns per player

Average turns **per player**, aggressive bots, 100-card decks, real Ammo cards (every ammunition type now has 1x/5x/10x/20x cards),
reveal-everything rule at 3 passive rounds. The game is tank-only for now, so every destroyed group is worth 1 chip.

| chips \ damage | 100% | 200% | 300% | 400% |
|---|---|---|---|---|
| 3 | | 21.5 | 16.3 | **14.4** |
| 4 | | 28.2 | 21.7 | 19.7 |
| 5 | 59 | 34.2 | 27.4 | 24.7 |

- **Damage is the main lever.** At 100% a 5-chip game takes about 59 turns per player. *Damage x4 is about the same as HP /4.*
- **Resource pace is not the limit:** playing 2 resource cards per turn instead of 1 changed nothing (within 1%).
- **How fast things get revealed is not the limit either:** revealing everything after 1, 2 or 3 passive rounds gave the same game length.
- **There is a floor.** Even with one-shot kills (damage x10), a 5-chip game still takes about 19 turns per player, because every group has to be
  rebuilt (tank, Supply, Ammo, Fuel) before it can be killed. That is about 4 turns per player per chip.
  Bigger groups (4+ vehicles) are worth 2 chips, so once those cards exist, 5 chips will be reached faster than these tank-only numbers show.
- **Conclusion for 13-15 turns per player with today's cards:** 3 chips and about 4x damage (or about 1/4 HP).

## Round 3: UAV and Recon cards (reveal ability), no stalemate rule

Cautious vs cautious bots, rules exactly as written, 80-card decks, 100 games. The bots bring a UAV/Recon vehicle out and reveal
enemy Leaders when their group is ready to attack.

| reveal cards in the deck | games decided by chips | attacks per game |
|---|---|---|
| none | 0% (never fight, deck-out decides, first player loses) | 0 |
| 1 copy of each of the 10 cards | 75% | 62 |

- **Reveal cards fix the stall** without a stalemate rule, as long as the deck has some. A deck without any can still stall.
- Counting copies is not a clean test: in a fixed-size deck every extra reveal card replaces a tank or a resource card,
  so more copies lowered the share of games decided by chips (24% with 2 copies, 3% with 3). The deck needs a balance.
- **Leader position is visible.** The first vehicle of an enemy group is its Leader, so a face-down group can be sniped
  (a UAV that reveals 1 can expose a Leader). Change `GameViewBuilder` if face-down order should be hidden.

## Things worth deciding
- A stalemate rule (or a reliable reveal effect from the start) so the game isn't decided by deck size and turn order.
- A target game length, then tune chips, damage/HP and deck minimum together (the table above is a starting point).
- Ammo cards for the .50 cal, 105mm, TOW and 40mm HE weapons: without them those tanks can't attack at all with real cards.
