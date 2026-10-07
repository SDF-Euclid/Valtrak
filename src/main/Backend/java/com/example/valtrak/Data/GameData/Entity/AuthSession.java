package com.example.valtrak.Data.GameData.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A signed-in client. Only the SHA-256 hash of the bearer token is stored, so a
 * leaked database does not expose usable tokens.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "auth_sessions", indexes = @Index(columnList = "token_hash", unique = true))
public class AuthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "player_id")
    private Player player;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public AuthSession(String tokenHash, Player player, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.tokenHash = tokenHash;
        this.player = player;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }
}
