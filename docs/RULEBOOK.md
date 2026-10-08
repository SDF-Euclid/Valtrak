# Valtrak Rulebook: DRAFT v0.9

Values in **[brackets]** are tunable numbers I picked as a starting point. Lines marked **(assumed)** are gaps I filled in;
please veto or change them. Items marked **(OPEN)** are listed again at the bottom.

## 1. Goal
Be the first player to take **[5]** Territory Chips. You take chips when you destroy an enemy strike group (see §9). Chips are never lost.
You also lose if you must draw from an empty deck.

## 2. Decks
- **[60–100]** cards, at most **3 copies** of any card.
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
- Group limit: **3** at a time, **+1 for every 2 chips you hold [so +3 at 6 chips]** (assumed).
- **Facing:** every vehicle is **face down** (hidden) or **face up** (revealed). Face-down vehicles can't attack or be attacked.
  A group is "revealed" when its attacking vehicles are face up. New vehicles are played face down.
- **Leaders can't leave:** a Leader stays in the group it leads for as long as it lives. Groups never merge.
- **Moving a vehicle** to another group (or, for a tank, out to lead a new group of one if you are under your limit) costs **Fuel**
  by its rarity, taken from the pool of the group it leaves (§7). This costs the same whether the vehicle is face up or face down.
  Playing a vehicle from your hand into a group is free; vehicles can't go back to your hand voluntarily (assumed).
- **Forming a multi-vehicle group:** when a second vehicle first joins a lone tank, spend **Supply** from your Depot by the Leader's rarity:
  Common/Uncommon **[1]**, Rare/Epic **[2]**, Legendary/Commander **[3]** (assumed).

## 5. Resources
Resources are **cards**, and they stay on the table as cards. Ammo, Fuel and Supply cards show an amount
(a "5x" crate = 5) and keep track of how much is left on them with **chips** placed on the card. You can place a resource card in one of two places:

| | **Depot** | **Strike group pool** |
|---|---|---|
| What goes there | Any resource card | Ammo and Fuel cards only (assumed) |
| Safe? | **Yes.** Never lost when a group is destroyed. | **At risk.** Part is discarded if the Leader dies (§9). |
| Used for | Forming groups (Supply), healing (Repair), refilling a pool through a convoy | Attacks and retreats |

- **Spending:** when something costs resources, spend amounts from the cards in that place. A card can be used partly
  (a 5x crate that has paid 3 keeps 2). A card with nothing left goes to your discard pile. Attacks use up the resources they need.
- **Repair cards** are used whole, from the Depot: pick one vehicle and restore up to the card's amount of HP, then discard the card.
  "Full Repairs" restores the vehicle to full HP and removes BREACH, then is discarded.
- **A lone tank's pool** is its own; when other vehicles join it, the pool becomes the group's pool (assumed).
- **Designate:** place one resource card per turn **[1]** from your hand into your Depot, or directly into a group's pool
  (items can raise the limit). Using Repair from the Depot does not count as designating.
- Depot contents are public (assumed); the cards in each pool are public too.
- **No storage limit** on the Depot or a pool: losing half of a pool when its Leader dies is the balance for that.
  There is also **no hand limit**; draw-more cards are kept in check by the risk of decking out (§1).
- **Convoy (a Resupply vehicle in the group):** during your main step you may move resource cards from your Depot into the group's pool,
  up to the Resupply vehicle's **capacity** per turn, counted in cards (a number on its card; assumed **[Common 1, Rare 2, Legendary 3]**).
  Without a convoy, cards can't move from the Depot into a pool. Everything beyond the capacity must already be in the pool.
  Example: an attack needs 9 Ammo and the convoy can move one card per turn: move a 5x crate in, and have 4 more already in the pool.
- There is **one discard pile** for everything, resource cards included (used up, lost with a group, or discarded by an effect).
  A card that goes to the discard pile is a normal card again, with its full amount (assumed).
  A future **Recycle** card will let you search your discard pile for resources (how many is up to that card).

## 6. Setup
1. Shuffle, draw **7**. If your hand has no tank, you **mulligan**: shuffle back and draw 7 again.
   For each mulligan you take, your opponent draws **1 extra card** (up to **3** extra cards total).
2. Each player puts one tank from hand onto the field **face down** (a group of one).
3. A coin flip decides who goes first. **The first player can't attack on their first turn; the second player can.**
   (Everything starts face down, so a first-turn attack needs a target that has been revealed.)

