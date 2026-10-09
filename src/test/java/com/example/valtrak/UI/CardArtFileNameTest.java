package com.example.valtrak.UI;

import com.example.valtrak.UI.components.CardTile;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Real card pictures are found by a file name made from the card's name. */
class CardArtFileNameTest {

    @Test
    void fileNamesAreLowerCaseWithDashes() {
        assertThat(CardTile.artFileName("Leopard 2A7V")).isEqualTo("leopard-2a7v");
        assertThat(CardTile.artFileName("Sho't Kal")).isEqualTo("sho-t-kal");
        assertThat(CardTile.artFileName("5x 120mm HEAT Crate")).isEqualTo("5x-120mm-heat-crate");
        assertThat(CardTile.artFileName("  M1A3 Abrams! ")).isEqualTo("m1a3-abrams");
        assertThat(CardTile.artFileName(null)).isEmpty();
    }
}
