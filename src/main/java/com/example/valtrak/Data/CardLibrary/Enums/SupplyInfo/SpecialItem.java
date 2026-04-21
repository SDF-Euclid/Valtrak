package com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo;

import com.example.valtrak.Data.CardLibrary.CardLevel;
import com.example.valtrak.Data.CardLibrary.Interfaces.Items.SpecialItemInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 */
@Getter
@AllArgsConstructor
public enum SpecialItem implements SpecialItemInterface {

    /*==================== DRAW CARDS ====================*/

    ADVANCED_INTEL("Advanced Intel Report", "Search your deck for 2 ammo supply cards, reveal them, and put them in your hand", CardLevel.UNCOMMON, ItemType.SPECIAL, 1, SpecialItemEffect.DRAW_CARDS, 2.0, 0.0),
    EMERGENCY_AIRDROP("Emergency Airdrop", "Draw 2 cards", CardLevel.UNCOMMON, ItemType.SPECIAL, 1, SpecialItemEffect.DRAW_CARDS, 2.0, 0.0),

    /*====================================================*/

    /*==================== ADDITIONAL ARMOR CARDS ====================*/

    ADD_ON_ERA("Add on ERA", "The unit this card is attached to takes 50% less damage from chemical attacks", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.TEMP_ARMOR_INCREASE, .5, 0.0),

    /*================================================================*/

    /*==================== DAMAGE CARDS ====================*/

    AIR_STRIKE("Air Strike", "Target 1 revealed unit for 50 true damage or target each vehicle in a Strike Group for 15 true damage. If a unit is covered by a smoke screen, it does not take any damage", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.DRAW_CARDS, 50.0, 15.0),

    /*======================================================*/

    /*==================== CONCEALMENT CARDS ====================*/

    SMOKESCREEN("Smoke Screen", "The vehicle this card is attached to cannot fire or be hit until your next turn", CardLevel.RARE, ItemType.SPECIAL, 1, SpecialItemEffect.UNTARGETABLE, 1.0, 0.0);

    /*======================================================*/

    /**
     *
     */
    private final String itemName;
    private final String itemDescription;
    private final CardLevel cardLevel;
    private final ItemType itemType;
    private final Integer count;
    private final SpecialItemEffect specialItemEffect;
    private final Double primaryEffectValue;
    private final Double secondaryEffectValue;

    /**
     *
     * @return
     */
    @Override
    public ItemType getItemType() {return ItemType.SPECIAL;}
}
