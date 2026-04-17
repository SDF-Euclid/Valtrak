package com.example.valtrak.Gameplay;

import com.example.valtrak.Gameplay.Cards.Card;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "players")
public class Player {

    /*======================================== DATABASE VARIABLES ========================================*/

    /**
     *
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     *
     */
    @Column(name = "creation_date")
    @CreationTimestamp
    private LocalDateTime creationDate;
    /**
     *
     */
    @Column(name = "username", unique = true)
    @NonNull
    private String userName;
    /**
     *
     */
    @Column(name = "display_name", unique = true)
    @NonNull
    private String displayName;
    /**
     *
     */
    @Column(name = "display_nation")
    @NonNull
    private String displayNation;
    /**
     *
     */
    @Column(name = "email", unique = true)
    @NonNull
    private String email;
    /**
     * A String variable representing the Employee's password (post hashing)
     */
    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //Updated so password is stored, but can never be accessed
    private String password;

    /*====================================================================================================*/

    /*======================================== GAMEPLAY VARIABLES ========================================*/

    private List<Card> deck = new ArrayList<>();
    private List<Card> hand = new ArrayList<>();
    private List<Card> discardPile = new ArrayList<>();

    /*====================================================================================================*/

}
