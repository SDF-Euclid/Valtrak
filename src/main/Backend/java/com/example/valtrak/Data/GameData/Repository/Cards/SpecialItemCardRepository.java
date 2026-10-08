package com.example.valtrak.Data.GameData.Repository.Cards;

import com.example.valtrak.Gameplay.Cards.Special.SpecialItemCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecialItemCardRepository extends JpaRepository<SpecialItemCard, Long> {

    Optional<SpecialItemCard> findByName(String name);
}
