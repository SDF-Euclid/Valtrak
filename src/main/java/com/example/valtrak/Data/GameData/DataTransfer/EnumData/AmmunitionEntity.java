package com.example.valtrak.Data.GameData.DataTransfer.EnumData;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table(name = "ammunition")
public class AmmunitionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "ammunition_name", unique = true)
    private String name;

    @NonNull
    @ManyToOne
    @JoinColumn(name = "damage_type_id")
    private DamageTypeEntity damageType;
}
