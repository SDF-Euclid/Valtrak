package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.GameData.Entity.EnumEntity.AmmunitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface AmmunitionRepository extends JpaRepository<AmmunitionEntity, Long> {
    boolean existsByName(String name);
    Optional<AmmunitionEntity> findByName(String name);
}
