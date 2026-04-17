package com.example.valtrak.Gameplay.Cards;

import com.example.valtrak.Data.CardLibrary.Vehicles.Interfaces.GroundVehicleCardInterface;
import com.example.valtrak.Data.GameData.DataTransfer.EnumData.*;
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

    @ManyToMany
    @JoinTable(name = "vehicle_primary_weapons",
               joinColumns = @JoinColumn(name = "vehicle_id"),
               inverseJoinColumns = @JoinColumn(name = "weapon_id"))
    private List<PrimaryWeaponEntity> primaryWeapons;

    @ManyToMany
    @JoinTable(name = "vehicle_secondary_weapons",
            joinColumns = @JoinColumn(name = "vehicle_id"),
            inverseJoinColumns = @JoinColumn(name = "weapon_id"))
    private List<SecondaryWeaponEntity> secondaryWeapons;


    public GroundVehicleCard(GroundVehicleCardInterface data,
                             VehicleTypeEntity vehicleType,
                             VehicleClassEntity vehicleClass,
                             List<PrimaryWeaponEntity> primaryWeapons,
                             List<SecondaryWeaponEntity> secondaryWeapons) {

        super(data.getVehicleName(), data.getDescription(), data.getLevel());
        this.vehicleNation = data.getVehicleNation();
        this.vehicleType = vehicleType;
        this.vehicleClass = vehicleClass;
        this.vehicleArmor = data.getVehicleArmor();
        this.primaryWeapons = primaryWeapons;
        this.secondaryWeapons = secondaryWeapons;
    }
}
