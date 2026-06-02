package com.example.valtrak.Data.GameData.Entity.GameState;

import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "field_units")
@Getter @Setter
@NoArgsConstructor
public class FieldUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "strike_group_id", nullable = false)
    @JsonIgnore
    private StrikeGroup strikeGroup;

    @ManyToOne
    @JoinColumn(name = "vehicle_card_id", nullable = false)
    private GroundVehicleCard vehicleCard;

    private int currentHp;
    private int maxHp;
    private int breachStacks;

    private boolean suppressed;
    private boolean disabled;
    private boolean stunned;

    public FieldUnit(StrikeGroup strikeGroup, GroundVehicleCard vehicleCard) {
        this.strikeGroup = strikeGroup;
        this.vehicleCard = vehicleCard;
        this.maxHp = vehicleCard.getVehicleHP();
        this.currentHp = this.maxHp;
        this.breachStacks = 0;
        this.suppressed = false;
        this.disabled = false;
        this.stunned = false;
    }

    public boolean isDestroyed() {
        return currentHp <= 0;
    }

    public void clearTurnEffects() {
        this.suppressed = false;
        this.stunned = false;
    }
}
