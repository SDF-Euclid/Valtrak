package com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo;

//TODO: Review and re-balance special effects

public enum SpecialEffect {
    NONE,
    PIERCE,       //Ignores armor modifier
    STUN,         //Target can't attack next turn
    SUPPRESSION,     //Exposed troops can't move next turn (targeted strike group only)
    DISABLE,      //Disables one of target's weapons next turn
    BREACH,       //Reduces target's armor permanently
    OVERPRESSURE
}