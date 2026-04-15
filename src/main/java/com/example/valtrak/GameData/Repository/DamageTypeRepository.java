package com.example.valtrak.GameData.Repository;

import com.example.valtrak.GameData.Entity.DamageTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface DamageTypeRepository extends JpaRepository<DamageTypeEntity, Long> {
    boolean existsByName(String name);
    Optional<DamageTypeEntity> findByName(String name);
}
