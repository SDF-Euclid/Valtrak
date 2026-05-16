package com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.AmmunitionItemInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum AmmoSupplyCrate implements AmmunitionItemInterface {

    /*==================== 1x SUPPLY CRATE ====================*/

    APFSDS_120MM_X1("1x 120mm Sabot Crate", "Re-supplies 1 120mm apfsds dart", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.APFSDS_120MM, 1),
    APFSDS_125MM_X1("1x 125mm Sabot Crate", "Re-supplies 1 125mm apfsds dart", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.APFSDS_125MM, 1),

    HEAT_120MM_X1("1x 120mm HEAT Crate", "Re-supplies 1 120mm heat shell", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.HEAT_120MM, 1),
    HEAT_125MM_X1("1x 125mm HEAT Crate", "Re-supplies 1 125mm heat shell", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.HEAT_125MM, 1),

    /*=========================================================*/

    /*==================== 5x SUPPLY CRATE ====================*/

    APFSDS_120MM_X5("5x 120mm Sabot Crate", "Re-supplies 5 120mm apfsds darts", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.APFSDS_120MM, 5),
    APFSDS_125MM_X5("5x 125mm Sabot Crate", "Re-supplies 5 125mm apfsds darts", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.APFSDS_125MM, 5),

    HEAT_120MM_X5("5x 120mm HEAT Crate", "Re-supplies 5 120mm heat shells", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.HEAT_120MM, 5),
    HEAT_125MM_X5("5x 120mm HEAT Crate", "Re-supplies 5 125mm heat shells", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.HEAT_125MM, 5),

    /*=========================================================*/

    /*==================== 10x SUPPLY CRATE ====================*/

    APFSDS_120MM_X10("10x 120mm Sabot Crate", "Re-supplies 10 120mm apfsds darts", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.APFSDS_120MM, 10),
    APFSDS_125MM_X10("10x 125mm Sabot Crate", "Re-supplies 10 125mm apfsds darts", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.APFSDS_125MM, 10),

    HEAT_120MM_X10("10x 120mm HEAT Crate", "Re-supplies 10 120mm heat shells", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.HEAT_120MM, 10),
    HEAT_125MM_X10("10x 125mm HEAT Crate", "Re-supplies 10 125mm heat shells", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.HEAT_125MM, 10),;

    /*==========================================================*/

    /**
     *
     */
    private final String itemName;
    private final String itemDescription;
    private final CardLevel cardLevel;
    private final ItemType itemType;
    private final Ammunition ammunition;
    private final Integer count;
}