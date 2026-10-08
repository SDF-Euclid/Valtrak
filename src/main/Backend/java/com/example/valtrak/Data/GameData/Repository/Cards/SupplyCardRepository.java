package com.example.valtrak.Data.GameData.Repository.Cards;

import com.example.valtrak.Gameplay.Cards.Resource.SupplyCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyCardRepository extends JpaRepository<SupplyCard, Long> {

    java.util.Optional<SupplyCard> findByName(String name);

    boolean existsByName(String name);
}
