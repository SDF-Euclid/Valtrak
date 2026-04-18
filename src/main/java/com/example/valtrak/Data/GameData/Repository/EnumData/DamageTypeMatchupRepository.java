package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.DamageTypeMatchupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DamageTypeMatchupRepository extends JpaRepository<DamageTypeMatchupEntity, Long> {
    Optional<DamageTypeMatchupEntity> findByDamageTypeAndArmorBracket(DamageType damageType, ArmorBracket armorBracket);
}
