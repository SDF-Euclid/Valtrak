package com.example.valtrak.GameData.Repository.EnumData;

import com.example.valtrak.GameData.Entity.DataTransfer.EnumData.NationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NationRepository extends JpaRepository<NationEntity, Long> {
    boolean existsByNationName(String nationName);

    boolean existsByNationAbbreviation(String nationAbbreviation);
}
