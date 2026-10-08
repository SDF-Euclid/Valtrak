package com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.AmmunitionItemInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Ammunition crates: every ammunition type comes in 1x (Common), 5x (Uncommon), 10x (Rare) and 20x (Legendary).
 * The card names of the original 120mm/125mm crates are kept as they were.
 */
@Getter
@AllArgsConstructor
public enum AmmoSupplyCrate implements AmmunitionItemInterface {

    /*==================== 1x SUPPLY CRATE ====================*/

    /*==========APDS==========*/
    APDS_105MM_X1("1x 105mm APDS Crate", "Re-supplies 1 105mm apds round", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.APDS_105MM, 1),
    /*========================*/

    /*==========APFSDS==========*/
    APFSDS_25MM_X1("1x 25mm Sabot Crate", "Re-supplies 1 25mm apfsds dart", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.APFSDS_25MM, 1),
    APFSDS_105MM_X1("1x 105mm Sabot Crate", "Re-supplies 1 105mm apfsds dart", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.APFSDS_105MM, 1),
    APFSDS_120MM_X1("1x 120mm Sabot Crate", "Re-supplies 1 120mm apfsds dart", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.APFSDS_120MM, 1),
    APFSDS_125MM_X1("1x 125mm Sabot Crate", "Re-supplies 1 125mm apfsds dart", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.APFSDS_125MM, 1),
    /*==========================*/

    /*==========HEAT==========*/
    HEAT_120MM_X1("1x 120mm HEAT Crate", "Re-supplies 1 120mm heat shell", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.HEAT_120MM, 1),
    HEAT_125MM_X1("1x 125mm HEAT Crate", "Re-supplies 1 125mm heat shell", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.HEAT_125MM, 1),
    /*========================*/

    /*==========TOW==========*/
    BGM_71_152MM_X1("1x TOW Missile Crate", "Re-supplies 1 TOW missile", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.BGM_71_152MM, 1),
    /*=======================*/

    /*==========HE==========*/
    HE_25MM_X1("1x 25mm HE Crate", "Re-supplies 1 25mm he round", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.HE_25MM, 1),
    HE_40MM_X1("1x 40mm HE Crate", "Re-supplies 1 40mm he grenade", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.HE_40MM, 1),
    HE_120MM_X1("1x 120mm HE Crate", "Re-supplies 1 120mm HE shell", CardLevel.COMMANDER, ItemType.AMMUNITION, Ammunition.HE_120MM, 1),
    /*======================*/

    /*==========SQUASH HEAD==========*/
    SQUASH_HEAD_105MM_X1("1x 105mm Squash Head Crate", "Re-supplies 1 105mm squash head shell", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.SQUASH_HEAD_105MM, 1),
    /*===============================*/

    /*==========MACHINE GUN==========*/
    NATO_127x99MM_X1("1x .50 Cal Crate", "Re-supplies 1 12.7x99mm round", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.NATO_127x99MM, 1),
    MG3_762x51MM_X1("1x 7.62x51mm Crate", "Re-supplies 1 7.62x51mm round", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.MG3_762x51MM, 1),
    PKT_762x54MM_X1("1x 7.62x54mm Crate", "Re-supplies 1 7.62x54mm round", CardLevel.COMMON, ItemType.AMMUNITION, Ammunition.PKT_762x54MM, 1),
    /*===============================*/

    /*==================== 5x SUPPLY CRATE ====================*/

    /*==========APDS==========*/
    APDS_105MM_X5("5x 105mm APDS Crate", "Re-supplies 5 105mm apds rounds", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.APDS_105MM, 5),
    /*========================*/

    /*==========APFSDS==========*/
    APFSDS_25MM_X5("5x 25mm Sabot Crate", "Re-supplies 5 25mm apfsds darts", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.APFSDS_25MM, 5),
    APFSDS_105MM_X5("5x 105mm Sabot Crate", "Re-supplies 5 105mm apfsds darts", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.APFSDS_105MM, 5),
    APFSDS_120MM_X5("5x 120mm Sabot Crate", "Re-supplies 5 120mm apfsds darts", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.APFSDS_120MM, 5),
    APFSDS_125MM_X5("5x 125mm Sabot Crate", "Re-supplies 5 125mm apfsds darts", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.APFSDS_125MM, 5),
    /*==========================*/

