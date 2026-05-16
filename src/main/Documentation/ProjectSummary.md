# Valtrak — Project Summary

## What is Valtrak?
Valtrak is a 2-player turn-based card game with a military theme, built as a Spring Boot backend with a MySQL database. Players build decks of vehicle, item, event, and terrain cards and battle each other in structured turns. The game is Magic: The Gathering adjacent but distinct enough to stand on its own.

---

## Tech Stack
- **Backend:** Java, Spring Boot 4.x, Spring Security, Spring Data JPA
- **Database:** MySQL with Hibernate (ddl-auto: update)
- **Build:** Maven
- **Utilities:** Lombok, JUnit 5, Mockito
- **Password hashing:** Argon2 (via spring-security-crypto)
- **API docs:** SpringDoc OpenAPI (Swagger UI)

---

## Project Package Structure

```
com.example.valtrak
├── Data
│   ├── CardLibrary
│   │   ├── CardLevel.java               (COMMON, UNCOMMON, RARE, LEGENDARY)
│   │   ├── Nations.java                 (enum of all 195 real-world nations)
│   │   ├── Enums
│   │   │   ├── SupplyInfo
│   │   │   │   ├── ItemType.java        (AMMUNITION, FUEL, SUPPLY, REPAIR, SPECIAL)
│   │   │   │   ├── AmmoSupplyCrate.java
│   │   │   │   ├── FuelSupplyCrate.java
│   │   │   │   └── MaintenanceSupplyCrate.java
│   │   │   ├── VehicleInfo
│   │   │   │   ├── ArmorBracket.java    (UNARMORED, LIGHT, MEDIUM, HEAVY, SUPER_HEAVY)
│   │   │   │   ├── VehicleClass.java    (LIGHT_TANK, MAIN_BATTLE_TANK, etc.)
│   │   │   │   └── VehicleType.java     (GROUND, AIR, WATER, DEEP_SEA)
│   │   │   └── WeaponInfo
│   │   │       ├── Ammunition.java      (ammo types with damage type + caliber)
│   │   │       ├── AttackSlot.java      (ATTACK_1, ATTACK_2, ATTACK_3)
│   │   │       ├── DamageType.java      (KINETIC, CHEMICAL, EXPLOSIVE, ELECTRIC)
│   │   │       ├── SpecialEffect.java   (NONE, PIERCE, STUN, SUPPRESSION, DISABLE, BREACH, OVERPRESSURE)
│   │   │       └── Weapon.java          (all weapons with compatible ammo list)
│   │   ├── Interfaces
│   │   │   ├── Items
│   │   │   │   ├── ItemCardInterface.java
│   │   │   │   └── AmmunitionItemInterface.java (extends ItemCardInterface)
│   │   │   └── Vehicle
│   │   │       ├── GroundVehicleCardInterface.java
│   │   │       ├── AirVehicleCardInterface.java  (future)
│   │   │       └── VehicleAttackInterface.java
│   │   └── Vehicles
│   │       ├── US
│   │       │   └── USGroundVehicles.java
│   │       ├── Russia
│   │       │   └── RussianVehicles.java
│   │       └── Germany
│   │           └── GermanVehicles.java
│   └── GameData
│       ├── Config
│       │   ├── Annotation
│       │   │   ├── ValidNation.java
│       │   │   └── NationValidator.java
│       │   ├── Records
│       │   │   └── VehicleAttackDefinition.java  (record implementing VehicleAttackInterface)
│       │   ├── ArmorBracketHelper.java            (static combat utility methods)
│       │   ├── DataLoader.java                    (seeds DB on startup)
│       │   └── SecurityConfiguration.java
│       ├── Controller
│       │   └── PlayerController.java
│       ├── DataTransfer
│       │   ├── DamageData
│       │   │   └── CombatResult.java
│       │   └── PlayerData
│       │       ├── AccountCreation
│       │       │   ├── CreateAccountRequest.java
│       │       │   └── CreateAccountResponse.java
│       │       └── AccountUpdate
│       │           └── UpdateAccountRequest.java
│       ├── Entity
│       │   ├── EnumEntity                         (JPA entities for enum constants)
│       │   │   ├── AmmunitionEntity.java
│       │   │   ├── DamageTypeEntity.java
│       │   │   ├── DamageTypeMatchupEntity.java
│       │   │   ├── NationEntity.java
│       │   │   ├── VehicleAttackEntity.java
│       │   │   ├── VehicleClassEntity.java
│       │   │   ├── VehicleTypeEntity.java
│       │   │   └── WeaponEntity.java
│       │   └── Player.java
│       ├── ExceptionHandling
│       │   ├── Exceptions
│       │   │   └── PlayerNotFoundException.java
│       │   └── GlobalExceptionHandler.java
│       ├── Repository
│       │   ├── Cards
│       │   │   ├── VehicleCardRepository.java
│       │   │   └── AmmunitionCardRepository.java
│       │   ├── EnumData
│       │   │   ├── AmmunitionRepository.java
│       │   │   ├── DamageTypeRepository.java
│       │   │   ├── DamageTypeMatchupRepository.java
│       │   │   ├── NationRepository.java
│       │   │   ├── VehicleAttackRepository.java
│       │   │   ├── VehicleClassRepository.java
│       │   │   ├── VehicleTypeRepository.java
│       │   │   └── WeaponRepository.java
│       │   └── PlayerRepository.java
│       └── Service
│           ├── CombatService.java
│           └── PlayerService.java
└── Gameplay
    └── Cards
        ├── Base
        │   ├── Card.java      (abstract, JOINED inheritance, table: cards)
        │   └── ItemCard.java  (abstract, extends Card, table: item_cards)
        ├── Resource
        │   └── AmmunitionCard.java  (extends ItemCard, table: ammunition_card)
        └── Vehicle
            └── GroundVehicleCard.java  (extends Card, table: vehicle_card)
```

