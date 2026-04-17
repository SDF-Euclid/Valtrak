package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.GameData.DataTransfer.EnumData.SecondaryWeaponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SecondaryWeaponRepository extends JpaRepository<SecondaryWeaponEntity, Long> {
    boolean existsByWeaponName(String weaponName);
    Optional<SecondaryWeaponEntity> findByWeaponName(String weaponName);
}
