package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.GameData.Entity.EnumEntity.WeaponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WeaponRepository extends JpaRepository<WeaponEntity, Long> {
    boolean existsByWeaponName(String weaponName);
    Optional<WeaponEntity> findByWeaponName(String weaponName);

}
