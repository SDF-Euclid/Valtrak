package com.example.valtrak.Data.CardLibrary.Interfaces.Items;

import com.example.valtrak.Data.CardLibrary.Enums.SupplyInfo.SpecialItemEffect;

/**
 *
 */
public interface SpecialItemInterface extends ItemCardInterface{
    SpecialItemEffect getSpecialItemEffect();
    Double getPrimaryEffectValue();
    Double getSecondaryEffectValue();
}