---

## Database Design

### Two-layer approach
- **Card Library tables** — static definitions seeded on startup from enums
- **Game State tables** — live match data (not yet implemented)

### Seeding order (DataLoader.java)
```
DamageTypes → DamageTypeMatchups → VehicleTypes → VehicleClasses
→ Ammunition → Weapons → Nations → Ground Vehicles → Vehicle Attacks
→ Ammunition Cards
```

### Key tables
```
cards                  — base card fields (id, name, description, level)
vehicle_card           — vehicle-specific fields (nation, armor, class, type)
vehicle_attacks        — attack slots per vehicle (name, slot, weapon, damage, costs, effect)
item_cards             — item-specific fields (item_type)
ammunition_card        — ammo card fields (ammunition enum, count)
weapons                — weapon definitions
weapon_ammunition      — join table (weapon ↔ ammo)
ammunition             — ammo type definitions (name, damage_type_id)
damage_types           — KINETIC, CHEMICAL, EXPLOSIVE, ELECTRIC
damage_type_matchups   — modifier + auto effect per damage type per armor bracket
vehicle_classes        — LIGHT_TANK, MAIN_BATTLE_TANK, etc.
vehicle_types          — GROUND, AIR, etc.
nations                — 195 nations (name + ISO abbreviation)
players                — player accounts
```

---

## Card Hierarchy

```
Card (abstract, @Entity)
├── GroundVehicleCard       — deployed units on the field
│   (future) AirVehicleCard
│   (future) NavalVehicleCard
└── ItemCard (abstract, @Entity)
    ├── AmmunitionCard      — resupplies a specific ammo type
    │   (future) FuelCard
    │   (future) SupplyCard
    │   (future) RepairCard
    │   (future) SpecialCard
    (future) EventCard
    (future) TerrainCard
```

All card subclasses use `InheritanceType.JOINED` — each gets its own table linked to `cards` by id.

---

## Nation Vehicle Pattern

Each nation has its own enum implementing `GroundVehicleCardInterface`:
- `USGroundVehicles.java`
- `RussianVehicles.java`
- `GermanVehicles.java`

Adding a new nation = create a new enum + one line in DataLoader:
```java
loadGroundVehicles(NewNationVehicles.values());
```

Each vehicle defines up to 3 attack slots via `VehicleAttackDefinition` records:
```java
new VehicleAttackDefinition(attackName, attackSlot, weapon, baseDamage, ammoCost, fuelCost, specialEffect)
```

---

## Vehicle Stats Baseline

```
M1A3 Abrams — armor 100, HP 300, LEGENDARY (baseline)

Armor brackets:
UNARMORED    0-30    → HP guideline: 100-150
LIGHT        31-70   → HP guideline: 150-225
MEDIUM       71-120  → HP guideline: 225-325
HEAVY        121-180 → HP guideline: 325-425
SUPER_HEAVY  181+    → HP guideline: 425-500
```

---

## Damage System

### Formula
```
effectiveArmor = baseArmor * (0.88 ^ breachStacks)   [max 3 stacks]
finalDamage = max(1, round(baseDamage * modifier) - round(effectiveArmor * 0.15))
```

### Damage type modifiers (vs armor bracket)
```
             UNARMORED  LIGHT   MEDIUM  HEAVY   SUPER_HEAVY
KINETIC        0.6x     0.8x    1.0x    1.3x      1.5x
CHEMICAL       1.0x     1.0x    1.0x    1.0x      1.0x
EXPLOSIVE      1.8x     1.4x    0.7x    0.4x      0.2x
ELECTRIC       0.0x     0.0x    0.0x    0.0x      0.0x
```

### Overpressure (true damage, bypasses armor reduction)
```
EXPLOSIVE triggers when:
  bracket <= MEDIUM AND caliber >= armor * 1.5

CHEMICAL triggers when:
  bracket <= LIGHT AND caliber >= armor * 3.0

KINETIC and ELECTRIC never trigger overpressure
```

