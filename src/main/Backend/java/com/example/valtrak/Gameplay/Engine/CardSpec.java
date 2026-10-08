package com.example.valtrak.Gameplay.Engine;

import com.example.valtrak.Data.CardLibrary.CardLevel;

/** What the engine needs to know about a card. */
public sealed interface CardSpec permits VehicleSpec, ResourceSpec, ItemSpec {
    long cardId();

    String name();

    CardLevel level();
}
