package com.example.valtrak.Data.GameData.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** A named deck saved by a player: how many copies of each card it contains. */
@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "decks")
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(nullable = false, length = 60)
    private String name;

    /** card id -> number of copies */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "deck_cards", joinColumns = @JoinColumn(name = "deck_id"))
    @MapKeyColumn(name = "card_id")
    @Column(name = "copies")
    private Map<Long, Integer> cardCounts = new LinkedHashMap<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Deck(Player player, String name) {
        this.player = player;
        this.name = name;
    }
}
