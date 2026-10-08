# Valtrak Rulebook: DRAFT v0.4

Values in **[brackets]** are tunable numbers I picked as a starting point. Lines marked **(assumed)** are gaps I filled in;
please veto or change them. Items marked **(OPEN)** are listed again at the bottom.

## 1. Goal
Be the first player to take **[3]** Territory Chips. You take one chip each time you destroy an enemy strike group (see §9).
There are **5** chips in the middle (3 + 3 − 1: the most that can be taken before someone reaches 3), so there is always a winner.
A player who is behind 0–2 can still win by taking the next three. Chips are never lost.
You also lose if you must draw from an empty deck.

## 2. Decks
- **[50–80]** cards, at most **3 copies** of any card.
- At least **[12]** tanks, so a starting hand almost always has one (assumed).

## 3. Card types
| Type | Notes |
|---|---|
| Vehicle | HP, armor, and up to three attacks (ATTACK_1/2/3) that each list a resource requirement. A vehicle with no attacks never attacks. |
| Resource | **Ammo** (a specific type), **Fuel**, **Supply**, **Repair**. |
| Special item | Air Strike, Smoke Screen, extra draws, etc. (rules per card). |

## 4. Strike groups
A strike group is up to **5** vehicles with these slots:

| Slot | Count | Allowed vehicles |
|---|---|---|
| Leader | exactly 1 | a tank: Light, Medium, Heavy or Main Battle Tank |
| Line | up to 2 | any vehicle that is **not** a Specialist and **not** a Resupply vehicle |
| Specialist | up to 1 | Specialist class vehicle |
| Resupply | up to 1 | Supply class vehicle (the convoy, see §5) |

- **A single tank is a strike group of one.** It needs no formation cost and counts toward your group limit.
  Only tanks can stand alone (assumed); every other vehicle joins an existing group.
- Group limit: **3** at a time, **+1 for each chip you hold** (so at most +2, since the game ends at 3 chips).
- A group is **hidden** (face down) or **revealed** (face up). Units can be added to or removed from a group **only while it is hidden**.
  A removed unit must go to another hidden group with room, or back to your hand (assumed).
- **Forming a multi-vehicle group:** when a second vehicle first joins a lone tank, spend **Supply** from your Depot by the Leader's rarity:
  Common/Uncommon **[1]**, Rare/Epic **[2]**, Legendary/Commander **[3]** (assumed).

## 5. Resources
Resources are counters. When you play a Resource card it goes to your discard pile and you gain its resources
(a 5x Ammo crate = 5 Ammo of that type). You can place them in one of two places:

| | **Depot** | **Strike group pool** |
|---|---|---|
| What goes there | Any resource | Ammo and Fuel only (assumed; Supply and Repair are only ever spent from the Depot) |
| Safe? | **Yes.** Never lost when a group is destroyed. | **At risk.** Part is discarded if the Leader dies (§9). |
| Used for | Forming groups (Supply), healing (Repair), and refilling a pool through a convoy | Attacks and retreats |
| Spent when used? | Yes | **Yes.** Attacks use up the resources they need |

- Resources that are used up go into your **spent pile** (kept as counts, so a future Recycle card can return some of them).
- A **lone tank's pool** is its own; when other vehicles join it, the pool becomes the group's pool (assumed).
- **Designate:** play one Resource card per turn **[1]** (items can raise this). Put it in your Depot, or directly into a group's pool (unlimited amount, but at risk).
- Depot contents are public (assumed); the number of resources in each pool is public too.
- **Convoy (a Resupply vehicle in the group):** during your main step you may move resources from your Depot into the group's pool,
  up to the Resupply vehicle's **capacity** per turn (a number on its card; assumed **[Common 3, Rare 6, Legendary 10]**).
  Without a convoy, resources can't move from the Depot into a pool. Any resources beyond the capacity must already be in the pool.
  Example: an attack needs 9 Ammo and the convoy's capacity is 5, so at least 4 Ammo must already be in the pool.
