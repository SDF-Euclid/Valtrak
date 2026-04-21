package com.example.valtrak.Data.GameData.Repository.Cards;

import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleCardRepository extends JpaRepository<GroundVehicleCard, Long> {

    Optional<GroundVehicleCard> findByName(String name);
    List<GroundVehicleCard> findByVehicleNation(String nation);

}
