package com.example.valtrak.UI.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;
import java.util.Properties;

/**
 * Remembers the sign-in on this computer, so the player stays signed in after closing the app.
 * The token is kept in {@code ~/.valtrak/session.properties} (readable only by the user where the file system allows it),
 * together with the server it belongs to. Signing out, or the server saying the token has expired, deletes the file.
 * The folder can be moved with {@code -Dvaltrak.home=...}.
 */
public final class SavedSession {
    private SavedSession() {}

    private static Path file() {
        String home = System.getProperty("valtrak.home", Path.of(System.getProperty("user.home"), ".valtrak").toString());
        return Path.of(home, "session.properties");
    }

    /** Saves the token for this server. Failing to save only means signing in again next time. */
    public static void save(String server, String token) {
        Path file = file();
        try {
            Files.createDirectories(file.getParent());
            Path tmp = Files.createTempFile(file.getParent(), "session", ".tmp");
            try {
                Files.setPosixFilePermissions(tmp, PosixFilePermissions.fromString("rw-------"));
            } catch (UnsupportedOperationException ignored) {
                // Windows: the user's own profile folder is already private
            }
            Properties p = new Properties();
            p.setProperty("server", server);
            p.setProperty("token", token);
            try (OutputStream out = Files.newOutputStream(tmp)) {
                p.store(out, "Valtrak sign-in. Delete this file to sign out on this computer.");
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            System.err.println("Couldn't remember the sign-in: " + e.getMessage());
        }
    }

    /** The saved token, if there is one for this server. */
    public static Optional<String> load(String server) {
        Path file = file();
        if (!Files.isRegularFile(file)) return Optional.empty();
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        } catch (IOException e) {
            return Optional.empty();
        }
        String token = p.getProperty("token");
        if (token == null || token.isBlank() || !server.equals(p.getProperty("server"))) return Optional.empty();
        return Optional.of(token);
    }

    public static void clear() {
        try {
            Files.deleteIfExists(file());
        } catch (IOException e) {
            System.err.println("Couldn't forget the sign-in: " + e.getMessage());
        }
    }
}
