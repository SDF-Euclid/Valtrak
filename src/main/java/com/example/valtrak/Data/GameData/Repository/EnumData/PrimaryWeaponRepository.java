package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.GameData.DataTransfer.EnumData.PrimaryWeaponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;


@Repository
public interface PrimaryWeaponRepository extends JpaRepository<PrimaryWeaponEntity, Long> {
    boolean existsByWeaponName(String weaponName);
    Optional<PrimaryWeaponEntity> findByWeaponName(String weaponName);
}