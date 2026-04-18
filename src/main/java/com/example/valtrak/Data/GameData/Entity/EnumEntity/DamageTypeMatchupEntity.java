package com.example.valtrak.Data.GameData.Entity.EnumEntity;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "damage_type_matchups",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"damage_type", "armor_bracket"}
        ))
public class DamageTypeMatchupEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "damage_type")
    private DamageType damageType;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "armor_bracket")
    private ArmorBracket armorBracket;

    @NonNull
    @Column(name = "damage_modifier")
    private Double damageModifier;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "auto_effect")
    private SpecialEffect  autoEffect;

}
