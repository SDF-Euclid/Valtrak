package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.DataTransfer.AccountData.AccountDtos.*;
import com.example.valtrak.Data.GameData.Entity.AuthSession;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.NationEntity;
import com.example.valtrak.Data.GameData.Entity.Player;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.ApiException;
import com.example.valtrak.Data.GameData.Repository.AuthSessionRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Data.GameData.Repository.EnumData.NationRepository;
import com.example.valtrak.Data.GameData.Repository.PlayerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Account lifecycle: register, verify email, sign in/out, profile and favorites.
 * Accounts are optional: guests simply never call these endpoints.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern DISPLAY_NAME = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 _-]{1,18}[A-Za-z0-9]$");

    private static final Duration CODE_LIFETIME = Duration.ofMinutes(15);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_CODE_ATTEMPTS = 5;
    private static final int MAX_FAILED_LOGINS = 5;
    private static final Duration LOCKOUT = Duration.ofMinutes(10);
    private static final Duration SESSION_LIFETIME = Duration.ofDays(30);

    private final SecureRandom random = new SecureRandom();

    private final PlayerRepository players;
    private final AuthSessionRepository sessions;
    private final NationRepository nations;
    private final CardRepository cards;
    private final PasswordEncoder encoder;
    private final EmailService email;

    /** Compared against when an email is unknown, so timing doesn't reveal which emails exist. */
    private String dummyHash;

    @PostConstruct
    void init() {
        dummyHash = encoder.encode("not-a-real-password");
    }

    // ── Registration & verification ──────────────────────────────────────────

    @Transactional
    public MessageResponse register(RegisterRequest req) {
        String mail = normalizeEmail(req.email());
        String name = validateDisplayName(req.displayName());
        validatePassword(req.password());
        NationEntity nation = requireNation(req.nation());

        Player player = players.lockByEmail(mail).orElse(null);
        if (player != null && (player.isEmailVerified() || isBot(player))) {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists. Try signing in.");
        }
        boolean nameTaken = players.existsByDisplayNameIgnoreCase(name)
                && !(player != null && player.getDisplayName().equalsIgnoreCase(name));
        if (nameTaken) {
            throw new ApiException(HttpStatus.CONFLICT, "That display name is already taken.");
        }

        if (player == null) {
            player = new Player(mail, name, nation.getNationName(), mail);
        } else { // an earlier, never-verified sign-up: let the person start over
            player.setDisplayName(name);
            player.setDisplayNation(nation.getNationName());
        }
        player.setPassword(encoder.encode(req.password()));
        player = players.save(player);

        sendCode(player, true);
        return new MessageResponse("We sent a 6-digit verification code to " + mail + ".");
    }

    @Transactional(noRollbackFor = ApiException.class)
    public LoginResponse verify(VerifyRequest req) {
        Player player = players.lockByEmail(normalizeEmail(req.email()))
                .filter(p -> !isBot(p))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired code."));
        if (player.isEmailVerified()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This email is already verified. Please sign in.");
        }
        if (player.getVerificationCodeHash() == null || player.getVerificationExpiresAt() == null
                || player.getVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "That code has expired. Request a new one.");
        }
        if (player.getVerificationAttempts() >= MAX_CODE_ATTEMPTS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Too many incorrect attempts. Request a new code.");
        }
        String code = req.code() == null ? "" : req.code().trim();
        String password = req.password() == null ? "" : req.password();
        boolean codeOk = encoder.matches(code, player.getVerificationCodeHash());
        boolean passwordOk = encoder.matches(password, player.getPassword());   // both are always checked, so timing says nothing
        if (!codeOk || !passwordOk) {
            player.setVerificationAttempts(player.getVerificationAttempts() + 1);
            players.save(player);
            int left = MAX_CODE_ATTEMPTS - player.getVerificationAttempts();
            throw new ApiException(HttpStatus.BAD_REQUEST, "Incorrect code or password. " + left + " attempt(s) left.");
        }
        player.setEmailVerified(true);
        player.setVerificationCodeHash(null);
        player.setVerificationExpiresAt(null);
        player.setVerificationAttempts(0);
        players.save(player);
        return signIn(player);
    }

    /** Always reports success so it can't be used to find out which emails have accounts. */
    @Transactional
    public MessageResponse resendCode(EmailRequest req) {
        players.findByEmail(normalizeEmail(req.email()))
                .filter(p -> !p.isEmailVerified() && !isBot(p))
                .ifPresent(p -> sendCode(p, true));
        return new MessageResponse("If that email has a pending sign-up, a new code is on its way.");
    }

    // ── Sign in / out ────────────────────────────────────────────────────────

    @Transactional(noRollbackFor = ApiException.class)
    public LoginResponse login(LoginRequest req) {
        Player player = players.lockByEmail(normalizeEmail(req.email())).filter(p -> !isBot(p)).orElse(null);
        String password = req.password() == null ? "" : req.password();
        if (player == null) {
            encoder.matches(password, dummyHash);
            throw badCredentials();
        }
        LocalDateTime now = LocalDateTime.now();
        if (player.getLockedUntil() != null && player.getLockedUntil().isAfter(now)) {
            long mins = Math.max(1, Duration.between(now, player.getLockedUntil()).toMinutes());
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Too many failed attempts. Try again in " + mins + " minute(s).");
        }
        if (!encoder.matches(password, player.getPassword())) {
            int fails = player.getFailedLogins() + 1;
            if (fails >= MAX_FAILED_LOGINS) {
                player.setLockedUntil(now.plus(LOCKOUT));
                fails = 0;
            }
            player.setFailedLogins(fails);
            players.save(player);
            throw badCredentials();
        }
        player.setFailedLogins(0);
        player.setLockedUntil(null);
        if (!player.isEmailVerified()) {
            players.save(player);
            try {
                sendCode(player, true);
            } catch (ApiException ignored) {
                // a code was sent very recently; the old one is still valid
            }
            throw new ApiException(HttpStatus.FORBIDDEN, "Email not verified. We've sent you a verification code.");
        }
        players.save(player);
        return signIn(player);
    }

    @Transactional
    public void logout(String rawToken) {
        sessions.deleteByTokenHash(hash(rawToken));
    }

    /** @return the player id for a valid, unexpired token */
    @Transactional(readOnly = true)
    public Optional<Long> authenticate(String rawToken) {
        return sessions.findPlayerIdByToken(hash(rawToken), LocalDateTime.now());
    }

    // ── Profile & favorites ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long playerId) {
        return toProfile(requirePlayer(playerId));
    }

    @Transactional
    public ProfileDto updateProfile(Long playerId, UpdateProfileRequest req) {
        Player player = requirePlayer(playerId);
        if (req.displayName() != null) {
            String name = validateDisplayName(req.displayName());
            if (!name.equalsIgnoreCase(player.getDisplayName()) && players.existsByDisplayNameIgnoreCase(name)) {
                throw new ApiException(HttpStatus.CONFLICT, "That display name is already taken.");
            }
            player.setDisplayName(name);
        }
        if (req.nation() != null) {
            player.setDisplayNation(requireNation(req.nation()).getNationName());
        }
        return toProfile(players.save(player));
    }

    @Transactional(readOnly = true)
    public FavoritesResponse getFavorites(Long playerId) {
        return new FavoritesResponse(List.copyOf(requirePlayer(playerId).getFavoriteCardIds()));
    }

    @Transactional
    public FavoritesResponse addFavorite(Long playerId, Long cardId) {
        if (!cards.existsById(cardId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No such card.");
        }
        Player player = requirePlayer(playerId);
        player.getFavoriteCardIds().add(cardId);
        return new FavoritesResponse(List.copyOf(players.save(player).getFavoriteCardIds()));
    }

    @Transactional
    public FavoritesResponse removeFavorite(Long playerId, Long cardId) {
        Player player = requirePlayer(playerId);
        player.getFavoriteCardIds().remove(cardId);
        return new FavoritesResponse(List.copyOf(players.save(player).getFavoriteCardIds()));
    }

    @Transactional(readOnly = true)
    public List<NationDto> getNations() {
        return nations.findAll().stream()
                .map(n -> new NationDto(n.getNationName(), n.getNationAbbreviation()))
                .sorted(java.util.Comparator.comparing(NationDto::name))
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** The practice opponent's account: nobody may register over it, verify it or sign in as it. */
    private static boolean isBot(Player p) {
        return BotPlayer.USER_NAME.equals(p.getUserName());
    }

    private void sendCode(Player player, boolean enforceCooldown) {
        LocalDateTime now = LocalDateTime.now();
        if (enforceCooldown && player.getVerificationSentAt() != null
                && player.getVerificationSentAt().plus(RESEND_COOLDOWN).isAfter(now)) {
            long secs = Duration.between(now, player.getVerificationSentAt().plus(RESEND_COOLDOWN)).toSeconds() + 1;
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait " + secs + " seconds before requesting another code.");
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        player.setVerificationCodeHash(encoder.encode(code));
        player.setVerificationExpiresAt(now.plus(CODE_LIFETIME));
        player.setVerificationSentAt(now);
        player.setVerificationAttempts(0);
        players.save(player);
        email.sendVerificationCode(player.getEmail(), player.getDisplayName(), code);
    }

    private LoginResponse signIn(Player player) {
        LocalDateTime now = LocalDateTime.now();
        sessions.deleteByExpiresAtBefore(now);
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.save(new AuthSession(hash(token), player, now, now.plus(SESSION_LIFETIME)));
        return new LoginResponse(token, toProfile(player));
    }

    private ProfileDto toProfile(Player p) {
        String abbreviation = nations.findByNationName(p.getDisplayNation())
                .map(NationEntity::getNationAbbreviation).orElse("");
        return new ProfileDto(p.getId(), p.getEmail(), p.getDisplayName(), p.getDisplayNation(), abbreviation);
    }

    private Player requirePlayer(Long id) {
        return players.findById(id).orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
    }

    private NationEntity requireNation(String name) {
        if (name == null || name.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a nation.");
        }
        return nations.findByNationName(name)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unknown nation: " + name));
    }

    private static ApiException badCredentials() {
        return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
    }

    private static String normalizeEmail(String raw) {
        String mail = raw == null ? "" : raw.trim().toLowerCase();
        if (mail.length() > 254 || !EMAIL.matcher(mail).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please enter a valid email address.");
        }
        return mail;
    }

    private static String validateDisplayName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (!DISPLAY_NAME.matcher(name).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Display name must be 3-20 characters: letters, numbers, spaces, _ or -.");
        }
        return name;
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 128) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be 8-128 characters.");
        }
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
