package com.figueredo.ricardo.mvp.pam.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PamProducaoRegistroTest {

    @Test
    void deveCriarRegistroValidoEIdentificarProducaoEfetiva() {
        PamProducaoRegistro registro = new PamProducaoRegistro(
                10L,
                20L,
                2024,
                24100,
                24100,
                69408,
                2880,
                new BigDecimal("138816.00")
        );

        assertNotNull(registro);
        assertEquals(10L, registro.municipioId());
        assertEquals(20L, registro.culturaCodigoFonteId());
        assertEquals(2024, registro.ano());
        assertEquals(24100, registro.areaPlantadaHa());
        assertEquals(69408, registro.quantidadeProduzidaTon());
        assertTrue(registro.possuiProducaoOuPlantio());
    }

    @Test
    void deveRetornarFalsoQuandoTodasMetricasForemNulasOuZero() {
        PamProducaoRegistro registroNulo = new PamProducaoRegistro(
                10L,
                20L,
                2024,
                null,
                null,
                null,
                null,
                null
        );
        assertFalse(registroNulo.possuiProducaoOuPlantio());

        PamProducaoRegistro registroZero = new PamProducaoRegistro(
                10L,
                20L,
                2024,
                0,
                0,
                0,
                0,
                BigDecimal.ZERO
        );
        assertFalse(registroZero.possuiProducaoOuPlantio());
    }

    @Test
    void deveLancarExcecaoQuandoCamposObrigatoriosForemNulos() {
        assertThrows(NullPointerException.class, () -> new PamProducaoRegistro(
                null, 20L, 2024, 100, 100, 300, 3000, new BigDecimal("500")
        ));
        assertThrows(NullPointerException.class, () -> new PamProducaoRegistro(
                10L, null, 2024, 100, 100, 300, 3000, new BigDecimal("500")
        ));
        assertThrows(NullPointerException.class, () -> new PamProducaoRegistro(
                10L, 20L, null, 100, 100, 300, 3000, new BigDecimal("500")
        ));
    }

    @Test
    void deveLancarExcecaoQuandoAnoOuMetricasForemInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new PamProducaoRegistro(
                10L, 20L, 1950, 100, 100, 300, 3000, new BigDecimal("500")
        ));
        assertThrows(IllegalArgumentException.class, () -> new PamProducaoRegistro(
                10L, 20L, 2024, -1, 100, 300, 3000, new BigDecimal("500")
        ));
        assertThrows(IllegalArgumentException.class, () -> new PamProducaoRegistro(
                10L, 20L, 2024, 100, -1, 300, 3000, new BigDecimal("500")
        ));
        assertThrows(IllegalArgumentException.class, () -> new PamProducaoRegistro(
                10L, 20L, 2024, 100, 100, -1, 3000, new BigDecimal("500")
        ));
        assertThrows(IllegalArgumentException.class, () -> new PamProducaoRegistro(
                10L, 20L, 2024, 100, 100, 300, -1, new BigDecimal("500")
        ));
        assertThrows(IllegalArgumentException.class, () -> new PamProducaoRegistro(
                10L, 20L, 2024, 100, 100, 300, 3000, new BigDecimal("-1.00")
        ));
    }
}
