package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Gameplay.Cards.GroundVehicleCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<GroundVehicleCard, Long> {

    Optional<GroundVehicleCard> findByVehicleName(String name);
    List<GroundVehicleCard> findByVehicleNation(String nation);

}
