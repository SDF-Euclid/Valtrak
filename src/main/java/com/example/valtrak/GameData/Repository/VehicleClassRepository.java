package com.example.valtrak.GameData.Repository;

import com.example.valtrak.GameData.Entity.VehicleClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 *
 */
@Repository
public interface VehicleClassRepository extends JpaRepository<VehicleClassEntity, Long> {
    /**
     * A custom search method to search the VehicleClassRepository for
     * weather a VehicleClass exists or not
     * @param className
     *  The name of the VehicleClass being searched for
     * @return
     *  True or False
     */
    boolean existsByClassName(String className);

    Optional<VehicleClassEntity> findByClassName(String className);
}
