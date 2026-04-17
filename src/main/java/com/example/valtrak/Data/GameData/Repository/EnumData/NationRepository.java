package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.GameData.DataTransfer.EnumData.NationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NationRepository extends JpaRepository<NationEntity, Long> {
    boolean existsByNationName(String nationName);

    boolean existsByNationAbbreviation(String nationAbbreviation);
}
