# Playing Valtrak (desktop client)

Sign in (practice games and challenges are saved to your account), then press **PLAY GAME**.

## The lobby
- **Your deck:** the deck you take into a game. It has to be playable (60-100 cards, at least 12 tanks). Build decks in the Deck Builder.
- **Practice against the computer:** pick the computer's deck (a standard one the server builds, a mirror of yours, or any of your decks) and its style
  (Aggressive reveals and attacks as soon as it can; Cautious only reveals when it can attack straight away). Use it to test and tune decks.
  The computer can see the whole game, so it is a sparring partner, not a fair opponent.
- **Challenge a player:** type their display name. They accept in their own lobby with one of their decks.
- **Your games:** open a game, accept or decline a challenge, or cancel one you sent. The list refreshes every few seconds.

## The board
Your opponent is at the top, you are below, and your hand is along the bottom. The log is on the right. Everything you can do is a click:
- **A card in your hand:** a menu of what it can do (put a resource in the Depot or a group's pool, deploy a vehicle, play an item). Items that need
  targets (ERA, Camouflage, Smoke Screen, Artillery, Jammer) highlight the valid targets: click them, then **Confirm**. Search, Recycle and Rapid
  Deployment open a list to choose from.
- **One of your vehicles:** reveal it, retreat it (costs Fuel), use its ability (UAV and Recon: pick the enemy vehicles to reveal), move it to another
  group, or repair it with a card from your Depot.
- **A group heading:** reveal or retreat the whole group, convoy cards from the Depot into the pool, switch its Jammer on or off, or **attack**.
- **Attack:** tick the vehicles that attack, choose each one's attack, ammunition and target. One vehicle is a Skirmish; two or more are a Combined
  Assault. The cost is shown and paid from the group's pool. Attacking ends your turn.
- **End turn** is the big button on the right. Your turn also ends when you attack.
- The server checks every move. If something isn't allowed, its reason appears under the log.

Setup: click a tank in your hand to place it as your starting tank. Hover over any card or vehicle to see its numbers.

The client holds no rules: it shows what the server sends (your hand and deck list, but never the opponent's hand or face-down vehicles) and sends moves.
See `docs/RULEBOOK.md` for the rules and `docs/API.md` for the server calls.
