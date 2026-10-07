package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import com.example.valtrak.Gameplay.Cards.Base.ItemCard;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Resource.FuelCard;
import com.example.valtrak.Gameplay.Cards.Resource.RepairCard;
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

    public List<CardDto> getAllCards() {
        return cardRepo.findAll().stream().map(this::toDto).toList();
    }

    private CardDto toDto(Card card) {
        String level = card.getLevel() != null ? card.getLevel().name() : null;
        return switch (card) {
            case GroundVehicleCard v -> new CardDto(v.getId(), v.getName(), v.getDescription(), level,
                    "VEHICLE", v.getVehicleNation(),
                    v.getVehicleClass() != null ? v.getVehicleClass().getClassName() : null,
                    v.getVehicleHP(), v.getVehicleArmor(), null, null, null, null, null);
            case AmmunitionCard a -> new CardDto(a.getId(), a.getName(), a.getDescription(), level,
                    "AMMUNITION", null, null, null, null,
                    a.getAmmunition() != null ? a.getAmmunition().getDamageType().name() : null,
                    a.getAmmunition() != null ? a.getAmmunition().name() : null,
                    itemType(a), a.getCount(), null);
            case FuelCard f -> new CardDto(f.getId(), f.getName(), f.getDescription(), level,
                    "FUEL", null, null, null, null, null, null, itemType(f), f.getCount(), null);
            case RepairCard r -> new CardDto(r.getId(), r.getName(), r.getDescription(), level,
                    "REPAIR", null, null, null, null, null, null, itemType(r), r.getCount(), r.getRepairAmount());
            default -> new CardDto(card.getId(), card.getName(), card.getDescription(), level,
                    "OTHER", null, null, null, null, null, null,
                    card instanceof ItemCard i ? itemType(i) : null, null, null);
        };
    }

    private static String itemType(ItemCard i) {
        return i.getItemType() != null ? i.getItemType().name() : null;
    }
}
