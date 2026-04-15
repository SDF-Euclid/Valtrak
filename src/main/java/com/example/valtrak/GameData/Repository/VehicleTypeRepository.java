package com.example.valtrak.GameData.Repository;

import com.example.valtrak.GameData.Entity.VehicleTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleTypeEntity, Long> {
    boolean existsByName(String name);
    Optional<VehicleTypeEntity> findByName(String name);
}