### Special effects (caliber-based)
```
PIERCE      → KINETIC, bracket >= HEAVY, caliber >= armor * 0.8
               ignores armor reduction entirely
BREACH      → KINETIC, bracket == UNARMORED, caliber >= armor * 3.0
               -12% armor multiplicatively, max 3 stacks, permanent
SUPPRESSION → KINETIC, bracket <= LIGHT, caliber < armor
               unit cannot use ATTACK_1 next turn
DISABLE     → ELECTRIC vs any
               unit cannot attack, can still move with Strike Group
STUN        → EXPLOSIVE auto-effect vs UNARMORED and LIGHT
               unit skips next turn entirely
```

### DISABLE + SUPPRESSION stacked
Unit cannot attack AND cannot move independently.

---

## Turn Structure
```
START OF TURN
├── Draw 1 card (more with special item cards)
└── Resource pool resets to 0

LOGISTICS PHASE   — play resource/item cards from hand to pool
COMMAND PHASE     — deploy vehicles (costs SUPPLY), use items
ENGAGEMENT PHASE  — attack with field units (costs AMMO/FUEL per slot)

END OF TURN
├── Resource pool → 0
├── Hand stays as-is (no forced discard)
└── Field stays as-is
```

---

## Resource System
- Resources are cards played from hand, NOT an auto-refreshing pool
- Unplayed resource cards stay in hand between turns
- Only the resource pool resets to 0 each turn

```
AMMO    — powers attacks
FUEL    — powers movement and abilities
SUPPLY  — pays deploy costs
REPAIR  — restores unit HP
```

Item card quantities:
```
COMMON    → +1 resource / small ammo count
UNCOMMON  → +2-5 resource / medium ammo count
RARE      → +3-10 resource / large ammo count
LEGENDARY → +20 resource / special effects
```

---

## Strike Groups (partially designed, not yet implemented)
- Collection of units with same typing (unless specified otherwise)
- Max size: 5 units (under consideration)
- Provides attacking/resupply buffs to all included units
- DISABLEd units can still move with their Strike Group

---

## Player Accounts
- Stored in `players` table
- Fields: userName, displayName, displayNation (FK to NationEntity), email, password (Argon2 hashed)
- `@JsonProperty(WRITE_ONLY)` on password — never returned in responses
- `@ValidNation` custom annotation validates displayNation against nations table

### Implemented endpoints
```
POST /player/create   — creates a new player account
```

---

## Security
- Spring Security with Argon2 password encoding
- `/player/create` is public, all other endpoints require authentication
- CSRF disabled for API use
- Credentials stored in `application-local.properties` (gitignored)

---

## Game State Layer (not yet implemented)
Planned entities:
```
Game             — tracks two players, turn number, game status, active terrain
PlayerGameState  — hand, deck, discard pile, resource pools, deployed units
FieldUnit        — a deployed vehicle with current HP and breach stacks
```

PlayerGameState is separate from Player (account) — game state is created at match start and deleted at match end. Player entity only holds permanent account data.

---

## Currently Implemented Cards

### United States
- M1A3 Abrams (LEGENDARY, MAIN_BATTLE_TANK)
- More in progress

### Russia
- One vehicle in progress

### Germany
- One vehicle in progress

### Item Cards
- AmmoSupplyCrate enum (12 entries: 1x/5x/10x for APFSDS_120mm, APFSDS_125mm, HEAT_120mm, HEAT_125mm)
- AmmunitionCard entity seeded and working

---

## Still To Design/Implement
```
[ ] More vehicle cards (US, Russia, Germany + future nations)
[ ] FuelCard, SupplyCard, RepairCard, SpecialCard entities + enums
[ ] EventCard and TerrainCard card types
[ ] Strike Group mechanics
[ ] Item cards (draw extra, deck search, temp armor boost, etc.)
[ ] Deck building rules (max size, copy limits, nation rules)
[ ] Win condition
[ ] ERA item interaction with CHEMICAL damage
[ ] Game state layer (Game, PlayerGameState, FieldUnit)
[ ] More REST endpoints (login, deck management, game actions)
[ ] Frontend
```

---

## Key Architectural Decisions
1. **Enum-first card definitions** — all card data lives in enums, seeded to DB on startup via DataLoader. Adding cards never requires touching DB directly.
2. **Interface-per-vehicle-type** — `GroundVehicleCardInterface`, future `AirVehicleCardInterface` etc. allow the DataLoader to handle all nations generically.
3. **JOINED inheritance** — each card subtype gets its own table, clean separation of concerns.
4. **Two-repo pattern** — `AmmunitionEntity` (ammo type definitions) vs `AmmunitionCard` (playable cards) are completely separate repositories and tables.
5. **`PlayerGameState` separate from `Player`** — account data persists forever, game state is ephemeral.
6. **Caliber-based effect triggers** — special effects like PIERCE, BREACH, OVERPRESSURE trigger based on ammo caliber relative to target armor, not hardcoded weapon types.