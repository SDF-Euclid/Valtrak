package com.example.valtrak.GameData.Repository;

import com.example.valtrak.GameData.Entity.VehicleCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleCard, Long> {

    Optional<VehicleCard> findByVehicleName(String name);
    List<VehicleCard> findByVehicleNation(String nation);

}
