package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.Config.DeckRules;
import com.example.valtrak.Data.GameData.DataTransfer.DeckData.DeckDtos.DeckDto;
import com.example.valtrak.Data.GameData.DataTransfer.DeckData.DeckDtos.SaveDeckRequest;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.*;
import com.example.valtrak.Data.GameData.Entity.Player;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.ApiException;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.GameNotFoundException;
import com.example.valtrak.Data.GameData.Repository.PlayerRepository;
import com.example.valtrak.Gameplay.Engine.CardSpec;
import com.example.valtrak.Gameplay.Engine.ResourceKind;
import com.example.valtrak.Gameplay.Engine.ResourceSpec;
import com.example.valtrak.Gameplay.Engine.VehicleSpec;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Plays real matches through the services against a real (in-memory) database. */
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:matchflow;DB_CLOSE_DELAY=-1")
class MatchFlowIntegrationTest {

    private static final AtomicInteger COUNTER = new AtomicInteger();

    @Autowired MatchService matches;
    @Autowired DeckService decks;
    @Autowired PlayerRepository players;
    @Autowired DbCardCatalog catalog;
    @Autowired com.example.valtrak.Data.GameData.Repository.Cards.CardRepository cards;

    private Player newPlayer() {
        int n = COUNTER.incrementAndGet();
        Player p = new Player("user" + n + "@x.com", "Player" + n, "United States", "user" + n + "@x.com");
        p.setEmailVerified(true);
        p.setPassword("unused");
        return players.save(p);
    }

    /** About 24 tanks at their copy limits, topped up with other cards (each at its limit) to about 90 cards. */
    private DeckDto playableDeck(Player owner, String name) {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        int total = 0;
        int tanks = 0;
        for (var c : cards.findAll()) {                      // 24 tanks are plenty; there are far more in the game now
            if (tanks < 24 && catalog.find(c.getId()) instanceof VehicleSpec v && v.isTank()) {
                int n = DeckRules.maxCopies(c.getLevel()); counts.put(c.getId(), n); total += n; tanks += n;
            }
        }
        for (var c : cards.findAll()) {
            if (total >= 90) break;
            if (!counts.containsKey(c.getId())) { int n = DeckRules.maxCopies(c.getLevel()); counts.put(c.getId(), n); total += n; }
        }
        return decks.create(owner.getId(), new SaveDeckRequest(name, counts));
    }

    private long aTankIn(GameView view) {
        return view.you().hand().stream()
                .filter(id -> catalog.find(id) instanceof VehicleSpec v && v.isTank()).findFirst().orElseThrow();
    }

    private ActionRequest action(String type) {
        return new ActionRequest(type, null, null, null, null, null, null, null, null);
    }

    private ActionRequest placeTank(long cardId) {
        return new ActionRequest("PLACE_STARTING_TANK", cardId, null, null, null, null, null, null, null);
    }

    /** Challenges, accepts, and has both players place their tank. */
    private Long startMatch(Player a, Player b) {
        DeckDto deckA = playableDeck(a, "A deck");
        DeckDto deckB = playableDeck(b, "B deck");
        MatchSummary pending = matches.challenge(a.getId(), new ChallengeRequest(b.getDisplayName(), deckA.id()));
        matches.accept(b.getId(), pending.id(), new AcceptRequest(deckB.id()));
        matches.act(a.getId(), pending.id(), placeTank(aTankIn(matches.view(a.getId(), pending.id()))));
        matches.act(b.getId(), pending.id(), placeTank(aTankIn(matches.view(b.getId(), pending.id()))));
        return pending.id();
    }

    // ── practice games against the computer ──────────────────────────────────

    @Test
    void aPracticeGameStartsAtOnceAndTheComputerAnswersEveryMove() {
        Player me = newPlayer();
        DeckDto deck = playableDeck(me, "Practice deck");
        MatchSummary m = matches.startBotMatch(me.getId(), new BotMatchRequest(deck.id(), "STANDARD", "AGGRESSIVE"));
        assertThat(m.status()).isEqualTo("ACTIVE");
        assertThat(m.vsBot()).isTrue();
        assertThat(m.opponentName()).startsWith("Training Bot");

        GameView view = matches.view(me.getId(), m.id());
        assertThat(view.phase()).isEqualTo("SETUP");
        assertThat(view.you().deckCards()).isNotNull().isSorted();
        assertThat(view.opponent().deckCards()).isNull();

        ActionResponse placed = matches.act(me.getId(), m.id(), placeTank(aTankIn(view)));
        assertThat(placed.view().phase()).isEqualTo("PLAYING");
        assertThat(placed.view().opponent().placedStartingTank()).as("the computer placed its tank too").isTrue();
        assertThat(placed.view().yourTurn()).as("it is always your turn again when the computer is done").isTrue();

        for (int i = 0; i < 12 && matches.view(me.getId(), m.id()).phase().equals("PLAYING"); i++) {
            ActionResponse r = matches.act(me.getId(), m.id(), action("END_TURN"));
            if (!r.view().phase().equals("FINISHED")) assertThat(r.view().yourTurn()).isTrue();
        }
        assertThat(matches.log(me.getId(), m.id(), 0)).anyMatch(l -> l.text().contains("Player 2"));
        assertThat(matches.list(me.getId())).anyMatch(s -> s.id().equals(m.id()) && s.vsBot());

        assertThat(matches.resign(me.getId(), m.id()).view().phase()).isEqualTo("FINISHED");
    }

