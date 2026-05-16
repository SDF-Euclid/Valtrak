package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleTypeEntity, Long> {
    boolean existsByName(String name);
    Optional<VehicleTypeEntity> findByName(String name);
}