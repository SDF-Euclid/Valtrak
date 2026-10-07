package com.example.valtrak.Data.GameData.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

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
    @JsonIgnore
    private String email;
    /**
     * True once the player has entered the code emailed to them.
     */
    @Column(name = "email_verified")
    @JsonIgnore
    private boolean emailVerified;
    /**
     * A String variable representing the Employee's password (post hashing)
     */
    @Column(name = "password")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //Updated so password is stored, but can never be accessed
    private String password;

    /*==================== EMAIL VERIFICATION ====================*/

    /** Argon2 hash of the pending 6-digit code (never the code itself). */
    @Column(name = "verification_code_hash")
    @JsonIgnore
    private String verificationCodeHash;
    @Column(name = "verification_expires_at")
    @JsonIgnore
    private LocalDateTime verificationExpiresAt;
    @Column(name = "verification_sent_at")
    @JsonIgnore
    private LocalDateTime verificationSentAt;
    @Column(name = "verification_attempts")
    @JsonIgnore
    private int verificationAttempts;

    /*==================== LOGIN THROTTLING ====================*/

    @Column(name = "failed_logins")
    @JsonIgnore
    private int failedLogins;
    @Column(name = "locked_until")
    @JsonIgnore
    private LocalDateTime lockedUntil;

    /*==================== PROFILE ====================*/

    /** Ids of the cards this player has starred in the deck builder. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_favorites", joinColumns = @JoinColumn(name = "player_id"))
    @Column(name = "card_id")
    @JsonIgnore
    private Set<Long> favoriteCardIds = new LinkedHashSet<>();

    /*====================================================================================================*/

}
