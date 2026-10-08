package com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.SupplyItemInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Supply cards. Supply is spent from your Depot to form a multi-vehicle strike group
 * (1 to 3 Supply, depending on the Leader's rarity).
 */
@Getter
@AllArgsConstructor
public enum SupplyCrate implements SupplyItemInterface {

    SUPPLY_CRATE_X1("1x Supply Crate", "Provides 1 supply", CardLevel.COMMON, ItemType.SUPPLY, 1),
    SUPPLY_CRATE_X3("3x Supply Crate", "Provides 3 supply", CardLevel.UNCOMMON, ItemType.SUPPLY, 3),
    SUPPLY_CRATE_X5("5x Supply Crate", "Provides 5 supply", CardLevel.RARE, ItemType.SUPPLY, 5);

    private final String itemName;
    private final String itemDescription;
    private final CardLevel cardLevel;
    private final ItemType itemType;
    private final Integer count;
}
