package com.example.valtrak.GameData.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;


@Data
@Entity
@Table(name = "damage_types")
public class DamageTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "damage_type_name", unique = true)
    private String name;
}
