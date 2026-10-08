package com.example.valtrak.Data.GameData.Repository;

import com.example.valtrak.Data.GameData.Entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeckRepository extends JpaRepository<Deck, Long> {

    List<Deck> findByPlayerIdOrderByUpdatedAtDesc(Long playerId);

    Optional<Deck> findByIdAndPlayerId(Long id, Long playerId);

    long countByPlayerId(Long playerId);

    boolean existsByPlayerIdAndNameIgnoreCase(Long playerId, String name);
}
