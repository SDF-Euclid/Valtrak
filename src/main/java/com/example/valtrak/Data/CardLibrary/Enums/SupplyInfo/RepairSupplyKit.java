package com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.RepairItemInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum RepairSupplyKit implements RepairItemInterface {

    /**/

    FIELD_REPAIR_KIT("Field Repair Kit", "Restores 25 HP to a unit", CardLevel.COMMON, 25, 1),

    /**/

    /**/

    MOBILE_WORKSHOP("Mobile Workshop", "Restores 75 HP to a unit", CardLevel.UNCOMMON, 75, 1),

    /**/

    /**/

    RECOVERY_VEHICLE("Recovery Vehicle", "Restores 150 HP to a unit", CardLevel.RARE, 150, 1),

    /**/

    /**/

    FULL_OVERHAUL("Full Repairs", "Fully restores a unit's HP and removes all BREACH stacks", CardLevel.LEGENDARY, 999, 1);

    /**/

    /**
     *
     */
    private final String itemName;
    private final String itemDescription;
    private final CardLevel cardLevel;
    private final Integer repairAmount;
    private final Integer count;

    @Override
    public ItemType getItemType() {return ItemType.REPAIR;}
}