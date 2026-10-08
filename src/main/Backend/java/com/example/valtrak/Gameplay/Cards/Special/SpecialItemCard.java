package com.example.valtrak.Gameplay.Cards.Special;

import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.SpecialItemEffect;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.SpecialItemInterface;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** An item card with an effect: ERA, Artillery, Search or Draw. See {@link com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.SpecialItem}. */
@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "special_item_cards")
public class SpecialItemCard extends ItemCard {

    @Enumerated(EnumType.STRING)
    private SpecialItemEffect effect;

    /** ERA: percent. Artillery: damage per target. Search and Draw: how many cards. */
    private Integer primaryValue;

    /** Artillery: how many targets. Otherwise 0. */
    private Integer secondaryValue;

    public SpecialItemCard(SpecialItemInterface data) {
        super(data.getItemName(), data.getItemDescription(), data.getCardLevel(), data.getItemType());
        this.effect = data.getSpecialItemEffect();
        this.primaryValue = (int) Math.round(data.getPrimaryEffectValue());
        this.secondaryValue = (int) Math.round(data.getSecondaryEffectValue());
    }
}
