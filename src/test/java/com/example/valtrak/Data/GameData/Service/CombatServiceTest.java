package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.ArmorBracket;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Ammunition;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.DamageType;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.SpecialEffect;
import com.example.valtrak.Data.GameData.Config.ArmorBracketHelper;
import com.example.valtrak.Data.GameData.DataTransfer.DamageData.CombatResult;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.DamageTypeMatchupEntity;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleAttackEntity;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.WeaponEntity;
import com.example.valtrak.Data.GameData.Repository.EnumData.DamageTypeMatchupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatServiceTest {

    @Mock
    private DamageTypeMatchupRepository matchupRepo;

    @InjectMocks
    private CombatService combatService;

    private VehicleAttackEntity attack;

    @BeforeEach
    void setUp() {
        WeaponEntity weapon = new WeaponEntity();
        attack = new VehicleAttackEntity();
        attack.setBaseDamage(45);
        attack.setSpecialEffect(SpecialEffect.NONE);
        attack.setWeapon(weapon);
    }

    // ==================== ARMOR BRACKET TESTS ====================

    @Test
    void testGetBracket_Unarmored() {
        assertEquals(ArmorBracket.UNARMORED, ArmorBracketHelper.getBracket(0));
        assertEquals(ArmorBracket.UNARMORED, ArmorBracketHelper.getBracket(15));
        assertEquals(ArmorBracket.UNARMORED, ArmorBracketHelper.getBracket(30));
    }

    @Test
    void testGetBracket_Light() {
        assertEquals(ArmorBracket.LIGHT, ArmorBracketHelper.getBracket(31));
        assertEquals(ArmorBracket.LIGHT, ArmorBracketHelper.getBracket(50));
        assertEquals(ArmorBracket.LIGHT, ArmorBracketHelper.getBracket(70));
    }

    @Test
    void testGetBracket_Medium() {
        assertEquals(ArmorBracket.MEDIUM, ArmorBracketHelper.getBracket(71));
        assertEquals(ArmorBracket.MEDIUM, ArmorBracketHelper.getBracket(100));
        assertEquals(ArmorBracket.MEDIUM, ArmorBracketHelper.getBracket(120));
    }

    @Test
    void testGetBracket_Heavy() {
        assertEquals(ArmorBracket.HEAVY, ArmorBracketHelper.getBracket(121));
        assertEquals(ArmorBracket.HEAVY, ArmorBracketHelper.getBracket(150));
        assertEquals(ArmorBracket.HEAVY, ArmorBracketHelper.getBracket(180));
    }

    @Test
    void testGetBracket_SuperHeavy() {
        assertEquals(ArmorBracket.SUPER_HEAVY, ArmorBracketHelper.getBracket(181));
        assertEquals(ArmorBracket.SUPER_HEAVY, ArmorBracketHelper.getBracket(250));
    }

    // ==================== BREACH STACK TESTS ====================

    @Test
    void testGetEffectiveArmor_NoBreach() {
        assertEquals(100, ArmorBracketHelper.getEffectiveArmor(100, 0));
    }

    @Test
    void testGetEffectiveArmor_OneBreach() {
        assertEquals(88, ArmorBracketHelper.getEffectiveArmor(100, 1));
    }

    @Test
    void testGetEffectiveArmor_TwoBreach() {
        assertEquals(77, ArmorBracketHelper.getEffectiveArmor(100, 2));
    }

    @Test
    void testGetEffectiveArmor_MaxBreach() {
        assertEquals(68, ArmorBracketHelper.getEffectiveArmor(100, 3));
    }

    // ==================== OVERPRESSURE TESTS ====================

    @Test
    void testOverpressure_ExplosiveVsUnarmored() {
        assertTrue(ArmorBracketHelper.isOverpressure(
                DamageType.EXPLOSIVE, ArmorBracket.UNARMORED, 120, 20
        ));
    }

    @Test
    void testOverpressure_ExplosiveVsLight() {
        assertTrue(ArmorBracketHelper.isOverpressure(
                DamageType.EXPLOSIVE, ArmorBracket.LIGHT, 120, 50
        ));
    }

    @Test
    void testOverpressure_ExplosiveVsLightFails() {
        assertFalse(ArmorBracketHelper.isOverpressure(
                DamageType.EXPLOSIVE, ArmorBracket.LIGHT, 37, 50
        ));
    }

    @Test
    void testOverpressure_ExplosiveVsHeavy() {
        assertFalse(ArmorBracketHelper.isOverpressure(
                DamageType.EXPLOSIVE, ArmorBracket.HEAVY, 120, 150
        ));
    }

    @Test
    void testOverpressure_ChemicalVsUnarmored() {
        assertTrue(ArmorBracketHelper.isOverpressure(
                DamageType.CHEMICAL, ArmorBracket.UNARMORED, 120, 20
        ));
    }

    @Test
    void testOverpressure_ChemicalVsLightFails() {
        assertFalse(ArmorBracketHelper.isOverpressure(
                DamageType.CHEMICAL, ArmorBracket.LIGHT, 120, 50
        ));
    }

    @Test
    void testOverpressure_KineticNeverTriggers() {
        assertFalse(ArmorBracketHelper.isOverpressure(
                DamageType.KINETIC, ArmorBracket.UNARMORED, 120, 20
        ));
    }

    // ==================== PIERCE TESTS ====================

    @Test
    void testPierce_KineticVsSuperHeavy() {
        assertTrue(ArmorBracketHelper.isPierce(
                DamageType.KINETIC, ArmorBracket.SUPER_HEAVY, 120, 140
        ));
    }

    @Test
    void testPierce_KineticVsMediumFails() {
        assertFalse(ArmorBracketHelper.isPierce(
                DamageType.KINETIC, ArmorBracket.MEDIUM, 120, 100
        ));
    }

    // ==================== BREACH TRIGGER TESTS ====================

    @Test
    void testBreach_KineticVsUnarmored() {
        assertTrue(ArmorBracketHelper.isBreach(
                DamageType.KINETIC, ArmorBracket.UNARMORED, 120, 20
        ));
    }

    @Test
    void testBreach_KineticVsUnarmoredFails() {
        assertFalse(ArmorBracketHelper.isBreach(
                DamageType.KINETIC, ArmorBracket.UNARMORED, 12, 20
        ));
    }

    // ==================== FULL COMBAT CALCULATION TESTS ====================

    @Test
    void testCombat_KineticVsMedium_NormalDamage() {
        DamageTypeMatchupEntity matchup = new DamageTypeMatchupEntity(
                DamageType.KINETIC, ArmorBracket.MEDIUM, 1.0, SpecialEffect.NONE
        );
        when(matchupRepo.findByDamageTypeAndArmorBracket(any(), any()))
                .thenReturn(Optional.of(matchup));

        // armor 100, MEDIUM bracket, no PIERCE (requires HEAVY/SUPER_HEAVY)
        CombatResult result = combatService.calculateDamage(
                attack, Ammunition.APFSDS_120MM, 100, 0
        );

        // (45 * 1.0) - round(100 * 0.15) = 45 - 15 = 30
        assertEquals(30, result.getFinalDamage());
        assertFalse(result.isTrueDamage());
        assertEquals(SpecialEffect.NONE, result.getTriggeredEffect());
    }

    @Test
    void testCombat_ExplosiveVsUnarmored_Overpressure() {
        CombatResult result = combatService.calculateDamage(
                attack, Ammunition.HE_120MM, 20, 0
        );

        assertEquals(45, result.getFinalDamage());
        assertTrue(result.isTrueDamage());
        assertEquals(SpecialEffect.OVERPRESSURE, result.getTriggeredEffect());
    }

    @Test
    void testCombat_KineticVsHeavy_HighModifier() {
        DamageTypeMatchupEntity matchup = new DamageTypeMatchupEntity(
                DamageType.KINETIC, ArmorBracket.HEAVY, 1.3, SpecialEffect.NONE
        );
        when(matchupRepo.findByDamageTypeAndArmorBracket(any(), any()))
                .thenReturn(Optional.of(matchup));

        // armor 155, caliber 120 < 155 * 0.8 = 124 → PIERCE does NOT trigger
        CombatResult result = combatService.calculateDamage(
                attack, Ammunition.APFSDS_120MM, 155, 0
        );

        // (45 * 1.3) - round(155 * 0.15) = 58.5 - 23 = 35
        assertEquals(36, result.getFinalDamage());
        assertFalse(result.isTrueDamage());
        assertFalse(result.isPiercing());
    }

    @Test
    void testCombat_WithBreachStacks_ReducedArmor() {
        DamageTypeMatchupEntity matchup = new DamageTypeMatchupEntity(
                DamageType.KINETIC, ArmorBracket.MEDIUM, 1.0, SpecialEffect.NONE
        );
        when(matchupRepo.findByDamageTypeAndArmorBracket(any(), any()))
                .thenReturn(Optional.of(matchup));

        // effective armor = 100 * 0.88 * 0.88 = 77
        CombatResult result = combatService.calculateDamage(
                attack, Ammunition.APFSDS_120MM, 100, 2
        );

        // (45 * 1.0) - round(77 * 0.15) = 45 - round(11.55) = 45 - 12 = 33
        assertEquals(33, result.getFinalDamage());
    }

    @Test
    void testCombat_Suppression_LowCaliberVsLight() {
        DamageTypeMatchupEntity matchup = new DamageTypeMatchupEntity(
                DamageType.KINETIC, ArmorBracket.LIGHT, 0.8, SpecialEffect.NONE
        );
        when(matchupRepo.findByDamageTypeAndArmorBracket(any(), any()))
                .thenReturn(Optional.of(matchup));

        // 12.7mm vs armor 50 (LIGHT) — caliber(12) < armor(50) → SUPPRESSION
        attack.setBaseDamage(15);
        CombatResult result = combatService.calculateDamage(
                attack, Ammunition.NATO_127x99MM, 50, 0
        );

        assertEquals(SpecialEffect.SUPPRESSION, result.getTriggeredEffect());
    }
}