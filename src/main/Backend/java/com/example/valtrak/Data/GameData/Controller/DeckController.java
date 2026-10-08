package com.example.valtrak.Data.GameData.Controller;

import com.example.valtrak.Data.GameData.DataTransfer.DeckData.DeckDtos.*;
import com.example.valtrak.Data.GameData.Service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** The signed-in player's saved decks. */
@RestController
@RequestMapping("/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    public List<DeckDto> list(Authentication auth) {
        return deckService.list(playerId(auth));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeckDto create(Authentication auth, @RequestBody SaveDeckRequest request) {
        return deckService.create(playerId(auth), request);
    }

    @PutMapping("/{deckId}")
    public DeckDto update(Authentication auth, @PathVariable Long deckId, @RequestBody SaveDeckRequest request) {
        return deckService.update(playerId(auth), deckId, request);
    }

    @DeleteMapping("/{deckId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication auth, @PathVariable Long deckId) {
        deckService.delete(playerId(auth), deckId);
    }

    private static Long playerId(Authentication auth) {
        return (Long) auth.getPrincipal();
    }
}
