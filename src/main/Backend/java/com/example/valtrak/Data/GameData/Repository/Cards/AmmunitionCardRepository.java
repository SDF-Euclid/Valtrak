package com.example.valtrak.Data.GameData.Repository.Cards;

import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AmmunitionCardRepository extends JpaRepository<AmmunitionCard, Long> {

    Optional<AmmunitionCard> findByName(String name);
    boolean existsByName(String name);
}