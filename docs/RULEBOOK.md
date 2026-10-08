# Valtrak Rulebook: DRAFT v0.19

Values in **[brackets]** are tunable numbers I picked as a starting point. Lines marked **(assumed)** are gaps I filled in;
please veto or change them. Items marked **(OPEN)** are listed again at the bottom.

## 1. Goal
Be the first player to take **[3]** Territory Chips. You take chips when you destroy an enemy strike group (see §9). Chips are never lost.
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
| Leader | exactly 1 | the **highest-rarity tank** in the group (Light, Medium, Heavy or Main Battle Tank). On a tie, the tank that has been in the group longest leads |
| Line | up to 2 | any vehicle that is **not** a Specialist and **not** a Resupply vehicle |
| Specialist | up to 1 | Specialist class vehicle |
| Resupply | up to 1 | Supply class vehicle (the convoy, see §5) |

- **A single tank is a strike group of one.** It needs no formation cost and counts toward your group limit.
  Only tanks can stand alone (assumed); every other vehicle joins an existing group.
- Group limit: **3** at a time, **+1 for every 2 chips you hold** (assumed).
- **Facing:** every vehicle is **face down** (hidden) or **face up** (revealed). Face-down vehicles can't attack or be attacked.
  A group is "revealed" when its attacking vehicles are face up. New vehicles are played face down.
- **Leaders can't leave:** a Leader stays in the group it leads for as long as it lives. Groups never merge.
  When a better tank joins, it becomes the Leader and the old Leader becomes an ordinary Line tank (which can then be moved).
- **Moving a vehicle** to another group (or, for a tank, out to lead a new group of one if you are under your limit) costs **Fuel**
  by its rarity, taken from the pool of the group it leaves (§7). This costs the same whether the vehicle is face up or face down.
  Playing a vehicle from your hand into a group is free; vehicles can't go back to your hand voluntarily (assumed).
- **Forming a multi-vehicle group:** when a second vehicle first joins a lone tank, spend **Supply** from your Depot by the rarity of the Leader the group will have (so adding a Legendary tank to a Common tank costs the Legendary price):
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
  **The Resupply vehicles in the game now** (names and stats are placeholders; they have no attacks and no ability):
  Rare (**convoy capacity 2**): M977 HEMTT Supply Truck (US), Ural-4320 Supply Truck (Russia), MAN SX 8x8 Supply Truck (Germany).
  Legendary (**capacity 3**): M1075 Palletized Load System (US), KamAZ-5350 Armored Convoy Truck (Russia), Rheinmetall HX Armored Logistics Truck (Germany).
  Anti-air and air vehicles are left for a later update.
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

