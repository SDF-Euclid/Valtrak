package com.example.valtrak.GameData.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Data
@Entity
@Table(name = "secondary_weapons")
public class SecondaryWeaponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "weapon_name", unique = true)
    private String weaponName;

    @NonNull
    @ManyToMany
    @JoinTable(name = "secondary_weapon_ammo",
            joinColumns = @JoinColumn(name = "weapon_id"),
            inverseJoinColumns = @JoinColumn(name = "ammo_id"))
    private List<AmmunitionEntity> compatibleAmmo;
}
