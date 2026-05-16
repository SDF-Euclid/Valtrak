package com.example.valtrak.Data.GameData.Entity.GameState;

import com.example.valtrak.Data.GameData.Entity.Player;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "player_game_states")
@Getter @Setter
@NoArgsConstructor
public class PlayerGameState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    // Deck: ordered list of card IDs (template cards, top = index 0)
    @ElementCollection
    @CollectionTable(name = "pgs_deck", joinColumns = @JoinColumn(name = "pgs_id"))
    @Column(name = "card_id")
    @OrderColumn(name = "deck_position")
    private List<Long> deck = new ArrayList<>();

    // Hand: unordered set of card IDs (may contain duplicates if same card appears multiple times)
    @ElementCollection
    @CollectionTable(name = "pgs_hand", joinColumns = @JoinColumn(name = "pgs_id"))
    @Column(name = "card_id")
    private List<Long> hand = new ArrayList<>();

    // Discard: ordered list of card IDs (top = most recently discarded)
    @ElementCollection
    @CollectionTable(name = "pgs_discard", joinColumns = @JoinColumn(name = "pgs_id"))
    @Column(name = "card_id")
    @OrderColumn(name = "discard_position")
    private List<Long> discard = new ArrayList<>();

    // Ammo tracked per type (Ammunition.name() → count)
    @ElementCollection
    @CollectionTable(name = "pgs_ammo", joinColumns = @JoinColumn(name = "pgs_id"))
    @MapKeyColumn(name = "ammo_type")
    @Column(name = "quantity")
    private Map<String, Integer> ammoInventory = new HashMap<>();

    private int fuelPool;
    private int supplyPool;
    private int repairPool;
    private int territoryChips;

    @OneToMany(mappedBy = "playerGameState", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StrikeGroup> strikeGroups = new ArrayList<>();

    public PlayerGameState(Player player, List<Long> deck) {
        this.player = player;
        this.deck = new ArrayList<>(deck);
        this.fuelPool = 0;
        this.supplyPool = 0;
        this.repairPool = 0;
        this.territoryChips = 0;
    }

    public void resetResourcePools() {
        this.ammoInventory.clear();
        this.fuelPool = 0;
        this.supplyPool = 0;
        this.repairPool = 0;
    }

    @JsonIgnore
    public int getTotalFieldUnits() {
        return strikeGroups.stream().mapToInt(sg -> sg.getUnits().size()).sum();
    }
}
