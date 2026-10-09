package com.example.valtrak.UI.net;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.*;
import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import com.example.valtrak.Data.GameData.DataTransfer.DeckData.DeckDtos.*;
import com.example.valtrak.Data.GameData.DataTransfer.MatchData.MatchDtos.*;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * The desktop client's only connection to the game server. Calls here block,
 * so run them off the JavaFX thread (see {@code Ui.async}).
 * The server address comes from -Dvaltrak.server=... (default http://localhost:8080).
 * When a player is signed in, their token is sent with every request.
 */
public class ServerApi {

    public static final String BASE_URL =
            System.getProperty("valtrak.server", "http://localhost:8080").replaceAll("/+$", "");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final JsonMapper JSON = JsonMapper.builder().build();

    /** The server answered with an error status; the message is safe to show to the player. */
    public static class ApiError extends IOException {
        private final int status;

        public ApiError(int status, String message) {
            super(message);
            this.status = status;
        }

        public int status() { return status; }
    }

    // ── Cards & nations (open to guests) ─────────────────────────────────────

    public static List<CardDto> fetchCards() throws IOException {
        return send("GET", "/cards", null, new TypeReference<List<CardDto>>() {});
    }

    public static List<NationDto> fetchNations() throws IOException {
        return send("GET", "/nations", null, new TypeReference<List<NationDto>>() {});
    }

    // ── Accounts ─────────────────────────────────────────────────────────────

    public static MessageResponse register(String email, String displayName, String password, String nation)
            throws IOException {
        return send("POST", "/account/register", new RegisterRequest(email, displayName, password, nation),
                new TypeReference<MessageResponse>() {});
    }

    public static LoginResponse verify(String email, String code, String password) throws IOException {
        return send("POST", "/account/verify", new VerifyRequest(email, code, password),
                new TypeReference<LoginResponse>() {});
    }

    public static MessageResponse resendCode(String email) throws IOException {
        return send("POST", "/account/resend-code", new EmailRequest(email),
                new TypeReference<MessageResponse>() {});
    }

    public static LoginResponse login(String email, String password) throws IOException {
        return send("POST", "/account/login", new LoginRequest(email, password),
                new TypeReference<LoginResponse>() {});
    }

    public static void logout() throws IOException {
        send("POST", "/account/logout", null, null);
    }

    /** The profile of the player a token belongs to (used to check a saved sign-in at start-up). */
    public static ProfileDto fetchProfile(String token) throws IOException {
        return send("GET", "/account/me", null, new TypeReference<ProfileDto>() {}, token);
    }

    public static ProfileDto updateProfile(String displayName, String nation) throws IOException {
        return send("PUT", "/account/me", new UpdateProfileRequest(displayName, nation),
                new TypeReference<ProfileDto>() {});
    }

    public static List<Long> fetchFavorites() throws IOException {
        return send("GET", "/account/me/favorites", null, new TypeReference<FavoritesResponse>() {}).cardIds();
    }

    public static void setFavorite(long cardId, boolean favorite) throws IOException {
        send(favorite ? "PUT" : "DELETE", "/account/me/favorites/" + cardId, null,
                new TypeReference<FavoritesResponse>() {});
    }

    // ── Saved decks (signed-in players) ──────────────────────────────────────

    public static List<DeckDto> fetchDecks() throws IOException {
        return send("GET", "/decks", null, new TypeReference<List<DeckDto>>() {});
    }

    public static DeckDto createDeck(String name, Map<Long, Integer> cardCounts) throws IOException {
        return send("POST", "/decks", new SaveDeckRequest(name, cardCounts), new TypeReference<DeckDto>() {});
    }

    public static DeckDto updateDeck(long deckId, String name, Map<Long, Integer> cardCounts) throws IOException {
        return send("PUT", "/decks/" + deckId, new SaveDeckRequest(name, cardCounts), new TypeReference<DeckDto>() {});
    }

    public static void deleteDeck(long deckId) throws IOException {
        send("DELETE", "/decks/" + deckId, null, null);
    }

    // ── Matches (signed-in players) ──────────────────────────────────────────

    public static List<MatchSummary> fetchMatches() throws IOException {
        return send("GET", "/matches", null, new TypeReference<List<MatchSummary>>() {});
    }

    public static MatchSummary challenge(String opponentName, long deckId) throws IOException {
        return send("POST", "/matches/challenge", new ChallengeRequest(opponentName, deckId), new TypeReference<MatchSummary>() {});
    }

    public static MatchSummary acceptChallenge(long matchId, long deckId) throws IOException {
        return send("POST", "/matches/" + matchId + "/accept", new AcceptRequest(deckId), new TypeReference<MatchSummary>() {});
    }

    public static MatchSummary declineChallenge(long matchId) throws IOException {
        return send("POST", "/matches/" + matchId + "/decline", null, new TypeReference<MatchSummary>() {});
    }

    public static MatchSummary cancelChallenge(long matchId) throws IOException {
        return send("POST", "/matches/" + matchId + "/cancel", null, new TypeReference<MatchSummary>() {});
    }

    /** A practice game against the computer. {@code botDeck}: STANDARD, MIRROR or the id of one of your decks. */
    public static MatchSummary startBotMatch(long deckId, String botDeck, String style) throws IOException {
        return send("POST", "/matches/bot", new BotMatchRequest(deckId, botDeck, style), new TypeReference<MatchSummary>() {});
    }

    public static GameView fetchMatch(long matchId) throws IOException {
        return send("GET", "/matches/" + matchId, null, new TypeReference<GameView>() {});
    }

    public static List<LogLine> fetchLog(long matchId, int after) throws IOException {
        return send("GET", "/matches/" + matchId + "/log?after=" + after, null, new TypeReference<List<LogLine>>() {});
    }

    public static ActionResponse act(long matchId, ActionRequest request) throws IOException {
        return send("POST", "/matches/" + matchId + "/actions", request, new TypeReference<ActionResponse>() {});
    }

    public static ActionResponse resign(long matchId) throws IOException {
        return send("POST", "/matches/" + matchId + "/resign", null, new TypeReference<ActionResponse>() {});
    }

    // ── Plumbing ─────────────────────────────────────────────────────────────

    private static <T> T send(String method, String path, Object body, TypeReference<T> type) throws IOException {
        return send(method, path, body, type, AccountSession.isSignedIn() ? AccountSession.token() : null);
    }

    private static <T> T send(String method, String path, Object body, TypeReference<T> type, String token) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }
        HttpResponse<String> response;
        try {
            response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        } catch (IOException e) {
            throw new IOException("Can't reach the server at " + BASE_URL + ". Is it running?", e);
        }
        int status = response.statusCode();
        if (status / 100 != 2) {
            String text = response.body() == null ? "" : response.body().trim();
            throw new ApiError(status, text.isEmpty() || text.startsWith("<") || text.startsWith("{")
                    ? "The server returned an error (HTTP " + status + ")." : text);
        }
        if (type == null || response.body() == null || response.body().isBlank()) return null;
        return JSON.readValue(response.body(), type);
    }
}
