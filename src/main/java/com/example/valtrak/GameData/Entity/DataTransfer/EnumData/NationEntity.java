package com.example.valtrak.GameData.Entity.DataTransfer.EnumData;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@Entity
@Table(name = "nations")
public class NationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "nation_name", unique = true)
    private String nationName;

    @NonNull
    @Column(name = "nation_abbreviation", unique = true)
    private String nationAbbreviation;
}