package com.example.valtrak.Gameplay.Cards.Resource;

import com.example.valtrak.Data.CardLibrary.Interfaces.Items.SupplyItemInterface;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@Entity @Table(name = "supply_cards")
public class SupplyCard extends ItemCard {

    private Integer count;

    public SupplyCard(SupplyItemInterface data) {
        super(data.getItemName(), data.getItemDescription(), data.getCardLevel(), data.getItemType());
        this.count = data.getCount();
    }
}
