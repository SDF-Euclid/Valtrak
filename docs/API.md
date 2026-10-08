# Valtrak server API (matches)

All `/matches` and `/decks` calls need a signed-in player: send `Authorization: Bearer <token>` (from `/account/login`).
Errors come back as plain text with an HTTP status (400 = the move or request isn't allowed, 404 = not found, 409 = wrong state).

## Challenges
| Call | What it does |
|---|---|
| `POST /matches/challenge` `{opponentName, deckId}` | Invite a player (by display name) using one of your playable decks. Max 5 unanswered invites. |
| `POST /matches/{id}/accept` `{deckId}` | The invited player accepts with one of their decks; the game is dealt and starts in SETUP. |
| `POST /matches/{id}/decline` | The invited player declines. |
| `POST /matches/{id}/cancel` | The challenger withdraws a pending invite. |
| `GET /matches` | Your matches: `status` (PENDING, ACTIVE, FINISHED), opponent, `yourTurn`, `result` (WON/LOST). |

## Playing
| Call | What it does |
|---|---|
| `GET /matches/{id}` | The game as **you** may see it (`GameView`): your hand, but only the size of theirs; face-down enemy vehicles show no identity. Poll it and compare `version`. |
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
| `ATTACK` | `groupId`, `choices`: `[{vehicleId, slot, ammo, targetVehicleId}]` (one choice = Skirmish, two or more = Combined Assault) |
| `END_TURN` | |

Cards in your hand are identified by **card id** (the ids from `GET /cards`); things on the table are identified by their **field id**
(vehicle, group and resource-card ids in the `GameView`).

## Not built yet
- Pushing updates to the other player (right now the client polls `GET /matches/{id}`).
- A turn timer, and matchmaking without naming an opponent.
- Log lines say "Player 1/2"; the client can swap in display names using `youIndex`.
