package com.example.valtrak.Data.GameData.Entity.EnumEntity;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.*;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "vehicle_attacks")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class VehicleAttackEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    @JsonIgnore
    private GroundVehicleCard vehicle;

    @NonNull
    @Column(name = "attack_name")
    private String attackName;

    @NonNull
    @Enumerated(EnumType.STRING)
    private AttackSlot attackSlot;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "weapon_id")
    private WeaponEntity weapon;

    @NonNull
    private Integer baseDamage;

    @NonNull
    private Integer ammoCost;

    @NonNull
    private Integer fuelCost;

    @NonNull
    @Enumerated(EnumType.STRING)
    private SpecialEffect specialEffect;
}
