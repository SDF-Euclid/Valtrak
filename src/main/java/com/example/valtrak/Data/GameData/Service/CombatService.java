package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.GameData.Config.ArmorBracketHelper;
import com.example.valtrak.Data.GameData.DataTransfer.DamageData.CombatResult;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.DamageTypeMatchupEntity;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleAttackEntity;
import com.example.valtrak.Data.GameData.Repository.EnumData.DamageTypeMatchupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//TODO: REVIEW DAMAGE CALCULATIONS FOR BALANCE

/**
 *
 */
@Service
@RequiredArgsConstructor
public class CombatService {

    private final DamageTypeMatchupRepository matchupRepo;

    public CombatResult calculateDamage(VehicleAttackEntity attack, Ammunition ammoUsed, int targetBaseArmor, int targetBreachStacks) {
        //Get target's effective armor
        int effectiveArmor = ArmorBracketHelper.getEffectiveArmor(targetBaseArmor, targetBreachStacks);

        //Find armor bracket
        ArmorBracket bracket = ArmorBracketHelper.getBracket(effectiveArmor);

        //Ammo properties
        DamageType damageType = ammoUsed.getDamageType();
        int caliber = ammoUsed.getCaliber();
        int baseDamage = attack.getBaseDamage();

        //Check Overpressure
        if (ArmorBracketHelper.isOverpressure(damageType, bracket, caliber, effectiveArmor)) {
            return CombatResult.builder()
                    .finalDamage(baseDamage)
                    .isTrueDamage(true)
                    .triggeredEffect(SpecialEffect.OVERPRESSURE)
                    .build();
        }

        //Damage modifier from matchup table
        DamageTypeMatchupEntity matchup = matchupRepo
                .findByDamageTypeAndArmorBracket(damageType, bracket)
                .orElseThrow(() -> new RuntimeException(
                        "Matchup not found for: " + damageType + " vs " + bracket
                ));

        //Check pierce
        boolean piercing = ArmorBracketHelper.isPierce(damageType, bracket,
                caliber, effectiveArmor);

        //Final damage calc
        double modifiedDamage = baseDamage * matchup.getDamageModifier();
        int armorReduction = piercing ? 0 : (int) Math.round(effectiveArmor * 0.15);
        int finalDamage = Math.max(1, (int) Math.round(modifiedDamage) - armorReduction);


        //Apply special effect
        SpecialEffect triggeredEffect = determineEffect(
                damageType, bracket, caliber, effectiveArmor,
                matchup.getAutoEffect(), attack.getSpecialEffect()
        );

        return CombatResult.builder()
                .finalDamage(finalDamage)
                .isTrueDamage(false)
                .isPiercing(piercing)
                .triggeredEffect(triggeredEffect)
                .build();
    }

    /**
     * Determines which special effect triggers, prioritizing
     * caliber-based effects over auto effects from matchup table
     */
    private SpecialEffect determineEffect(DamageType damageType, ArmorBracket bracket, int caliber, int effectiveArmor, SpecialEffect autoEffect, SpecialEffect attackSlotEffect) {
        //Effects based on caliber take priority
        if (ArmorBracketHelper.isBreach(damageType, bracket, caliber, effectiveArmor)) return SpecialEffect.BREACH;

        if (ArmorBracketHelper.isPierce(damageType, bracket, caliber, effectiveArmor)) return SpecialEffect.PIERCE;

        if (ArmorBracketHelper.isSuppress(damageType, bracket, caliber, effectiveArmor)) return SpecialEffect.SUPPRESSION;

        if (damageType == DamageType.ELECTRIC) return SpecialEffect.DISABLE;

        //Auto applied effects from matchup table
        if (autoEffect != SpecialEffect.NONE) return autoEffect;

        //Default case
        return attackSlotEffect;
    }
}
