package com.example.valtrak.Data.GameData.Config;

import com.example.valtrak.Data.GameData.Service.DbCardCatalog;
import com.example.valtrak.Gameplay.Engine.GameEngine;
import com.example.valtrak.Gameplay.Engine.GameRules;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The one rules engine the server uses. Change the numbers by editing {@link GameRules}. */
@Configuration
public class EngineConfig {

    @Bean
    public GameEngine gameEngine(DbCardCatalog catalog) {
        return new GameEngine(GameRules.defaults(), catalog);
    }
}
