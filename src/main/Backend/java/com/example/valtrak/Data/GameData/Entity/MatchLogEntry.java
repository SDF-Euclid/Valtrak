package com.example.valtrak.Data.GameData.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One line of a match's game log. Everything in it is visible to both players. */
@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "match_log", indexes = @Index(columnList = "match_id, seq"))
public class MatchLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(nullable = false)
    private int seq;

    private int turn;

    @Column(length = 500)
    private String text;

    public MatchLogEntry(Long matchId, int seq, int turn, String text) {
        this.matchId = matchId;
        this.seq = seq;
        this.turn = turn;
        this.text = text.length() > 500 ? text.substring(0, 500) : text;
    }
}
