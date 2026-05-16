package com.example.valtrak.Gameplay.Cards.Base;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.ItemType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

/**
 * Abstract base class for all item cards in Valtrak.
 * Item cards are played from hand to provide resources, ammunition,
 * repairs, or other effects. They are consumed on use and sent to
 * the discard pile.
 * Subclasses define the specific item type and its associated data,
 * such as AmmunitionCard, and future types like FuelCard, RepairCard, etc.
 */
@Entity
@Table(name = "item_cards")
@Getter @Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ItemCard extends Card {

    /**
     * The category of item this card represents.
     * Used to determine how the card is processed when played.
     */
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type")
    private ItemType itemType;

    /**
     * @param name        display name of the card
     * @param description flavour/effect description
     * @param level       rarity level of the card
     * @param itemType    the category of item this card represents
     */
    public ItemCard(String name, String description, CardLevel level, @NonNull ItemType itemType) {
        super(name, description, level);
        this.itemType = itemType;
    }
}