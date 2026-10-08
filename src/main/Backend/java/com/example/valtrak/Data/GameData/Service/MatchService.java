package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.*;
import com.example.valtrak.Data.GameData.Entity.MatchLogEntry;
import com.example.valtrak.Data.GameData.Entity.MatchRecord;
import com.example.valtrak.Data.GameData.Entity.Player;
import com.example.valtrak.Data.GameData.Enums.MatchStatus;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.ApiException;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.GameNotFoundException;
import com.example.valtrak.Data.GameData.Repository.MatchLogRepository;
import com.example.valtrak.Data.GameData.Repository.MatchRepository;
import com.example.valtrak.Data.GameData.Repository.PlayerRepository;
import com.example.valtrak.Gameplay.Engine.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.security.SecureRandom;
import java.util.List;

/**
 * Matches between two players: challenge, accept, play moves, resign.
 * The rules themselves live in {@link GameEngine}; this class handles who is allowed to do what,
 * and saving the game after every move.
 */
@Service
@RequiredArgsConstructor
public class MatchService {

    static final int MAX_PENDING_CHALLENGES_SENT = 5;
    static final int MAX_ACTIVE_MATCHES = 10;

    private final MatchRepository matches;
    private final MatchLogRepository logs;
    private final PlayerRepository players;
    private final DeckService decks;
    private final GameEngine engine;

    private final JsonMapper json = JsonMapper.builder().build();
    private final SecureRandom random = new SecureRandom();

    // ── Challenges ───────────────────────────────────────────────────────────

    @Transactional
    public MatchSummary challenge(Long callerId, ChallengeRequest req) {
        if (req == null || req.opponentName() == null || req.opponentName().isBlank() || req.deckId() == null) {
            throw bad("Choose an opponent (display name) and one of your decks.");
        }
        Player opponent = players.findByDisplayNameIgnoreCase(req.opponentName().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No player is called \"" + req.opponentName().trim() + "\"."));
        if (opponent.getId().equals(callerId)) throw bad("You can't challenge yourself.");
        if (!opponent.isEmailVerified()) throw new ApiException(HttpStatus.NOT_FOUND, "No player is called \"" + req.opponentName().trim() + "\".");
        Player me = player(callerId);

