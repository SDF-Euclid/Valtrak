package com.example.valtrak.Data.GameData.Repository.EnumData;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.AttackSlot;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleAttackEntity;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleAttackRepository extends JpaRepository<VehicleAttackEntity, Long> {
    List<VehicleAttackEntity> findByVehicle(GroundVehicleCard vehicle);
    List<VehicleAttackEntity> findByVehicle_Id(Long vehicleId);
    Optional<VehicleAttackEntity> findByVehicleAndAttackSlot(GroundVehicleCard vehicle, AttackSlot attackSlot);
}