- **Repair:** spend any amount from your Depot to restore that much HP to one vehicle (assumed).

## 6. Setup
1. Shuffle, draw **7**. If your hand has no tank, you **mulligan**: shuffle back and draw 7 again.
   For each mulligan you take, your opponent draws **1 extra card** (up to **3** extra cards total).
2. Each player puts one tank from hand onto the field **face down** (a group of one).
3. A coin flip decides who goes first. Nobody can attack on their own first turn.

## 7. Your turn
1. **Draw 1 card** (items or field conditions can add more). The first player also draws on turn 1.
2. **Main step.** Do any of these in any order, as often as the rules allow:
   - **Designate** a Resource card into your Depot or a group's pool, and use a **convoy** to refill a pool (§5).
   - **Deploy** a vehicle from hand, face down: as a lone tank, or into a hidden group with a free slot. No resource cost.
   - **Form** a strike group (§4), **organize** hidden groups (§4), **heal** with Repair (§5).
   - **Reveal** a group: its Leader and Line vehicles turn face up. Free. Revealed vehicles can attack and be attacked.
   - **Reveal your Specialist** (separate, free): it only does its job while revealed, but a revealed Specialist can be attacked.
   - **Retreat** a vehicle: spend **[1 Fuel]** from its group's pool (refill it with the convoy first if needed); it leaves the group and goes back to hidden.
   - **Play item cards**.
3. **Attack, or pass.** Attacking **ends your turn** immediately. If you can't or won't attack, choose **End Turn**.

## 8. Attacking
- Choose one **revealed** strike group as the attacker.
- Choose which of its vehicles attack (at least one, assumed) and which attack each uses. Add up the requirements of all chosen attacks:
  the group's pool must contain at least that much of each Ammo type and of Fuel at that moment. **That amount is spent.**
  Anything extra stays in the pool. Use the convoy in your main step, before attacking, to move resources in from your Depot.
- The Resupply vehicle never attacks. A Specialist attacks only if its card lists attacks.
- Each attack targets one **revealed** enemy vehicle. Damage uses the existing damage-type, armor and special-effect rules.
- A **hidden Specialist** doesn't stop the group from attacking, but it can't use its ability. The **Resupply** vehicle never has to be revealed
  and can't be targeted (unless an item or effect says otherwise).
- A vehicle at 0 HP is destroyed and goes to the discard pile.

## 9. Losing vehicles and Territory Chips
- **A vehicle that is not the Leader is destroyed:** it goes to the discard pile, the group keeps going with one fewer attacker,
  and the pool is unchanged. **No chip.**
- **The Leader is destroyed:** the strike group is destroyed. The attacker takes **1 Territory Chip**, and the group disbands:
  - Its owner discards **[half, rounded up]** of each resource type in the pool; the rest goes to the Depot (assumed).
  - Surviving **tanks** become groups of one, face down.
  - Surviving **Specialist, anti-air, Resupply and any other non-tank vehicles**, with all cards attached to them,
    **return to your hand**. They can't be played on the turn they return.
- **A group of one** is its own Leader, so destroying it takes a chip and discards its whole pool.
- Each chip raises the taker's group limit by 1 (§4). If disbanded survivors put you over your limit,
  you can't create new groups until you are back under it (assumed).

## 10. Hidden information
- Your opponent sees how many vehicles you have, which groups are revealed, your Depot, and pool sizes,
  but **not** the identity of face-down cards.
- Your opponent never sees your hand or deck order, only their sizes.

---

## Open questions
1. Convoy capacity numbers per Resupply vehicle, and whether a cap is worth the extra rules.
2. A hand limit (and, if you add one, an extra "reserve" play area for spare vehicles).
3. Resupply vehicle abilities beyond moving resources, special item rules, and where air units fit.
4. All bracketed numbers: need playtesting.
