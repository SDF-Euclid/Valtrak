package com.example.valtrak.Data.GameData.Service;

import com.example.valtrak.Data.GameData.Config.DeckRules;
import com.example.valtrak.Data.GameData.DataTransfer.DeckData.DeckDtos.*;
import com.example.valtrak.Data.GameData.Entity.Deck;
import com.example.valtrak.Data.GameData.ExceptionHandling.Exceptions.ApiException;
import com.example.valtrak.Data.GameData.Repository.Cards.CardRepository;
import com.example.valtrak.Data.GameData.Repository.DeckRepository;
import com.example.valtrak.Data.GameData.Repository.PlayerRepository;
import com.example.valtrak.Gameplay.Cards.Base.Card;
import com.example.valtrak.Gameplay.Cards.Vehicle.GroundVehicleCard;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Saved decks. A deck always belongs to one player and is only visible to them. */
@Service
@RequiredArgsConstructor
@Transactional
public class DeckService {

    private final DeckRepository decks;
    private final PlayerRepository players;
    private final CardRepository cards;

    @Transactional(readOnly = true)
    public List<DeckDto> list(Long playerId) {
        return decks.findByPlayerIdOrderByUpdatedAtDesc(playerId).stream().map(this::toDto).toList();
    }

    public DeckDto create(Long playerId, SaveDeckRequest req) {
        String name = validateName(req.name());
        Map<Long, Integer> counts = validateCards(req.cardCounts());
        if (decks.countByPlayerId(playerId) >= DeckRules.MAX_SAVED_DECKS) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "You can save up to " + DeckRules.MAX_SAVED_DECKS + " decks. Delete one first.");
        }
        if (decks.existsByPlayerIdAndNameIgnoreCase(playerId, name)) {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a deck called \"" + name + "\".");
        }
        var player = players.findById(playerId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
        Deck deck = new Deck(player, name);
        deck.setCardCounts(counts);
        return toDto(decks.saveAndFlush(deck));
    }

    public DeckDto update(Long playerId, Long deckId, SaveDeckRequest req) {
        Deck deck = require(playerId, deckId);
        String name = validateName(req.name());
        Map<Long, Integer> counts = validateCards(req.cardCounts());
        if (!name.equalsIgnoreCase(deck.getName()) && decks.existsByPlayerIdAndNameIgnoreCase(playerId, name)) {
            throw new ApiException(HttpStatus.CONFLICT, "You already have a deck called \"" + name + "\".");
        }
        deck.setName(name);
        deck.getCardCounts().clear();
        deck.getCardCounts().putAll(counts);
        return toDto(decks.saveAndFlush(deck));
    }

    public void delete(Long playerId, Long deckId) {
        decks.delete(require(playerId, deckId));
    }

    /** Someone else's deck looks exactly like a deck that doesn't exist. */
    private Deck require(Long playerId, Long deckId) {
        return decks.findByIdAndPlayerId(deckId, playerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Deck not found."));
    }

    private static String validateName(String raw) {
        String name = raw == null ? "" : raw.trim();
        if (name.isEmpty() || name.length() > DeckRules.MAX_NAME_LENGTH) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Deck names must be 1-" + DeckRules.MAX_NAME_LENGTH + " characters.");
        }
        return name;
    }

    /** Checks sizes and that every card exists; drops nothing silently. */
    private Map<Long, Integer> validateCards(Map<Long, Integer> requested) {
        if (requested == null) requested = Map.of();
        int total = 0;
        for (var e : requested.entrySet()) {
            int copies = e.getValue() == null ? 0 : e.getValue();
            if (copies < 1 || copies > DeckRules.MAX_COPIES) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Each card can appear 1-" + DeckRules.MAX_COPIES + " times in a deck.");
            }
            total += copies;
        }
        if (total > DeckRules.MAX_DECK_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "A deck can have at most " + DeckRules.MAX_DECK_SIZE + " cards.");
        }
        long found = cards.findAllById(requested.keySet()).size();
        if (found != requested.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "The deck contains a card that doesn't exist.");
        }
        return new LinkedHashMap<>(requested);
    }

    private DeckDto toDto(Deck deck) {
        int total = deck.getCardCounts().values().stream().mapToInt(Integer::intValue).sum();
        boolean hasVehicle = cards.findAllById(deck.getCardCounts().keySet()).stream()
                .anyMatch(c -> c instanceof GroundVehicleCard);
        boolean playable = total > 0 && total <= DeckRules.MAX_DECK_SIZE && hasVehicle;
        return new DeckDto(deck.getId(), deck.getName(), new LinkedHashMap<>(deck.getCardCounts()),
                total, playable, deck.getUpdatedAt());
    }
}
