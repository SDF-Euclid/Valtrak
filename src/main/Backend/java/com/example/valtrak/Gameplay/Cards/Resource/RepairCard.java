package com.example.valtrak.Gameplay.Cards.Resource;

import com.example.valtrak.Data.CardLibrary.Interfaces.Items.RepairItemInterface;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity @Table(name = "repair_cards")
public class RepairCard extends ItemCard {

    private Integer repairAmount;

    private Integer count;

    public RepairCard(RepairItemInterface data) {
        super(data.getItemName(), data.getItemDescription(), data.getCardLevel(), data.getItemType());
        this.repairAmount = data.getRepairAmount();
        this.count = data.getCount();
    }
}
