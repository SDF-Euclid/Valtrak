package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.GameData.Entity.EnumEntity.DamageTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface DamageTypeRepository extends JpaRepository<DamageTypeEntity, Long> {
    boolean existsByName(String name);
    Optional<DamageTypeEntity> findByName(String name);
}
