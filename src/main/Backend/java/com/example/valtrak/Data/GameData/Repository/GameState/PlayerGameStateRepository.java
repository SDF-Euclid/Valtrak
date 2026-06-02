package com.example.valtrak.Data.GameData.Repository.GameState;

import com.example.valtrak.Data.GameData.Entity.GameState.PlayerGameState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerGameStateRepository extends JpaRepository<PlayerGameState, Long> {
}