## 7b. Abilities (UAV teams and Recon vehicles)
Some vehicles have an **ability**. A vehicle can use its ability **once per turn**, only while it is **face up** (and not stunned or disabled),
and it costs **Fuel** from its group's pool. Using one does not end your turn, so you can scout and then attack.
- **Reveal** (UAV teams and Recon vehicles): turn up to *N* face-down enemy vehicles face up. You choose which ones
  (you can't tell which face-down vehicle is a Leader). Revealed vehicles stay face up until their owner retreats them.
  A Resupply vehicle can be revealed this way, and then it can be attacked like any other face-up vehicle.
- **UAV teams** are Specialists (they take the Specialist slot and don't attack): Common and Uncommon reveal 1,
  Rare and Epic reveal 2, Legendary reveals 3. **Recon vehicles** take a Line slot, carry a light MG, and reveal 1 (Rare: 2).

## 7c. Item cards
Item cards are played from your hand during your main step, **as many as you like per turn** (like Pokémon trainer cards). Playing one never ends your turn.
Some cards have their own **limit per turn** (marked below). The engine has a per-rarity Supply cost switch (`itemSupply`, set to 0 now) in case items turn out too easy.
A played item goes to the discard pile, except attached cards (ERA, Camouflage, Jammer), which stay on the field until the thing they are attached to is gone.
Cards come in the rarities listed; the number is by rarity: **C / U / R / E / L**.

| Card | What it does | By rarity |
|---|---|---|
| **ERA** (attached to a vehicle) | That vehicle takes a **steady percentage** less chemical damage (HEAT, TOW, other CHEMICAL ammo) until it is destroyed (the ERA then goes to the discard pile). It stays through moves, retreats and reveals. If the vehicle returns to your hand (a non-tank whose group was destroyed), the ERA returns with it. One ERA per vehicle: a new one replaces the old. | **[20% / 25% / 35% / 40% / 50%]** |
| **Artillery** (**1 per turn**) | Choose up to *N* enemy vehicles. Each takes **true damage** (ignores armor and breach). Below Legendary you may only choose **face-up** vehicles. **Legendary can also choose face-down vehicles**; any vehicle it hits turns face up. It does not expose any vehicle of yours. A destroyed Leader takes the chip as usual (a later target in a group that has just been destroyed is skipped). Smoke Screen protects against it. | targets **1 / 1 / 2 / 2 / 3**, damage each **[20 / 25 / 30 / 35 / 40]** (times the damage scale) |
| **Search** | Look through your deck for up to *N* cards of one kind, show them to your opponent, put them in your hand, shuffle. Three kinds: **resources** (Ammo, Fuel, Supply, Repair), **tanks**, **support vehicles** (Specialists, Resupply, Recon and other non-tanks). You may find fewer than *N*. | *N* = **1 / 1 / 2 / 2 / 3** (Common, Rare and Legendary cards exist for each kind) |
| **Draw** | Draw *N* cards. Condition: your deck must have at least *N* cards. | *N* = **1 / 1 / 2 / 2 / 3** |
| **Smoke Screen** | Choose up to *N* of your vehicles. Until the start of your next turn they **can't be targeted** by attacks or Artillery (Legendary too), **and they can't attack** while they are in the smoke. (Without that cost you could smoke up and then shoot.) Reveals still work on them. | *N* = **1 / 1 / 2 / 2 / 3** (cards exist at C, R, L) |
| **Jammer** (part of a strike group) | Attach it to one of your strike groups (it is **not a vehicle and takes no slot**). It starts **off**; you can switch it **on or off at any time for free**, independently of revealing anything. **While on**, enemy **reveal abilities (UAV, Recon) can't target any vehicle in the group**, and it costs **upkeep** Fuel from the group's pool at the start of each of your turns (if the pool can't pay, it switches itself off; it is **not** discarded). **While on it can be attacked** (it has its own HP, no armor, and is a target like a face-up vehicle), which costs the attacker ammo and their turn. When it is destroyed it goes to the discard pile and the jamming ends: the group is **not** revealed, but UAV and Recon cards can reveal it again. A group destroyed by losing its Leader loses its Jammer. One Jammer per group (a new one replaces the old). Opponents only see a Jammer that is on. *Why it can't be used to stall:* it works only while it can be shot, and it costs Fuel. | upkeep **[2 / 2 / 1 / 1 / 1]** Fuel, HP **[80 / - / 160]** (cards exist at C and R) |
| **Camouflage** (attached to a vehicle) | Retreating this vehicle costs less Fuel (never below 0). If it is a group's Leader, a group retreat costs less too. It goes to the discard pile if the vehicle is destroyed and returns to your hand with it, like ERA. | **[1 / 1 / 2 / 2 / 3]** less Fuel (cards exist at C, R, L) |
| **Sabotage** (**1 per turn**) | Your opponent discards **up to 3** cards from their hand, **chosen at random** (fewer if they hold fewer). They go to the public discard pile, so you see what they were. Condition: your opponent has a card in hand. | one card (Rare) |
| **Recycle** | Return up to *N* resource cards from your discard pile to your hand. | *N* = **1 / 1 / 2 / 2 / 3** (cards exist at C, R, L) |
| **Airdrop** (**1 per turn**) | Move up to *N* **Ammo or Fuel** cards from your Depot into one of your strike groups' pools. **No Resupply vehicle needed**, and it doesn't use up the convoy's per-turn capacity. (Supply and Repair cards can't go into a pool.) | *N* = **1 / 1 / 2 / 2 / 3** (cards exist at C, R, L) |
| **Rapid Deployment** | Deploy up to *N* vehicles from your hand into one of your strike groups **without paying the formation cost** (slot rules still apply). | *N* = **1 / 1 / 2 / 2 / 3** (cards exist at C, R, L) |

## 8. Attacking
- Choose a strike group, then which of its **face-up** vehicles attack (at least one) and which attack each one uses.
- **Skirmish:** exactly one vehicle attacks. The Leader may stay face down. Only that vehicle's requirement is paid.
- **Combined Assault** (names are placeholders): two or more vehicles attack together. **The Leader must be face up** (it does not have to attack).
- Add up the requirements of all chosen attacks: the group's pool must contain at least that much of each Ammo type and of Fuel at that moment.
  **That amount is spent** from the cards in the pool. Anything extra stays in the pool.
  Use the convoy in your main step, before attacking, to move resources in from your Depot.
- The Resupply vehicle never attacks. A Specialist attacks only if its card lists attacks.
- Each attack targets one **face-up** enemy vehicle. Damage uses the existing damage-type, armor and special-effect rules.
- **Damage scale [4x]:** all damage dealt (by attacks and by Artillery) is multiplied by 4 when it lands (armor is taken off first). It is one setting
  so the numbers on the cards stay small; it can be baked into the cards later. With 3 chips to win it gives about 13-15 turns per player.
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
- **Everything about a face-down vehicle is hidden**, including which one is the Leader and any ERA attached to it. A vehicle's place in its group is not shown while it is face down.

---

## Open questions
1. **Stalling:** UAV and Recon cards make players fight, but a deck without them can still stall. Do you want a backstop (a minimum number
   of reveal cards per deck, or a deck-out tiebreak)? Legendary Artillery now also hits face-down vehicles, which helps.
2. Convoy capacity, and (later) anti-air and air units.
3. All bracketed numbers: need playtesting.

## Planned cards (wish list)
- Capture/seize cards, field-condition cards. (Hand disruption and Recycle are in the item card ideas above.)
