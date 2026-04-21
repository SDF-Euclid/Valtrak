package com.example.valtrak.Data.CardLibrary.Interfaces.Items;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.ItemType;

/**
 *
 */
public interface ItemCardInterface {
    String getItemName();
    String getItemDescription();
    CardLevel getCardLevel();
    ItemType getItemType();
    Integer getCount();
}