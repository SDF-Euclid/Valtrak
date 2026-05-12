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

    FIELD_REPAIR_KIT("Field Repair Kit", "Restores 25 HP to a unit", CardLevel.COMMON, ItemType.REPAIR, 25, 1),

    /**/

    /**/

    MOBILE_WORKSHOP("Mobile Workshop", "Restores 75 HP to a unit", CardLevel.UNCOMMON, ItemType.REPAIR, 75, 1),

    /**/

    /**/

    RECOVERY_VEHICLE("Recovery Vehicle", "Restores 150 HP to a unit", CardLevel.RARE, ItemType.REPAIR, 150, 1),

    /**/

    /**/

    FULL_OVERHAUL("Full Repairs", "Fully restores a unit's HP and removes all BREACH stacks", CardLevel.LEGENDARY, ItemType.REPAIR, 999, 1);

    /**/

    /**
     *
     */
    private final String itemName;
    private final String itemDescription;
    private final CardLevel cardLevel;
    private final ItemType itemType;
    private final Integer repairAmount;
    private final Integer count;
}