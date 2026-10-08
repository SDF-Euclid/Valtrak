# Valtrak server API (matches)

All `/matches` and `/decks` calls need a signed-in player: send `Authorization: Bearer <token>` (from `/account/login`).
Errors come back as plain text with an HTTP status (400 = the move or request isn't allowed, 404 = not found, 409 = wrong state).

## Challenges
| Call | What it does |
|---|---|
| `POST /matches/challenge` `{opponentName, deckId}` | Invite a player (by display name) using one of your playable decks. Max 5 unanswered invites. |
| `POST /matches/bot` `{deckId, botDeck, style}` | A **practice game against the computer**: starts at once (no invite). `botDeck` is `STANDARD` (a 100-card deck the server builds), `MIRROR` (a copy of your deck) or the id of another of your decks; `style` is `AGGRESSIVE` or `CAUTIOUS`. The computer plays its own moves as part of each of yours, so every response is already back at your turn. It plays from the whole game state (it can see your hand), so it is a sparring partner, not a fair opponent. It can't be challenged by name. |
| `POST /matches/{id}/accept` `{deckId}` | The invited player accepts with one of their decks; the game is dealt and starts in SETUP. |
| `POST /matches/{id}/decline` | The invited player declines. |
| `POST /matches/{id}/cancel` | The challenger withdraws a pending invite. |
| `GET /matches` | Your matches: `status` (PENDING, ACTIVE, FINISHED), opponent, `yourTurn`, `result` (WON/LOST). |

## Playing
| Call | What it does |
|---|---|
| `GET /matches/{id}` | The game as **you** may see it (`GameView`): your hand, but only the size of theirs; face-down enemy vehicles show no identity, and an opponent's group lists its face-up vehicles first, then the face-down ones in a scrambled order (so the Leader can't be picked out). Each vehicle has `eraCardId` and `camoCardId` (hidden on face-down enemies) and `smoked` (public); each group has `jammer` (`id`, `cardId`, `hp`, `maxHp`, `on`): always shown for your own groups, and for an enemy group only while it is on. While on, its `id` can be used as `targetVehicleId` in an `ATTACK` choice. Your own `deckCards` lists the cards left in your deck, sorted (so you can pick for a Search card without seeing the order). Poll it and compare `version`. |
| `POST /matches/{id}/actions` `ActionRequest` | Make a move. Returns the new `GameView` and the log lines it produced. |
| `POST /matches/{id}/resign` | Give up (any time, even out of turn). |
| `GET /matches/{id}/log?after=N` | Game log lines after sequence number N. |

Matches you are not in look exactly like matches that don't exist (404).

## Moves (`ActionRequest`)
`type` plus only the fields that move needs:

| type | fields |
|---|---|
| `PLACE_STARTING_TANK` (setup) | `cardId` |
| `DESIGNATE` | `cardId`, `groupId` (omit = your Depot) |
| `DEPLOY` | `cardId`, `groupId` (omit = new group of one, tanks only) |
| `CONVOY` | `groupId`, `resourceIds` |
| `REPAIR` | `resourceId`, `vehicleId` |
| `REVEAL` | `vehicleIds` |
| `REVEAL_GROUP` | `groupId` |
| `RETREAT` | `vehicleId` |
| `RETREAT_GROUP` | `groupId` |
| `MOVE` | `vehicleId`, `toGroupId` (omit = out to a new group, tanks only) |
| `USE_ABILITY` | `vehicleId` (the UAV/Recon vehicle), `vehicleIds` (the face-down enemy vehicles to reveal) |
| `PLAY_ITEM` | `cardId` (the item card in your hand). ERA: `vehicleIds` = one of your vehicles. Artillery: `vehicleIds` = the enemy vehicles to hit (face-up ones; Legendary may also pick face-down ones). Search: `cardIds` = the cards to take from your deck (up to the card's limit, may be empty). Draw, Sabotage: nothing else. Smoke Screen: `vehicleIds` = your vehicles. Camouflage: `vehicleIds` = one of your vehicles. Jammer: `groupId` = one of your strike groups. Recycle: `cardIds` = resource cards in your discard pile. Rapid Deployment: `groupId` = your strike group, `cardIds` = vehicle cards in your hand |
| `JAMMER_ON` / `JAMMER_OFF` | `groupId` (switch that group's Jammer on or off, free) |
| `ATTACK` | `groupId`, `choices`: `[{vehicleId, slot, ammo, targetVehicleId}]` (one choice = Skirmish, two or more = Combined Assault) |
| `END_TURN` | |

Cards in your hand are identified by **card id** (the ids from `GET /cards`); things on the table are identified by their **field id**
(vehicle, group and resource-card ids in the `GameView`).

## Not built yet
- Pushing updates to the other player (right now the client polls `GET /matches/{id}`).
- A turn timer, and matchmaking without naming an opponent.
- Log lines say "Player 1/2"; the client can swap in display names using `youIndex`.
