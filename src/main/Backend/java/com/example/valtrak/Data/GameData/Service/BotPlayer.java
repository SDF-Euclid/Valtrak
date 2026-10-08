package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.Entity.Player;
import com.example.valtrak.Data.GameData.Repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * The computer opponent for practice games. It is a normal player row so matches can point at it, but it has
 * no password and no usable email, so nobody can sign in as it, and it can't be challenged by name.
 */
@Component
@RequiredArgsConstructor
public class BotPlayer implements CommandLineRunner {

    public static final String USER_NAME = "valtrak-training-bot";
    private static final Logger log = LoggerFactory.getLogger(BotPlayer.class);

    private final PlayerRepository players;

    @Override
    public void run(String... args) {
        get();
    }

    /** The bot's player row (created the first time it is needed). */
    public synchronized Player get() {
        return players.findByUserName(USER_NAME).orElseGet(() -> {
            String name = players.existsByDisplayNameIgnoreCase("Training Bot") ? "Training Bot (AI)" : "Training Bot";
            Player bot = new Player(USER_NAME, name, "Neutral", "training-bot@valtrak.invalid");
            bot.setEmailVerified(false);       // not verified: it can't log in or be found by name
            log.info("Created the practice opponent '{}'.", name);
            return players.save(bot);
        });
    }

    public boolean isBot(Player p) {
        return p != null && USER_NAME.equals(p.getUserName());
    }
}
