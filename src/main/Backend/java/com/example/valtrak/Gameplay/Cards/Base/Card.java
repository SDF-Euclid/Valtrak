package com.example.valtrak.Gameplay.Cards.Base;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import jakarta.persistence.*;
import lombok.*;

/**
 *
 */
@Entity
@Getter @Setter
@NoArgsConstructor @RequiredArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "cards")
public abstract class Card {
    /**
     *
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @NonNull
    protected String name;

    @NonNull
    protected String description;

    @NonNull
    @Enumerated(EnumType.STRING)
    protected CardLevel level;
}
