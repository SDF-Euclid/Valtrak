package com.example.valtrak.Data.GameData.DataTransfer.DamageData;

import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CombatResult {

    private int finalDamage;
    private boolean isTrueDamage;
    private boolean isPiercing;
    private SpecialEffect triggeredEffect;

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Damage: ").append(finalDamage);
        if (isTrueDamage) sb.append(" (TRUE DAMAGE");
        if (isPiercing) sb.append(" (PIERCING)");
        if (triggeredEffect != SpecialEffect.NONE) {
            sb.append(" | Effect: ").append(triggeredEffect);
        }
        return sb.toString();
    }
}
