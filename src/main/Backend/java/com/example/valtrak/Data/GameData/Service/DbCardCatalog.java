package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.CardLibrary.Enums.VehicleInfo.VehicleClass;
import com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon;
import com.example.valtrak.Data.GameData.Entity.EnumEntity.VehicleAttackEntity;
import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Resource.FuelCard;
import com.example.valtrak.Gameplay.Cards.Resource.RepairCard;
import com.example.valtrak.Gameplay.Cards.Resource.SupplyCard;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import com.example.valtrak.Gameplay.Engine.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gives the rules engine its card data from the database. Cards don't change while the server runs,
 * so they are read once (on first use, after the seeding at startup) and kept in memory.
 */
@Component
@RequiredArgsConstructor
public class DbCardCatalog implements CardCatalog {

    private final CardRepository cards;
    private final PlatformTransactionManager transactionManager;

    private volatile Map<Long, CardSpec> specs;

    @Override
    public CardSpec find(long cardId) {
        return specs().get(cardId);
    }

    /** Forget the cached cards (they are reloaded on next use). */
    public void refresh() {
        specs = null;
    }

    private Map<Long, CardSpec> specs() {
        Map<Long, CardSpec> loaded = specs;
        if (loaded == null) {
            synchronized (this) {
                if (specs == null) specs = load();
                loaded = specs;
            }
        }
        return loaded;
    }

    private Map<Long, CardSpec> load() {
        return new TransactionTemplate(transactionManager).execute(status -> {
            Map<Long, CardSpec> map = new HashMap<>();
            for (Card card : cards.findAll()) {
                CardSpec spec = toSpec(card);
                if (spec != null) map.put(card.getId(), spec);
            }
            return map;
        });
    }

    private static CardSpec toSpec(Card card) {
        return switch (card) {
            case GroundVehicleCard v -> new VehicleSpec(v.getId(), v.getName(), v.getLevel(),
                    VehicleClass.valueOf(v.getVehicleClass().getClassName()),
                    v.getVehicleHP(), v.getVehicleArmor(), attacksOf(v),
                    v.getAbilityType() == null ? null
                            : new AbilitySpec(v.getAbilityType(), v.getAbilityPower(), v.getAbilityFuelCost()));
            case AmmunitionCard a -> new ResourceSpec(a.getId(), a.getName(), a.getLevel(), ResourceKind.AMMO,
                    a.getAmmunition(), a.getCount());
            case FuelCard f -> new ResourceSpec(f.getId(), f.getName(), f.getLevel(), ResourceKind.FUEL, null, f.getCount());
            case SupplyCard s -> new ResourceSpec(s.getId(), s.getName(), s.getLevel(), ResourceKind.SUPPLY, null, s.getCount());
            case RepairCard r -> new ResourceSpec(r.getId(), r.getName(), r.getLevel(), ResourceKind.REPAIR, null, r.getRepairAmount());
            default -> null;   // card types the rules don't use yet
        };
    }

    private static List<AttackSpec> attacksOf(GroundVehicleCard v) {
        return v.getAttacks().stream()
                .sorted(Comparator.comparing(VehicleAttackEntity::getAttackSlot))
                .map(a -> new AttackSpec(a.getAttackSlot(), a.getAttackName(),
                        Weapon.valueOf(a.getWeapon().getWeaponName()), a.getBaseDamage(),
                        a.getAmmoCost(), a.getFuelCost(), a.getSpecialEffect()))
                .toList();
    }
}
