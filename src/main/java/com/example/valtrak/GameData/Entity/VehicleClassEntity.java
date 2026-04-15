package com.example.valtrak.GameData.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NonNull;


@Data
@Entity
@Table(name = "vehicle_classes")
public class VehicleClassEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long classId;

    @NonNull
    @Column(name = "class_name")
    private String className;
}
