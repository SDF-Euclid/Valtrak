package com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.FuelItemInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum FuelSupplyDrum implements FuelItemInterface {

    /*==================== 1x FUEL DRUM ====================*/

    FUEL_DRUM_X1("1x Fuel Drum", "Resupplies 1 fuel unit", CardLevel.COMMON, 1),

    /*======================================================*/

    /*==================== 5x FUEL DRUM ====================*/

    FUEL_DRUM_X5("5x Fuel Drum", "Resupplies 5 fuel units", CardLevel.UNCOMMON, 5),

    /*======================================================*/

    /*==================== 10x FUEL DRUM ====================*/

    FUEL_DRUM_X10("10x Fuel Drum", "Resupplies 10 fuel units", CardLevel.RARE, 10),

    /*=======================================================*/

    /*==================== 20x FUEL DRUM ====================*/

    FUEL_TANKER("Fuel Tanker", "Resupplies 20 fuel units", CardLevel.LEGENDARY, 20);

    /*=======================================================*/

    /**
     *
     */
    private final String itemName;
    private final String itemDescription;
    private final CardLevel cardLevel;
    private final Integer count;

    /**
     *
     * @return
     */
    @Override
    public ItemType getItemType() {return ItemType.FUEL;}
}