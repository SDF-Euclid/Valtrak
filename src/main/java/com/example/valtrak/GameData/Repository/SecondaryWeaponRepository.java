package com.example.valtrak.GameData.Repository;

import com.example.valtrak.GameData.Entity.SecondaryWeaponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SecondaryWeaponRepository extends JpaRepository<SecondaryWeaponEntity, Long> {
    boolean existsByWeaponName(String weaponName);
    Optional<SecondaryWeaponEntity> findByWeaponName(String weaponName);
}
