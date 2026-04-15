package com.example.valtrak.GameData.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;

import java.util.List;


@Data
@Entity
@Table(name = "primary_weapons")
public class PrimaryWeaponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "weapon_name", unique = true)
    private String weaponName;

    @NonNull
    @ManyToMany
    @JoinTable(name = "primary_weapon_ammunition",
               joinColumns = @JoinColumn(name = "weapon_id"),
               inverseJoinColumns = @JoinColumn(name = "ammunition_id"))
    private List<AmmunitionEntity> compatibleAmmunition;
}
