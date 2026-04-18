package com.example.valtrak.Data.GameData.Entity.EnumEntity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.util.List;


@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table(name = "weapons")
public class WeaponEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "weapon_name", unique = true)
    private String weaponName;

    @NonNull
    @ManyToMany
    @JoinTable(name = "weapon_ammunition",
            joinColumns = @JoinColumn(name = "weapon_id"),
            inverseJoinColumns = @JoinColumn(name = "ammunition_id"))
    private List<AmmunitionEntity> compatibleAmmunition;
}
