package com.example.valtrak.Data.GameData.Repository.Cards;

import com.example.valtrak.Gameplay.Cards.Resource.RepairCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairCardRepository extends JpaRepository<RepairCard, Long> {

    java.util.Optional<RepairCard> findByName(String name);

    boolean existsByName(String name);
}
