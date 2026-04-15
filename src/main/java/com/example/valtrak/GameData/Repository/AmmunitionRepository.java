package com.example.valtrak.GameData.Repository;

import com.example.valtrak.GameData.Entity.AmmunitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface AmmunitionRepository extends JpaRepository<AmmunitionEntity, Long> {
    boolean existsByName(String name);
    Optional<AmmunitionEntity> findByName(String name);
}
