package com.example.valtrak.UI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Remembers which cards the player has starred in the deck builder.
 * Stored by card name in ~/.valtrak/favorites.txt (one per line) because the
 * in-memory database is rebuilt on every launch and card ids are not stable.
 */
public class FavoritesStore {

    private final Path file = Path.of(System.getProperty("user.home"), ".valtrak", "favorites.txt");
    private final Set<String> names = new LinkedHashSet<>();

    public FavoritesStore() {
        try {
            if (Files.exists(file)) {
                for (String line : Files.readAllLines(file)) {
                    if (!line.isBlank()) names.add(line.strip());
                }
            }
        } catch (IOException ignored) {
            // start with no favorites if the file can't be read
        }
    }

    public boolean isFavorite(String cardName) {
        return names.contains(cardName);
    }

    public void toggle(String cardName) {
        if (!names.remove(cardName)) names.add(cardName);
        save();
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, names);
        } catch (IOException ignored) {
            // favorites still work for this session if saving fails
        }
    }
}