    /*==========HEAT==========*/
    HEAT_120MM_X5("5x 120mm HEAT Crate", "Re-supplies 5 120mm heat shells", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.HEAT_120MM, 5),
    HEAT_125MM_X5("5x 125mm HEAT Crate", "Re-supplies 5 125mm heat shells", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.HEAT_125MM, 5),
    /*========================*/

    /*==========TOW==========*/
    BGM_71_152MM_X5("5x TOW Missile Crate", "Re-supplies 5 TOW missiles", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.BGM_71_152MM, 5),
    /*=======================*/

    /*==========HE==========*/
    HE_25MM_X5("5x 25mm HE Crate", "Re-supplies 5 25mm he rounds", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.HE_25MM, 5),
    HE_40MM_X5("5x 40mm HE Crate", "Re-supplies 5 40mm he grenades", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.HE_40MM, 5),
    HE_120MM_X5("5x 120mm HE Crate", "Re-supplies 5 120mm he shells", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.HE_120MM, 5),
    /*======================*/

    /*==========SQUASH HEAD==========*/
    SQUASH_HEAD_105MM_X5("5x 105mm Squash Head Crate", "Re-supplies 5 105mm squash head shells", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.SQUASH_HEAD_105MM, 5),
    /*===============================*/

    /*==========MACHINE GUN==========*/
    NATO_127x99MM_X5("5x .50 Cal Crate", "Re-supplies 5 12.7x99mm rounds", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.NATO_127x99MM, 5),
    MG3_762x51MM_X5("5x 7.62x51mm Crate", "Re-supplies 5 7.62x51mm rounds", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.MG3_762x51MM, 5),
    PKT_762x54MM_X5("5x 7.62x54mm Crate", "Re-supplies 5 7.62x54mm rounds", CardLevel.UNCOMMON, ItemType.AMMUNITION, Ammunition.PKT_762x54MM, 5),
    /*===============================*/

    /*==================== 10x SUPPLY CRATE ====================*/

    /*==========APDS==========*/
    APDS_105MM_X10("10x 105mm APDS Crate", "Re-supplies 10 105mm apds rounds", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.APDS_105MM, 10),
    /*========================*/

    /*==========APFSDS==========*/
    APFSDS_25MM_X10("10x 25mm Sabot Crate", "Re-supplies 10 25mm apfsds darts", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.APFSDS_25MM, 10),
    APFSDS_105MM_X10("10x 105mm Sabot Crate", "Re-supplies 10 105mm apfsds darts", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.APFSDS_105MM, 10),
    APFSDS_120MM_X10("10x 120mm Sabot Crate", "Re-supplies 10 120mm apfsds darts", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.APFSDS_120MM, 10),
    APFSDS_125MM_X10("10x 125mm Sabot Crate", "Re-supplies 10 125mm apfsds darts", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.APFSDS_125MM, 10),
    /*==========================*/

    /*==========HEAT==========*/
    HEAT_120MM_X10("10x 120mm HEAT Crate", "Re-supplies 10 120mm heat shells", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.HEAT_120MM, 10),
    HEAT_125MM_X10("10x 125mm HEAT Crate", "Re-supplies 10 125mm heat shells", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.HEAT_125MM, 10),
    /*========================*/

    /*==========TOW==========*/
    BGM_71_152MM_X10("10x TOW Missile Crate", "Re-supplies 10 TOW missiles", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.BGM_71_152MM, 10),
    /*=======================*/

    /*==========HE==========*/
    HE_25MM_X10("10x 25mm HE Crate", "Re-supplies 10 25mm he rounds", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.HE_25MM, 10),
    HE_40MM_X10("10x 40mm HE Crate", "Re-supplies 10 40mm he grenades", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.HE_40MM, 10),
    HE_120MM_X10("10x 120mm HE Crate", "Re-supplies 10 120mm he shells", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.HE_120MM, 10),
    /*======================*/

