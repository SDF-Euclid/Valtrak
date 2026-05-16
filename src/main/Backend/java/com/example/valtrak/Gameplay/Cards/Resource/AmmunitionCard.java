package com.example.valtrak.Gameplay.Cards.Resource;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.AmmunitionItemInterface;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name = "ammunition_card")
public class AmmunitionCard extends ItemCard {

    @Enumerated(EnumType.STRING)
    private Ammunition ammunition;

    private Integer count;

    public AmmunitionCard(AmmunitionItemInterface data) {
        super(data.getItemName(), data.getItemDescription(), data.getCardLevel(), data.getItemType());
        this.ammunition = data.getAmmunition();
        this.count = data.getCount();
    }
}
