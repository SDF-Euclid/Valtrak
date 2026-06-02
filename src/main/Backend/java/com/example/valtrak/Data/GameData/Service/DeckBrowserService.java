package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.Repository.Cards.AmmunitionCardRepository;
import com.example.valtrak.Data.GameData.Repository.Cards.VehicleCardRepository;
import com.example.valtrak.Gameplay.Cards.Resource.AmmunitionCard;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeckBrowserService {

    private final VehicleCardRepository vehicleRepo;
    private final AmmunitionCardRepository ammoRepo;

    public List<GroundVehicleCard> getAllVehicleCards() {
        return vehicleRepo.findAll(Sort.by("name"));
    }

    public List<AmmunitionCard> getAllAmmunitionCards() {
        return ammoRepo.findAll(Sort.by("name"));
    }
}
