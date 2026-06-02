package com.example.valtrak.Data.GameData.Entity.GameState;

import com.example.valtrak.Data.GameData.Entity.Player;
import com.example.valtrak.Data.GameData.Enums.GamePhase;
import com.example.valtrak.Data.GameData.Enums.GameStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter @Setter
@NoArgsConstructor
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player1_id", nullable = false)
    private Player player1;

    @ManyToOne
    @JoinColumn(name = "player2_id", nullable = false)
    private Player player2;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "player1_state_id")
    private PlayerGameState player1State;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "player2_state_id")
    private PlayerGameState player2State;

    // ID of the player whose turn it currently is
    private Long activePlayerId;

    private int turnNumber;

    @Enumerated(EnumType.STRING)
    private GamePhase currentPhase;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    // Null until game ends
    private Long winnerId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Game(Player player1, Player player2,
                PlayerGameState player1State, PlayerGameState player2State) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1State = player1State;
        this.player2State = player2State;
        this.activePlayerId = player1.getId();
        this.turnNumber = 1;
        this.currentPhase = GamePhase.LOGISTICS;
        this.status = GameStatus.ACTIVE;
    }

    public PlayerGameState getActivePlayerState() {
        return activePlayerId.equals(player1.getId()) ? player1State : player2State;
    }

    public PlayerGameState getOpponentState() {
        return activePlayerId.equals(player1.getId()) ? player2State : player1State;
    }

    public Player getOpponent() {
        return activePlayerId.equals(player1.getId()) ? player2 : player1;
    }
}
