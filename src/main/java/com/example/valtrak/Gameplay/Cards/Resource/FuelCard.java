package com.example.valtrak.Gameplay.Cards.Resource;

import com.example.valtrak.Data.CardLibrary.Interfaces.Items.FuelItemInterface;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@Getter @Setter
@Entity @Table(name = "fuel_cards")
public class FuelCard extends ItemCard {

    private Integer count;

    public FuelCard(FuelItemInterface data) {
        super (data.getItemName(), data.getItemDescription(), data.getCardLevel(), data.getItemType());
        this.count = data.getCount();
    }
}