        List<Long> deck = decks.expandedCards(callerId, req.deckId());
        requirePlayable(deck, "Your deck");
        if (matches.countByPlayer0IdAndStatus(callerId, MatchStatus.PENDING) >= MAX_PENDING_CHALLENGES_SENT) {
            throw new ApiException(HttpStatus.CONFLICT, "You have " + MAX_PENDING_CHALLENGES_SENT
                    + " unanswered challenges. Wait for a reply or cancel one.");
        }
        if (matches.existsByPlayer0IdAndPlayer1IdAndStatus(callerId, opponent.getId(), MatchStatus.PENDING)) {
            throw new ApiException(HttpStatus.CONFLICT, "You already challenged " + opponent.getDisplayName() + ".");
        }
        MatchRecord m = matches.save(new MatchRecord(me, opponent, deck));
        return summary(m, callerId);
    }

    @Transactional
    public MatchSummary accept(Long callerId, Long matchId, AcceptRequest req) {
        MatchRecord m = lock(matchId, callerId);
        if (m.getStatus() != MatchStatus.PENDING || m.indexOf(callerId) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "There is no challenge for you to accept here.");
        }
        if (req == null || req.deckId() == null) throw bad("Choose one of your decks to play with.");
        List<Long> deck = decks.expandedCards(callerId, req.deckId());
        requirePlayable(deck, "Your deck");
        if (matches.countForPlayer(callerId, MatchStatus.ACTIVE) >= MAX_ACTIVE_MATCHES
                || matches.countForPlayer(m.getPlayer0().getId(), MatchStatus.ACTIVE) >= MAX_ACTIVE_MATCHES) {
            throw new ApiException(HttpStatus.CONFLICT, "One of you already has " + MAX_ACTIVE_MATCHES + " games in progress.");
        }

        GameState state = engine.newGame(m.getChallengerDeck(), deck, random);
        m.setStatus(MatchStatus.ACTIVE);
        m.getChallengerDeck().clear();
        m.setStateJson(write(state));
        matches.saveAndFlush(m);
        appendLog(m, state, List.of("The game begins. Place your starting tanks."));
        return summary(m, callerId);
    }

    @Transactional
    public MatchSummary decline(Long callerId, Long matchId) {
        MatchRecord m = lock(matchId, callerId);
        if (m.getStatus() != MatchStatus.PENDING || m.indexOf(callerId) != 1) {
            throw new ApiException(HttpStatus.CONFLICT, "There is no challenge for you to decline here.");
        }
        m.setStatus(MatchStatus.DECLINED);
        m.getChallengerDeck().clear();
        return summary(matches.saveAndFlush(m), callerId);
    }

    @Transactional
    public MatchSummary cancel(Long callerId, Long matchId) {
        MatchRecord m = lock(matchId, callerId);
        if (m.getStatus() != MatchStatus.PENDING || m.indexOf(callerId) != 0) {
            throw new ApiException(HttpStatus.CONFLICT, "You have no pending challenge here to cancel.");
        }
        m.setStatus(MatchStatus.CANCELLED);
        m.getChallengerDeck().clear();
        return summary(matches.saveAndFlush(m), callerId);
    }

    // ── Reading ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MatchSummary> list(Long callerId) {
        return matches.findByPlayer0IdOrPlayer1IdOrderByUpdatedAtDesc(callerId, callerId).stream()
                .filter(m -> m.getStatus() != MatchStatus.DECLINED && m.getStatus() != MatchStatus.CANCELLED)
                .map(m -> summary(m, callerId)).toList();
    }

    @Transactional(readOnly = true)
    public GameView view(Long callerId, Long matchId) {
        MatchRecord m = find(matchId, callerId);
        requireStarted(m);
        return GameViewBuilder.build(m, read(m), m.indexOf(callerId), engine.rules());
    }

    @Transactional(readOnly = true)
    public List<LogLine> log(Long callerId, Long matchId, int after) {
        find(matchId, callerId);
        return logs.findByMatchIdAndSeqGreaterThanOrderBySeq(matchId, after).stream()
                .map(e -> new LogLine(e.getSeq(), e.getTurn(), e.getText())).toList();
    }

    // ── Playing ──────────────────────────────────────────────────────────────

    @Transactional
    public ActionResponse act(Long callerId, Long matchId, ActionRequest req) {
        MatchRecord m = lock(matchId, callerId);
        requireActive(m);
        int me = m.indexOf(callerId);
        Action action = ActionMapper.map(req);
        GameState state = read(m);

        ActionResult result;
        try {
            result = engine.apply(state, me, action);
        } catch (RuleViolationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        save(m, state, result.log);
        return new ActionResponse(result.log, GameViewBuilder.build(m, state, me, engine.rules()));
    }

    @Transactional
    public ActionResponse resign(Long callerId, Long matchId) {
        MatchRecord m = lock(matchId, callerId);
        requireActive(m);
        int me = m.indexOf(callerId);
        GameState state = read(m);
        ActionResult result;
        try {
            result = engine.resign(state, me);
        } catch (RuleViolationException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        save(m, state, result.log);
        return new ActionResponse(result.log, GameViewBuilder.build(m, state, me, engine.rules()));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void save(MatchRecord m, GameState state, List<String> logLines) {
        m.setStateJson(write(state));
        if (state.phase == GameState.Phase.FINISHED) {
            m.setStatus(MatchStatus.FINISHED);
            m.setWinnerId(m.player(state.winner).getId());
            m.setEndReason(state.endReason);
        }
        matches.saveAndFlush(m);
        appendLog(m, state, logLines);
    }

    private void appendLog(MatchRecord m, GameState state, List<String> lines) {
        int seq = logs.lastSeq(m.getId());
        for (String line : lines) logs.save(new MatchLogEntry(m.getId(), ++seq, state.turnCount, line));
    }

    private MatchSummary summary(MatchRecord m, Long callerId) {
        int me = m.indexOf(callerId);
        Player other = m.player(1 - me);
        boolean yourTurn = false;
        if (m.getStatus() == MatchStatus.ACTIVE) {
            GameState s = read(m);
            yourTurn = s.phase == GameState.Phase.SETUP ? !s.player(me).placedStartingTank : s.activePlayer == me;
        } else if (m.getStatus() == MatchStatus.PENDING) {
            yourTurn = me == 1;            // a challenge is waiting for your answer
        }
        String result = m.getStatus() == MatchStatus.FINISHED
                ? (callerId.equals(m.getWinnerId()) ? "WON" : "LOST") : null;
        return new MatchSummary(m.getId(), m.getStatus().name(), other.getId(), other.getDisplayName(),
                me == 0, yourTurn, result, m.getUpdatedAt());
    }

    /** A match you aren't in looks exactly like one that doesn't exist. */
    private MatchRecord find(Long matchId, Long callerId) {
        MatchRecord m = matches.findById(matchId).orElseThrow(() -> new GameNotFoundException(matchId));
        if (m.indexOf(callerId) < 0) throw new GameNotFoundException(matchId);
        return m;
    }

    private MatchRecord lock(Long matchId, Long callerId) {
        MatchRecord m = matches.lockById(matchId).orElseThrow(() -> new GameNotFoundException(matchId));
        if (m.indexOf(callerId) < 0) throw new GameNotFoundException(matchId);
        return m;
    }

    private static void requireStarted(MatchRecord m) {
        if (m.getStateJson() == null) throw new ApiException(HttpStatus.CONFLICT, "This game hasn't started.");
    }

    private static void requireActive(MatchRecord m) {
        if (m.getStatus() != MatchStatus.ACTIVE) throw new ApiException(HttpStatus.CONFLICT, "This game isn't in progress.");
    }

    private void requirePlayable(List<Long> deck, String who) {
        List<String> problems = engine.validateDeck(deck);
        if (!problems.isEmpty()) throw bad(who + " isn't playable: " + String.join(" ", problems));
    }

    private Player player(Long id) {
        return players.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
    }

    private GameState read(MatchRecord m) {
        return json.readValue(m.getStateJson(), GameState.class);
    }

    private String write(GameState state) {
        return json.writeValueAsString(state);
    }

    private static ApiException bad(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
