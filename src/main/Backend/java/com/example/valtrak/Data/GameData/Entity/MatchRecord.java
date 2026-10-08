package com.example.valtrak.Data.GameData.Entity;

import com.example.valtrak.Data.GameData.Enums.MatchStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A match between two players. Player 0 is the one who sent the challenge.
 * While the match is ACTIVE or FINISHED the whole game state is stored as JSON in {@code stateJson}.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "matches")
public class MatchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player0_id")
    private Player player0;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player1_id")
    private Player player1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    /** The challenger's deck, copied when the challenge is sent (kept until the match starts). */
    @ElementCollection
    @CollectionTable(name = "match_challenger_deck", joinColumns = @JoinColumn(name = "match_id"))
    @Column(name = "card_id")
    @OrderColumn(name = "position")
    private List<Long> challengerDeck = new ArrayList<>();

    @Lob
    private String stateJson;

    private Long winnerId;

    private String endReason;

    /** Goes up on every change; clients use it to see whether anything is new. */
    @Version
    private long version;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public MatchRecord(Player player0, Player player1, List<Long> challengerDeck) {
        this.player0 = player0;
        this.player1 = player1;
        this.status = MatchStatus.PENDING;
        this.challengerDeck = new ArrayList<>(challengerDeck);
    }

    /** @return 0 or 1, or -1 if the player isn't in this match */
    public int indexOf(Long playerId) {
        if (player0.getId().equals(playerId)) return 0;
        if (player1.getId().equals(playerId)) return 1;
        return -1;
    }

    public Player player(int index) {
        return index == 0 ? player0 : player1;
    }
}
