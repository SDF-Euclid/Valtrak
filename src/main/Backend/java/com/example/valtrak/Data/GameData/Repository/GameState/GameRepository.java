package com.example.valtrak.Data.GameData.Repository.GameState;

import com.example.valtrak.Data.GameData.Entity.GameState.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
