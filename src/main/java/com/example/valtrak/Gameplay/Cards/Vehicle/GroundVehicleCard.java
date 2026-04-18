package com.example.valtrak.Gameplay.Cards.Vehicle;

import com.example.valtrak.Data.CardLibrary.Interfaces.GroundVehicleCardInterface;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.*;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

/**
 *
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "vehicle_card")
public class GroundVehicleCard extends Card {

    private String vehicleNation;

    @ManyToOne
    @JoinColumn(name = "vehicle_type_id")
    private VehicleTypeEntity vehicleType;

    @ManyToOne
    @JoinColumn(name = "vehicle_class_id")
    private VehicleClassEntity vehicleClass;

    private Integer vehicleArmor;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL)
    private List<VehicleAttackEntity> attacks;


    public GroundVehicleCard(GroundVehicleCardInterface data,
                             VehicleTypeEntity vehicleType,
                             VehicleClassEntity vehicleClass) {
        super(data.getVehicleName(), data.getDescription(), data.getLevel());
        this.vehicleNation = data.getVehicleNation();
        this.vehicleType = vehicleType;
        this.vehicleClass = vehicleClass;
        this.vehicleArmor = data.getVehicleArmor();
    }
}