    @Test
    void thePracticeOpponentCanUseYourDeckOrAnotherOfYourDecksAndCannotBeChallenged() {
        Player me = newPlayer();
        DeckDto deck = playableDeck(me, "Mine");
        DeckDto other = playableDeck(me, "Other");
        assertThat(matches.startBotMatch(me.getId(), new BotMatchRequest(deck.id(), "MIRROR", "CAUTIOUS")).status()).isEqualTo("ACTIVE");
        assertThat(matches.startBotMatch(me.getId(), new BotMatchRequest(deck.id(), String.valueOf(other.id()), null)).status()).isEqualTo("ACTIVE");

        assertThatThrownBy(() -> matches.startBotMatch(me.getId(), new BotMatchRequest(deck.id(), "STANDARD", "RECKLESS")))
                .isInstanceOf(ApiException.class).hasMessageContaining("AGGRESSIVE or CAUTIOUS");
        assertThatThrownBy(() -> matches.startBotMatch(me.getId(), new BotMatchRequest(deck.id(), "nonsense", null)))
                .isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> matches.startBotMatch(me.getId(), new BotMatchRequest(null, "STANDARD", null)))
                .isInstanceOf(ApiException.class);
        String botName = matches.list(me.getId()).get(0).opponentName();
        assertThatThrownBy(() -> matches.challenge(me.getId(), new ChallengeRequest(botName, deck.id())))
                .isInstanceOf(ApiException.class).hasMessageContaining("Play vs Bot");
    }

    @Test
    void savingADeckRespectsTheRarityCopyLimits() {
        Player me = newPlayer();
        var legendary = cards.findAll().stream().filter(c -> c.getLevel() == com.example.valtrak.Data.CardLibrary.CardLevel.LEGENDARY)
                .findFirst().orElseThrow();
        assertThatThrownBy(() -> decks.create(me.getId(), new SaveDeckRequest("Too many", Map.of(legendary.getId(), 2))))
                .isInstanceOf(ApiException.class).hasMessageContaining("1-1 time");
        assertThat(decks.create(me.getId(), new SaveDeckRequest("Fine", Map.of(legendary.getId(), 1))).totalCards()).isEqualTo(1);
    }

    @Test
    void theSupplyCardsAreSeededAndKnownToTheEngine() {
        long supply = cards.findAll().stream().filter(c -> c.getName().equals("3x Supply Crate")).findFirst().orElseThrow().getId();
        CardSpec spec = catalog.find(supply);
        assertThat(spec).isInstanceOf(ResourceSpec.class);
        assertThat(((ResourceSpec) spec).kind()).isEqualTo(ResourceKind.SUPPLY);
        assertThat(((ResourceSpec) spec).amount()).isEqualTo(3);
    }

    @Test
    void theCatalogKnowsTheVehiclesWithTheirAttacks() {
        VehicleSpec abrams = cards.findAll().stream().filter(c -> c.getName().equals("M1A1 Abrams"))
                .map(c -> (VehicleSpec) catalog.find(c.getId())).findFirst().orElseThrow();
        assertThat(abrams.isTank()).isTrue();
        assertThat(abrams.hp()).isEqualTo(260);
        assertThat(abrams.attacks()).hasSize(3);
        assertThat(abrams.attacks().get(0).slot().name()).isEqualTo("ATTACK_1");
    }

    @Test
    void uavAndReconCardsComeFromTheDatabaseWithTheirAbilities() {
        VehicleSpec reaper = cards.findAll().stream().filter(c -> c.getName().equals("MQ-9 Reaper Flight"))
                .map(c -> (VehicleSpec) catalog.find(c.getId())).findFirst().orElseThrow();
        assertThat(reaper.isSpecialist()).isTrue();
        assertThat(reaper.attacks()).isEmpty();
        assertThat(reaper.ability().power()).isEqualTo(3);
        assertThat(reaper.ability().fuelCost()).isEqualTo(1);

        VehicleSpec fennek = cards.findAll().stream().filter(c -> c.getName().equals("Fennek"))
                .map(c -> (VehicleSpec) catalog.find(c.getId())).findFirst().orElseThrow();
        assertThat(fennek.vehicleClass().name()).isEqualTo("RECON");
        assertThat(fennek.attacks()).hasSize(1);
        assertThat(fennek.ability().power()).isEqualTo(2);

        VehicleSpec abrams = cards.findAll().stream().filter(c -> c.getName().equals("M1A1 Abrams"))
                .map(c -> (VehicleSpec) catalog.find(c.getId())).findFirst().orElseThrow();
        assertThat(abrams.ability()).isNull();
    }

    @Test
    void aChallengeIsAcceptedAndTheMatchStarts() {
        Player a = newPlayer(), b = newPlayer();
        DeckDto deckA = playableDeck(a, "Deck A"), deckB = playableDeck(b, "Deck B");

        MatchSummary pending = matches.challenge(a.getId(), new ChallengeRequest(b.getDisplayName().toLowerCase(), deckA.id()));
        assertThat(pending.status()).isEqualTo("PENDING");
        assertThat(pending.youChallenged()).isTrue();
        assertThat(pending.yourTurn()).isFalse();
        assertThat(matches.list(b.getId())).singleElement().satisfies(m -> {
            assertThat(m.opponentName()).isEqualTo(a.getDisplayName());
            assertThat(m.yourTurn()).isTrue();                       // waiting for B's answer
        });
        assertThatThrownBy(() -> matches.view(a.getId(), pending.id()))
                .isInstanceOf(ApiException.class).hasMessageContaining("hasn't started");

        MatchSummary active = matches.accept(b.getId(), pending.id(), new AcceptRequest(deckB.id()));
        assertThat(active.status()).isEqualTo("ACTIVE");

        GameView mine = matches.view(a.getId(), pending.id());
        GameView theirs = matches.view(b.getId(), pending.id());
        assertThat(mine.phase()).isEqualTo("SETUP");
        assertThat(mine.you().hand()).hasSizeGreaterThanOrEqualTo(7);
        assertThat(mine.opponent().hand()).isNull();
        assertThat(theirs.you().displayName()).isEqualTo(b.getDisplayName());
        assertThat(mine.you().hand()).isNotEqualTo(theirs.you().hand());
    }

    @Test
    void onlyTheChallengedPlayerCanAcceptAndOnlyOnce() {
        Player a = newPlayer(), b = newPlayer(), c = newPlayer();
        DeckDto deckA = playableDeck(a, "Deck A"), deckB = playableDeck(b, "Deck B");
        MatchSummary pending = matches.challenge(a.getId(), new ChallengeRequest(b.getDisplayName(), deckA.id()));

        assertThatThrownBy(() -> matches.accept(a.getId(), pending.id(), new AcceptRequest(deckA.id())))
                .isInstanceOf(ApiException.class);                    // the challenger can't accept their own challenge
        assertThatThrownBy(() -> matches.accept(c.getId(), pending.id(), new AcceptRequest(deckB.id())))
                .isInstanceOf(GameNotFoundException.class);           // outsiders see nothing
        matches.accept(b.getId(), pending.id(), new AcceptRequest(deckB.id()));
        assertThatThrownBy(() -> matches.accept(b.getId(), pending.id(), new AcceptRequest(deckB.id())))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void challengeRulesAreEnforced() {
        Player a = newPlayer(), b = newPlayer();
        DeckDto deckA = playableDeck(a, "Deck A");
        Map<Long, Integer> tiny = Map.of(cards.findAll().get(0).getId(), 1);
        DeckDto draft = decks.create(a.getId(), new SaveDeckRequest("Draft", tiny));

        assertThatThrownBy(() -> matches.challenge(a.getId(), new ChallengeRequest(a.getDisplayName(), deckA.id())))
                .isInstanceOf(ApiException.class).hasMessageContaining("yourself");
        assertThatThrownBy(() -> matches.challenge(a.getId(), new ChallengeRequest("Nobody Here", deckA.id())))
                .isInstanceOf(ApiException.class).hasMessageContaining("No player");
        assertThatThrownBy(() -> matches.challenge(a.getId(), new ChallengeRequest(b.getDisplayName(), draft.id())))
                .isInstanceOf(ApiException.class).hasMessageContaining("isn't playable");
        matches.challenge(a.getId(), new ChallengeRequest(b.getDisplayName(), deckA.id()));
        assertThatThrownBy(() -> matches.challenge(a.getId(), new ChallengeRequest(b.getDisplayName(), deckA.id())))
                .isInstanceOf(ApiException.class).hasMessageContaining("already challenged");
    }

    @Test
    void declinedAndCancelledChallengesDisappear() {
        Player a = newPlayer(), b = newPlayer(), c = newPlayer();
        DeckDto deckA = playableDeck(a, "Deck A");
        MatchSummary one = matches.challenge(a.getId(), new ChallengeRequest(b.getDisplayName(), deckA.id()));
        MatchSummary two = matches.challenge(a.getId(), new ChallengeRequest(c.getDisplayName(), deckA.id()));
        matches.decline(b.getId(), one.id());
        matches.cancel(a.getId(), two.id());
        assertThat(matches.list(a.getId())).isEmpty();
        assertThat(matches.list(b.getId())).isEmpty();
    }

    @Test
    void movesAreSavedAndEachPlayerOnlySeesWhatTheyShould() {
        Player a = newPlayer(), b = newPlayer();
        Long id = startMatch(a, b);

        GameView viewA = matches.view(a.getId(), id);
        GameView viewB = matches.view(b.getId(), id);
        assertThat(viewA.phase()).isEqualTo("PLAYING");
        int first = viewA.activePlayer();
        Player firstPlayer = first == viewA.youIndex() ? a : b;
        Player secondPlayer = firstPlayer == a ? b : a;

        // each player's own tank is visible to them and hidden from the other
        assertThat(viewA.you().groups()).hasSize(1);
        assertThat(viewA.you().groups().get(0).vehicles().get(0).cardId()).isNotNull();
        assertThat(viewA.opponent().groups().get(0).vehicles().get(0).cardId()).isNull();
        assertThat(viewB.opponent().groups().get(0).vehicles().get(0).cardId()).isNull();

        // the player whose turn it isn't can't move; an illegal move is a 400 with the reason
        assertThatThrownBy(() -> matches.act(secondPlayer.getId(), id, action("END_TURN")))
                .isInstanceOf(ApiException.class).hasMessageContaining("not your turn");
        assertThatThrownBy(() -> matches.act(firstPlayer.getId(), id,
                new ActionRequest("ATTACK", null, 1L, null, null, null, null, null,
                        List.of(new AttackChoiceRequest(1L, "ATTACK_1", null, 2L)))))
                .isInstanceOf(ApiException.class);

        long before = matches.view(firstPlayer.getId(), id).version();
        ActionResponse response = matches.act(firstPlayer.getId(), id, action("END_TURN"));
        assertThat(response.log()).isNotEmpty();
        assertThat(response.view().yourTurn()).isFalse();
        assertThat(response.view().version()).isGreaterThan(before);
        assertThat(matches.view(secondPlayer.getId(), id).yourTurn()).isTrue();

        // the log is shared and can be read from a point onward
        List<LogLine> log = matches.log(a.getId(), id, 0);
        assertThat(log).isNotEmpty();
        assertThat(matches.log(b.getId(), id, log.get(log.size() - 1).seq())).isEmpty();
    }

    @Test
    void outsidersCannotSeeOrTouchAMatch() {
        Player a = newPlayer(), b = newPlayer(), outsider = newPlayer();
        Long id = startMatch(a, b);
        assertThatThrownBy(() -> matches.view(outsider.getId(), id)).isInstanceOf(GameNotFoundException.class);
        assertThatThrownBy(() -> matches.log(outsider.getId(), id, 0)).isInstanceOf(GameNotFoundException.class);
        assertThatThrownBy(() -> matches.act(outsider.getId(), id, action("END_TURN"))).isInstanceOf(GameNotFoundException.class);
        assertThatThrownBy(() -> matches.resign(outsider.getId(), id)).isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void resigningEndsTheMatchAndRecordsTheWinner() {
        Player a = newPlayer(), b = newPlayer();
        Long id = startMatch(a, b);
        ActionResponse response = matches.resign(a.getId(), id);
        assertThat(response.view().phase()).isEqualTo("FINISHED");
        assertThat(matches.list(a.getId())).singleElement().satisfies(m -> assertThat(m.result()).isEqualTo("LOST"));
        assertThat(matches.list(b.getId())).singleElement().satisfies(m -> assertThat(m.result()).isEqualTo("WON"));
        assertThatThrownBy(() -> matches.act(b.getId(), id, action("END_TURN"))).isInstanceOf(ApiException.class);
    }
}
