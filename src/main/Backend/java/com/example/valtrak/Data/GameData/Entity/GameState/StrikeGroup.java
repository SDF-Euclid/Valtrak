package com.example.valtrak.Data.GameData.Entity.GameState;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "strike_groups")
@Getter @Setter
@NoArgsConstructor
public class StrikeGroup {

    public static final int MAX_SIZE = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_game_state_id", nullable = false)
    @JsonIgnore
    private PlayerGameState playerGameState;

    @OneToMany(mappedBy = "strikeGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FieldUnit> units = new ArrayList<>();

    public StrikeGroup(PlayerGameState playerGameState) {
        this.playerGameState = playerGameState;
    }

    public boolean isFull() {
        return units.size() >= MAX_SIZE;
    }

    public boolean isEmpty() {
        return units.isEmpty();
    }
}
