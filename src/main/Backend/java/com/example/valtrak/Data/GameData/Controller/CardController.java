package com.example.valtrak.Data.GameData.Controller;

import com.example.valtrak.Data.GameData.DataTransfer.CardData.CardDto;
import com.example.valtrak.Data.GameData.Service.CardCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardCatalogService catalog;

    @GetMapping
    public List<CardDto> getAllCards() {
        return catalog.getAllCards();
    }
}
