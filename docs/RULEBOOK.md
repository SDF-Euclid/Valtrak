# Valtrak Rulebook: DRAFT v0.2

Values in **[brackets]** are tunable numbers I picked as a starting point. Lines marked **(assumed)** are gaps I filled in;
please veto or change them. Items marked **(OPEN)** are listed again at the bottom.

## 1. Goal
Be the first player to take **[3]** Territory Chips. You take one chip each time you destroy an enemy strike group (see §9).
**(OPEN)** "5 chips available": what the other two chips do is not settled yet.
You also lose if you must draw from an empty deck.

## 2. Decks
- **[50–80]** cards, at most **3 copies** of any card.
- At least **[12]** vehicles that can stand alone (see §4), so mulligans are rare (assumed).

## 3. Card types
| Type | Notes |
|---|---|
| Vehicle | HP, armor, and up to three attacks (ATTACK_1/2/3) that each list a resource requirement. |
| Resource | **Ammo** (a specific type), **Fuel**, **Supply**, **Repair**. |
| Special item | Air Strike, Smoke Screen, extra draws, etc. (rules per card). |

## 4. Strike groups
A strike group is up to **5** vehicles with these slots:

| Slot | Count | Allowed vehicles |
|---|---|---|
| Leader | exactly 1 | a tank: Light, Medium, Heavy or Main Battle Tank |
| Line | up to 2 | any vehicle that is **not** a Specialist and **not** a Resupply vehicle |
| Specialist | up to 1 | Specialist class vehicle |
| Resupply | up to 1 | Supply class vehicle (convoy; abilities to come) |

- **A single vehicle counts as a strike group of one.** It needs no formation cost and counts toward your group limit.
  Only a tank can be a group of one (assumed). Specialist and Resupply vehicles can only join an existing group.
- Group limit: **3** at a time, **+1 for each chip you hold** (so at most +2 if the game ends at 3 chips).
- A group is **hidden** (face down) or **revealed** (face up). Units can be added to or removed from a group **only while it is hidden**.
  A removed unit must go to another hidden group with room, or back to your hand (assumed).
- **Forming a multi-vehicle group:** when a second vehicle first joins a lone tank, pay **Supply** by the Leader's rarity:
  Common/Uncommon **[1]**, Rare/Epic **[2]**, Legendary/Commander **[3]** (assumed).

## 5. Resources
- **Depot:** each player has one. It holds your stored resources of every type (Ammo by type, Fuel, Supply, Repair).
  Your opponent can see what is in it (assumed).
- **Designate:** put a Resource card from your hand into your Depot, **[1] card per turn** (items can raise this).
  The card goes to your discard pile and you gain its resources as counters (a 5x crate = 5 Ammo of that type).
- **Move:** to use resources you move them out of the Depot (free, any amount, one-way):
  - **Ammo and Fuel** go into a strike group's **pool** (shared by all its vehicles). They **stay there** and are not used up.
  - **Supply** is spent to form a group (§4).
  - **Repair** is spent to heal: spend any amount to restore that much HP to one vehicle (assumed).
- If a strike group is destroyed, everything in its pool is **discarded** (unless an opponent's item says otherwise).

## 6. Setup
1. Shuffle, draw **7**. If your hand has no tank that can start as a group of one, you **mulligan**: shuffle back and draw 7 again.
   For each mulligan you take, your opponent draws **1 extra card** (up to **3** extra cards total).
2. Each player puts one tank from hand onto the field **face down** (a group of one).
3. A coin flip decides who goes first. Nobody can attack on their own first turn.

## 7. Your turn
1. **Draw 1 card** (items or field conditions can add more). The first player also draws on turn 1.
2. **Main step.** Do any of these in any order, as often as the rules allow:
   - **Designate** a resource (§5), **move** resources out of your Depot (§5).
   - **Deploy** a vehicle from hand, face down: as a lone tank, or into a hidden group with a free slot. No resource cost.
   - **Form** a strike group (§4), **organize** hidden groups (§4).
   - **Reveal** a group: its Leader and Line vehicles turn face up. Free. Revealed vehicles can attack and be attacked.
   - **Reveal your Specialist** (separate, free): it only does its job while revealed, but a revealed Specialist can be attacked.
   - **Retreat** a vehicle: spend **[1 Fuel]** from its group's pool; it leaves the group and goes back to hidden (assumed).
   - **Play item cards**.
3. **Attack, or pass.** Attacking **ends your turn** immediately. If you can't or won't attack, choose **End Turn**.

## 8. Attacking
- Choose one **revealed** strike group as the attacker.
- **Every attacking vehicle in the group uses one of its attacks.** Add up the requirements of all chosen attacks: the group's pool must contain
  at least that much of each Ammo type and of Fuel. Nothing is spent, so the resources stay in the pool after the attack.
- Each attack targets one **revealed** enemy vehicle. Damage uses the existing damage-type, armor and special-effect rules.
- A **hidden Specialist** doesn't stop the group from attacking, but it can't use its ability. The **Resupply** vehicle never has to be revealed
  and can't be targeted (unless an item or effect says otherwise).
- A vehicle at 0 HP is destroyed and goes to the discard pile.

## 9. Losing vehicles and Territory Chips
- **A vehicle that is not the Leader is destroyed:** it goes to the discard pile, the group keeps going with one fewer attacker, and
  the pool is unchanged. **No chip.**
- **The Leader is destroyed:** the strike group is destroyed. The attacker takes **1 Territory Chip**. The group disbands:
  its owner discards **[half, rounded up]** of each resource type in the pool, and the rest returns to the Depot (assumed).
  Surviving tanks become groups of one, face down. Surviving non-tank vehicles go back to a hidden group with room, or are discarded (assumed).
- **A group of one** is its own Leader, so destroying it takes a chip and discards its whole pool.
- Chips are never lost, and each one raises your group limit by 1 (§4).

## 10. Hidden information
- Your opponent sees how many vehicles you have, which groups are revealed, and your Depot, but **not** the identity of face-down cards.
- Your opponent never sees your hand or deck order, only their sizes.

---

## Open questions
1. How the "5 available, 3 to win" chips work (see below).
2. Whether Specialist and Resupply vehicles attack at all, and whether lone vehicles other than tanks are allowed.
3. Penalty when a Leader dies, and what happens to the survivors.
4. Resupply vehicle abilities (search deck, reduce attack costs), special item rules, and where air units fit.
5. All bracketed numbers: need playtesting.