## 7. Your turn
1. **Draw 1 card** (items or field conditions can add more). The first player also draws on turn 1.
2. **Main step.** Do any of these in any order, as often as the rules allow:
   - **Designate** a Resource card into your Depot or a group's pool, and use a **convoy** to refill a pool (§5).
   - **Deploy** a vehicle from hand, face down: as a lone tank, or into a group with a free slot. No resource cost.
   - **Form** a strike group (§4), **heal** with a Repair card (§5).
   - **Retreat** a vehicle (turn it face down): spend Fuel from its group's pool by its rarity
     (Common/Uncommon **[1]**, Rare/Epic **[2]**, Legendary/Commander **[3]**). It **stays in its group**.
   - **Retreat the whole group:** spend **[double]** the Leader's retreat cost; every vehicle in the group turns face down.
   - **Reveal** (turn face up) a vehicle or a whole group: free. A Specialist only does its job while it is face up, but a face-up Specialist can be attacked.
   - **Move a vehicle** to another group: spend Fuel by its rarity from the group it leaves (§4).
   - **Play item cards**.
3. **Attack, or pass.** Attacking **ends your turn** immediately, so the vehicles that attacked (and the Leader, in an assault) stay face up
   through your opponent's turn.
   If you can't or won't attack, choose **End Turn**.

## 8. Attacking
- Choose a strike group, then which of its **face-up** vehicles attack (at least one) and which attack each one uses.
- **Skirmish:** exactly one vehicle attacks. The Leader may stay face down. Only that vehicle's requirement is paid.
- **Combined Assault** (names are placeholders): two or more vehicles attack together. **The Leader must be face up** (it does not have to attack).
- Add up the requirements of all chosen attacks: the group's pool must contain at least that much of each Ammo type and of Fuel at that moment.
  **That amount is spent** from the cards in the pool. Anything extra stays in the pool.
  Use the convoy in your main step, before attacking, to move resources in from your Depot.
- The Resupply vehicle never attacks. A Specialist attacks only if its card lists attacks.
- Each attack targets one **face-up** enemy vehicle. Damage uses the existing damage-type, armor and special-effect rules.
- A **face-down Specialist** doesn't stop the group from attacking, but it can't use its ability. The **Resupply** vehicle never has to be
  face up and can't be targeted (unless an item or effect says otherwise).
- A vehicle at 0 HP is destroyed and goes to the discard pile.

## 9. Losing vehicles and Territory Chips
- **A vehicle that is not the Leader is destroyed:** it goes to the discard pile, the group keeps going with one fewer attacker,
  and the pool is unchanged. **No chip.**
- **The Leader is destroyed:** the strike group is destroyed. The attacker takes **1 Territory Chip**, plus **1 bonus chip if the group had 4 or more vehicles** (so a group is worth 1 or 2 chips). The group disbands:
  - Its owner discards **[half, rounded up]** of the resource **cards** in the pool (the owner chooses which); the rest go to the Depot (assumed).
  - Surviving **tanks** become groups of one, face down.
  - Surviving **Specialist, anti-air, Resupply and any other non-tank vehicles**, with all cards attached to them,
    **return to your hand**. (This happens during your opponent's turn, so you can play them on your next turn.)
- **A group of one** is its own Leader, so destroying it takes a chip and discards its whole pool.
- Every 2 chips raise the taker's group limit by 1 (§4). If disbanded survivors put you over your limit,
  you can't create new groups until you are back under it (assumed).

## 10. Hidden information
- Your opponent sees how many vehicles you have, which are face up, your Depot, and pool sizes,
  but **not** the identity of face-down cards.
- Your opponent never sees your hand or deck order, only their sizes.

---

## Open questions
1. Game length with 5 chips, once the simulator can measure it.
2. **Group upside:** big groups give the opponent a bonus chip when destroyed, so groups need a clear upside
   (Combined Assault's several attacks, Leader bonuses later). Wait for playtests.
3. Names for Skirmish and Combined Assault.
4. Fuel costs for retreating and moving vehicles, convoy capacity (and whether to keep the cap), group-size chip bonus.
5. Resupply abilities beyond moving cards, special item rules, and air units.
6. All bracketed numbers: need playtesting (a bot-vs-bot simulator can help).

## Planned cards (wish list)
- **UAV group (Specialist):** can force a hidden enemy vehicle or group face up. Whether this is an ability or an attack is undecided.
  (An ability can be used before attacking the same turn; an attack would use up the turn.)
- Hand-disruption cards (shrink the opponent's hand), Recycle cards (return resource cards from your discard pile), capture/seize cards.
