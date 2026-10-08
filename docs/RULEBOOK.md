# Valtrak Rulebook: DRAFT v0.6

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
- A group is **hidden** (face down) or **revealed** (face up). Vehicles can be added to or removed from a group **only while the whole group is hidden**.
  A removed vehicle goes into another hidden group with room, or back to your hand (assumed).
- **Moving a vehicle from one group to another** costs **Fuel** (see §7). Deploying from your hand into a hidden group is free.
- **Forming a multi-vehicle group:** when a second vehicle first joins a lone tank, spend **Supply** from your Depot by the Leader's rarity:
  Common/Uncommon **[1]**, Rare/Epic **[2]**, Legendary/Commander **[3]** (assumed).

## 5. Resources
Resources are **cards**, and they stay on the table as cards. Ammo, Fuel and Supply cards show an amount
(a "5x" crate = 5) and keep track of how much is left on them. You can place a resource card in one of two places:

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
- The discard pile is where used and lost resource cards go. A future Recycle card can bring some back from it.

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
   - **Deploy** a vehicle from hand, face down: as a lone tank, or into a hidden group with a free slot. No resource cost.
   - **Form** a strike group (§4), **organize** hidden groups (§4), **heal** with a Repair card (§5).
   - **Reveal** a group: its Leader and Line vehicles turn face up. Free. Revealed vehicles can attack and be attacked.
   - **Reveal your Specialist** (separate, free): it only does its job while revealed, but a revealed Specialist can be attacked.
   - **Retreat** a vehicle: spend Fuel from its group's pool by its rarity (Common/Uncommon **[1]**, Rare/Epic **[2]**, Legendary/Commander **[3]**).
     It turns face down but **stays in its group**; it can't attack or be attacked until you reveal it again (free).
   - **Retreat a Leader:** costs the most, **[double]** its rarity cost, and the **whole group** turns face down.
     A fully hidden group can then be reorganized this turn, with moves between groups costing **no** Fuel.
   - **Move a vehicle** between two hidden groups: spend Fuel by its rarity from the group it leaves (§4).
   - **Play item cards**.
3. **Attack, or pass.** Attacking **ends your turn** immediately, so an attacking group stays revealed through your opponent's turn.
   If you can't or won't attack, choose **End Turn**.

## 8. Attacking
- Choose one **revealed** strike group as the attacker.
- Choose which of its vehicles attack (at least one, assumed) and which attack each uses. Add up the requirements of all chosen attacks:
  the group's pool must contain at least that much of each Ammo type and of Fuel at that moment. **That amount is spent** from the cards in the pool.
  Anything extra stays in the pool. Use the convoy in your main step, before attacking, to move resources in from your Depot.
- The Resupply vehicle never attacks. A Specialist attacks only if its card lists attacks.
- Each attack targets one **revealed** enemy vehicle. Damage uses the existing damage-type, armor and special-effect rules.
- A **hidden Specialist** doesn't stop the group from attacking, but it can't use its ability. The **Resupply** vehicle never has to be revealed
  and can't be targeted (unless an item or effect says otherwise).
- A vehicle at 0 HP is destroyed and goes to the discard pile.

## 9. Losing vehicles and Territory Chips
- **A vehicle that is not the Leader is destroyed:** it goes to the discard pile, the group keeps going with one fewer attacker,
  and the pool is unchanged. **No chip.**
- **The Leader is destroyed:** the strike group is destroyed. The attacker takes **1 Territory Chip**, plus **[1 bonus chip if the group had 4 or more vehicles]** (assumed; never more than 2 chips for one group). The group disbands:
  - Its owner discards **[half, rounded up]** of the resource **cards** in the pool (the owner chooses which); the rest go to the Depot (assumed).
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
1. **Group upside:** big groups give the opponent a bonus chip when destroyed, so groups need a clear upside beyond making several attacks per turn
   (for example Leader bonuses). Wait for playtests.
2. **Stalling:** Recon and UAV vehicles can force a reveal. Is that enough, or do you also want a stalemate rule?
3. Fuel costs for retreating and moving vehicles, convoy capacity (and whether to keep the cap), group-size chip bonus.
4. Resupply abilities beyond moving cards, special item rules, and air units.
5. All bracketed numbers: need playtesting (a bot-vs-bot simulator can help).

## Planned cards (wish list)
- **UAV group (Specialist):** can force a hidden enemy vehicle or group face up. Whether this is an ability or an attack is undecided.
  (An ability can be used before attacking the same turn; an attack would use up the turn.)
- Hand-disruption cards (shrink the opponent's hand), Recycle cards (return resource cards from your discard pile), capture/seize cards.
