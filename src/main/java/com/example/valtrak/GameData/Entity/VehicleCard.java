package com.example.valtrak.GameData.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;
import java.util.List;


@Entity
@Table(name = "vehicle_card")
@Data
public class VehicleCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String vehicleName;

    @NonNull
    private String vehicleDescription;

    @NonNull
    private String vehicleNation;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "vehicle_type_id")
    private VehicleTypeEntity vehicleType;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "vehicle_class_id")
    private VehicleClassEntity vehicleClass;

    @NonNull
    @ManyToMany
    @JoinTable(name = "vehicle_primary_weapons",
               joinColumns = @JoinColumn(name = "vehicle_id"),
               inverseJoinColumns = @JoinColumn(name = "weapon_id"))
    private List<PrimaryWeaponEntity> primaryWeapons;

    @NonNull
    @ManyToMany
    @JoinTable(name = "vehicle_secondary_weapons",
            joinColumns = @JoinColumn(name = "vehicle_id"),
            inverseJoinColumns = @JoinColumn(name = "weapon_id"))
    private List<SecondaryWeaponEntity> secondaryWeapons;
}
