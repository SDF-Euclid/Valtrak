package com.example.valtrak.UI;

import com.example.valtrak.UI.net.SavedSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import static org.assertj.core.api.Assertions.assertThat;

/** "Stay signed in": the token is kept in a file in the user's folder, for one server. */
class SavedSessionTest {

    @TempDir Path home;
    private String before;

    @BeforeEach
    void useTempFolder() {
        before = System.getProperty("valtrak.home");
        System.setProperty("valtrak.home", home.toString());
    }

    @AfterEach
    void restore() {
        if (before == null) System.clearProperty("valtrak.home"); else System.setProperty("valtrak.home", before);
    }

    @Test
    void aSavedTokenComesBackForTheSameServerOnly() {
        assertThat(SavedSession.load("http://localhost:8080")).isEmpty();
        SavedSession.save("http://localhost:8080", "abc123");
        assertThat(SavedSession.load("http://localhost:8080")).contains("abc123");
        assertThat(SavedSession.load("http://other:8080")).as("a token never goes to another server").isEmpty();
    }

    @Test
    void clearingForgetsIt() {
        SavedSession.save("http://localhost:8080", "abc123");
        SavedSession.clear();
        assertThat(SavedSession.load("http://localhost:8080")).isEmpty();
        SavedSession.clear();                                  // clearing twice is fine
    }

    @Test
    void theFileIsReadableOnlyByTheUserWhereTheFileSystemAllowsIt() throws Exception {
        SavedSession.save("http://localhost:8080", "abc123");
        Path file = home.resolve("session.properties");
        assertThat(file).exists();
        try {
            assertThat(PosixFilePermissions.toString(Files.getPosixFilePermissions(file))).isEqualTo("rw-------");
        } catch (UnsupportedOperationException windows) {
            // not a POSIX file system
        }
        try (var files = Files.list(home)) {
            assertThat(files).as("no temp files left behind").hasSize(1);
        }
    }
}