    /*==========SQUASH HEAD==========*/
    SQUASH_HEAD_105MM_X10("10x 105mm Squash Head Crate", "Re-supplies 10 105mm squash head shells", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.SQUASH_HEAD_105MM, 10),
    /*===============================*/

    /*==========MACHINE GUN==========*/
    NATO_127x99MM_X10("10x .50 Cal Crate", "Re-supplies 10 12.7x99mm rounds", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.NATO_127x99MM, 10),
    MG3_762x51MM_X10("10x 7.62x51mm Crate", "Re-supplies 10 7.62x51mm rounds", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.MG3_762x51MM, 10),
    PKT_762x54MM_X10("10x 7.62x54mm Crate", "Re-supplies 10 7.62x54mm rounds", CardLevel.RARE, ItemType.AMMUNITION, Ammunition.PKT_762x54MM, 10),
    /*===============================*/

    /*==================== 20x SUPPLY CRATE ====================*/

    /*==========APDS==========*/
    APDS_105MM_X20("20x 105mm APDS Crate", "Re-supplies 20 105mm apds rounds", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.APDS_105MM, 20),
    /*========================*/

    /*==========APFSDS==========*/
    APFSDS_25MM_X20("20x 25mm Sabot Crate", "Re-supplies 20 25mm apfsds darts", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.APFSDS_25MM, 20),
    APFSDS_105MM_X20("20x 105mm Sabot Crate", "Re-supplies 20 105mm apfsds darts", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.APFSDS_105MM, 20),
    APFSDS_120MM_X20("20x 120mm Sabot Crate", "Re-supplies 20 120mm apfsds darts", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.APFSDS_120MM, 20),
    APFSDS_125MM_X20("20x 125mm Sabot Crate", "Re-supplies 20 125mm apfsds darts", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.APFSDS_125MM, 20),
    /*==========================*/

    /*==========HEAT==========*/
    HEAT_120MM_X20("20x 120mm HEAT Crate", "Re-supplies 20 120mm heat shells", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.HEAT_120MM, 20),
    HEAT_125MM_X20("20x 125mm HEAT Crate", "Re-supplies 20 125mm heat shells", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.HEAT_125MM, 20),
    /*========================*/

    /*==========TOW==========*/
    BGM_71_152MM_X20("20x TOW Missile Crate", "Re-supplies 20 TOW missiles", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.BGM_71_152MM, 20),
    /*=======================*/

    /*==========HE==========*/
    HE_25MM_X20("20x 25mm HE Crate", "Re-supplies 20 25mm he rounds", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.HE_25MM, 20),
    HE_40MM_X20("20x 40mm HE Crate", "Re-supplies 20 40mm he grenades", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.HE_40MM, 20),
    HE_120MM_X20("20x 120mm HE Crate", "Re-supplies 20 120mm he shells", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.HE_120MM, 20),
    /*======================*/

    /*==========SQUASH HEAD==========*/
    SQUASH_HEAD_105MM_X20("20x 105mm Squash Head Crate", "Re-supplies 20 105mm squash head shells", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.SQUASH_HEAD_105MM, 20),
    /*===============================*/

    /*==========MACHINE GUN==========*/
    NATO_127x99MM_X20("20x .50 Cal Crate", "Re-supplies 20 12.7x99mm rounds", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.NATO_127x99MM, 20),
    MG3_762x51MM_X20("20x 7.62x51mm Crate", "Re-supplies 20 7.62x51mm rounds", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.MG3_762x51MM, 20),
    PKT_762x54MM_X20("20x 7.62x54mm Crate", "Re-supplies 20 7.62x54mm rounds", CardLevel.LEGENDARY, ItemType.AMMUNITION, Ammunition.PKT_762x54MM, 20);
    /*===============================*/

    private final String itemName;
    private final String itemDescription;
    private final CardLevel cardLevel;
    private final ItemType itemType;
    private final Ammunition ammunition;
    private final Integer count;
}
