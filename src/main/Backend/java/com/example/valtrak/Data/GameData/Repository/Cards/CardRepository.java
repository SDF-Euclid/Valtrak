package com.example.valtrak.Data.GameData.Repository.Cards;

import com.example.valtrak.Gameplay.Cards.Base.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}
