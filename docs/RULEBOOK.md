# Valtrak Rulebook: DRAFT v0.1

Values in **[brackets]** are tunable numbers I picked as a starting point. Lines marked **(assumed)** are gaps I filled in; please veto or change them.

## 1. Goal
Be the first player to hold **[5]** Territory Chips. You earn chips by destroying enemy vehicles (see §8).
You also lose if you must draw from an empty deck.

## 2. Decks
- **[50–80]** cards, at most **3 copies** of any card.
- A deck is playable only if it has at least one vehicle that can be a Leader (see §4).

## 3. Card types
| Type | Notes |
|---|---|
| Vehicle | Has HP, armor and up to three attacks (ATTACK_1/2/3). Rarity sets its value. |
| Resource | **Ammo** (a specific ammunition type), **Fuel**, **Supply**, **Repair**. |
| Special item | Air Strike, Smoke Screen, extra draws, etc. (rules per card). |

## 4. Strike groups
A strike group is up to **5** vehicles with these slots:

| Slot | Count | Allowed vehicles |
|---|---|---|
| Leader | exactly 1 | a tank: Light, Medium, Heavy or Main Battle Tank |
| Line | up to 2 | any non-specialist vehicle |
| Specialist | up to 1 | Specialist class vehicle |
| Resupply | up to 1 | Supply class vehicle (convoy; abilities to come) |

- A group is **hidden** (face down) or **revealed** (face up).
- Units can be added to or removed from a group **only while it is hidden**.
- Group limit: **3** at a time, **+1 for each Territory Chip you hold [max +2]** (assumed cap; see Suggestions).
- If the Leader is destroyed, the group is **disbanded**: survivors become independent units, face down (assumed).

## 5. Where cards live
- **Hand**, **Deck**, **Discard**.
- **Depot**: your stockpile of Supply and Repair (persists between turns).
- **Field**: your strike groups, plus **independent units** (deployed vehicles not in a group, face down, max **[5]**, assumed).
- Ammo and Fuel are held in a **pool** that belongs to a strike group (shared by its units) or to an independent unit.
  Pools persist between turns until spent.

## 6. Setup
1. Shuffle, draw **7**. Your hand must contain a vehicle (mulligan otherwise).
2. Each player puts one vehicle from hand onto the field as an independent unit, **face down**.
3. A coin flip decides who goes first. Nobody can attack on their own first turn.

## 7. Your turn
1. **Draw 1 card** (items or field conditions can add more). The first player also draws on turn 1.
2. **Main step.** Do any of these in any order, as often as the rules allow:
   - **Designate a resource**: put one Resource card from your hand into play, **[1] per turn** (items can raise this).
     Ammo and Fuel go into a chosen group's pool or an independent unit's pool. Supply and Repair go to your Depot.
   - **Deploy a vehicle** from your hand onto the field as an independent unit, face down. **No resource cost.**
   - **Form a strike group** by naming an independent unit as Leader. Cost: **Supply from your Depot**,
     by Leader rarity: Common/Uncommon **[1]**, Rare/Epic **[2]**, Legendary/Commander **[3]**.
   - **Organize**: move units into or out of a hidden group (slot limits in §4); no cost.
   - **Reveal** a group or an independent unit: it turns face up. No cost. Revealed units can attack and be attacked.
   - **Retreat a unit**: cost **[1 Fuel]** from its pool. It leaves its group (if any) and goes face down as an independent unit.
   - **Play item cards** (Repair, specials).
3. **Attack, or pass.** Attacking **ends your turn** immediately. If you can't or won't attack, you choose **End Turn**.

## 8. Attacking
- Choose one **revealed** group (or one revealed independent unit) as the attacker.
- **Group attack (assumed):** each unit in the group may use **one** of its attacks. All Ammo (by type) and Fuel costs are
  added up and must be in the group's pool; they are spent. An independent unit uses its own pool.
- Each attack targets one **revealed** enemy unit. Damage uses the existing damage-type, armor and special-effect rules.
- Units reduced to 0 HP are destroyed and go to the discard pile.

## 9. Territory Chips
- Destroying a vehicle gives chips by rarity **[Common–Rare: 1, Epic–Legendary: 2, Commander: 3]**.
- Wiping out an entire strike group gives **[+1]** bonus chip.
- Chips are never lost. Each chip also raises your strike-group limit (§4).

## 10. Hidden information
- Your opponent sees how many units you have and whether a group is revealed, but **not** the identity of face-down cards.
- Your opponent never sees your hand or deck order, only their sizes.

---

## Not decided yet
- What the Resupply vehicle's convoy abilities do (search deck, reduce attack costs).
- Exact numbers for chips, costs, retreat, caps, and attack balance, which need playtesting.
- Special item rules, and where air units and anti-air fit in the group slots.
