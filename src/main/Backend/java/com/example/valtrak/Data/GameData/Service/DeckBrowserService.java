package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only access to every card in the database for the deck builder.
 * Returns the concrete card subtypes (vehicles, ammo, fuel, repair, ...),
 * so new card types show up in the UI without changes here.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeckBrowserService {

    private final CardRepository cardRepo;

    public List<Card> getAllCards() {
        return cardRepo.findAll();
    }
}
