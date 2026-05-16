package com.example.valtrak.Data.GameData.DataTransfer.GameData;

public record AttackRequest(
        Long attackerFieldUnitId,
        String attackSlot,           // "ATTACK_1", "ATTACK_2", or "ATTACK_3"
        Long targetFieldUnitId,
        String ammoType              // Ammunition enum name (e.g. "APFSDS_120MM")
) {}
