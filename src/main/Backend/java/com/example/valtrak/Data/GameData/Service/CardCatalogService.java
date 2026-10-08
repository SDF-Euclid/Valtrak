package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Resource.FuelCard;
import com.example.valtrak.Gameplay.Cards.Resource.RepairCard;
import com.example.valtrak.Gameplay.Cards.Resource.SupplyCard;
import com.example.valtrak.Gameplay.Cards.Special.SpecialItemCard;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only catalog of every card, converted to {@link CardDto}s so clients
 * never see (or depend on) the JPA entities.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardCatalogService {

    private final CardRepository cardRepo;

    /** Cards don't change while the server runs, so the list is built once (one set of queries) and kept. */
    private volatile List<CardDto> cache;

    public List<CardDto> getAllCards() {
        List<CardDto> cards = cache;
        if (cards == null) {
            cards = cardRepo.findAll().stream().map(this::toDto).toList();
            cache = cards;
        }
        return cards;
    }

    /** Forget the cached list (the card loader calls this when it has finished). */
    public void refresh() {
        cache = null;
    }

    private CardDto toDto(Card card) {
        String level = card.getLevel() != null ? card.getLevel().name() : null;
        return switch (card) {
            case GroundVehicleCard v -> new CardDto(v.getId(), v.getName(), v.getDescription(), level,
                    "VEHICLE", v.getVehicleNation(),
                    v.getVehicleClass() != null ? v.getVehicleClass().getClassName() : null,
                    v.getVehicleHP(), v.getVehicleArmor(), null, null, null, null, null, abilityText(v),
                    attacksOf(v), v.getAbilityType() == null ? null : v.getAbilityPower(),
                    v.getAbilityType() == null ? null : v.getAbilityFuelCost(), null, null,
                    v.getVehicleType() != null && "AIR".equals(v.getVehicleType().getName()));
            case AmmunitionCard a -> new CardDto(a.getId(), a.getName(), a.getDescription(), level,
                    "AMMUNITION", null, null, null, null,
                    a.getAmmunition() != null ? a.getAmmunition().getDamageType().name() : null,
                    a.getAmmunition() != null ? a.getAmmunition().name() : null,
                    itemType(a), a.getCount(), null, null);
            case FuelCard f -> new CardDto(f.getId(), f.getName(), f.getDescription(), level,
                    "FUEL", null, null, null, null, null, null, itemType(f), f.getCount(), null, null);
            case SupplyCard sc -> new CardDto(sc.getId(), sc.getName(), sc.getDescription(), level,
                    "SUPPLY", null, null, null, null, null, null, itemType(sc), sc.getCount(), null, null);
            case RepairCard r -> new CardDto(r.getId(), r.getName(), r.getDescription(), level,
                    "REPAIR", null, null, null, null, null, null, itemType(r), r.getCount(), r.getRepairAmount(), null);
            case SpecialItemCard sp -> new CardDto(sp.getId(), sp.getName(), sp.getDescription(), level,
                    "ITEM", null, null, null, null, null, null, sp.getEffect().name(), null, null, specialText(sp),
                    null, null, null, sp.getPrimaryValue(), sp.getSecondaryValue(), null);
            default -> new CardDto(card.getId(), card.getName(), card.getDescription(), level,
                    "OTHER", null, null, null, null, null, null,
                    card instanceof ItemCard i ? itemType(i) : null, null, null, null);
        };
    }

    private static List<CardDto.AttackDto> attacksOf(GroundVehicleCard v) {
        return v.getAttacks().stream()
                .sorted(java.util.Comparator.comparing(a -> a.getAttackSlot()))
                .map(a -> new CardDto.AttackDto(a.getAttackSlot().name(), a.getAttackName(), a.getBaseDamage(), a.getAmmoCost(),
                        a.getFuelCost(),
                        com.example.valtrak.Data.CardLibrary.Enums.WeaponInfo.Weapon.valueOf(a.getWeapon().getWeaponName())
                                .getCompatibleAmmunition().stream().map(Enum::name).toList(),
                        a.getSpecialEffect() == null ? null : a.getSpecialEffect().name()))
                .toList();
    }

    private static String abilityText(GroundVehicleCard v) {
        if (v.getAbilityType() == null) return null;
        return switch (v.getAbilityType()) {
            case REVEAL_ENEMY -> "Reveals up to " + v.getAbilityPower() + " enemy vehicle" + (v.getAbilityPower() == 1 ? "" : "s")
                    + " (" + v.getAbilityFuelCost() + " Fuel, while face up)";
        };
    }

    /** The short effect line shown on an item card. */
    private static String specialText(SpecialItemCard c) {
        int power = c.getPrimaryValue() == null ? 0 : c.getPrimaryValue();
        int second = c.getSecondaryValue() == null ? 0 : c.getSecondaryValue();
        return switch (c.getEffect()) {
            case ERA_PROTECTION -> "Attached: " + power + "% less chemical damage";
            case ARTILLERY_STRIKE -> power + " explosive damage x" + second + " target" + (second == 1 ? "" : "s");
            case SEARCH_RESOURCES -> "Search: " + power + " resource card" + (power == 1 ? "" : "s");
            case SEARCH_TANKS -> "Search: " + power + " tank" + (power == 1 ? "" : "s");
            case SEARCH_SUPPORT -> "Search: " + power + " support vehicle" + (power == 1 ? "" : "s");
            case DRAW_CARDS -> "Draw " + power + " card" + (power == 1 ? "" : "s");
            case SMOKE_SCREEN -> "Smoke: " + power + " vehicle" + (power == 1 ? "" : "s") + " untargetable";
            case JAMMER -> "Group Jammer: blocks enemy reveals while on (" + power + " Fuel per turn, " + second + " HP)";
            case CAMOUFLAGE -> "Attached: retreat costs " + power + " less Fuel";
            case SABOTAGE -> "Opponent discards up to " + power + " at random (1 per turn)";
            case RECYCLE -> "Recycle: " + power + " resource card" + (power == 1 ? "" : "s") + " from the discard pile";
            case AIRDROP -> "Drop " + power + " Ammo/Fuel card" + (power == 1 ? "" : "s") + " from the Depot into a pool (1 per turn)";
            case RAPID_DEPLOYMENT -> "Deploy " + power + " vehicle" + (power == 1 ? "" : "s") + " free of the formation cost";
        };
    }

    private static String itemType(ItemCard i) {
        return i.getItemType() != null ? i.getItemType().name() : null;
    }
}
