package com.figueredo.ricardo.mvp.epagri.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CotacaoPrecoRegistroTest {

    @Test
    void deveCriarRegistroValidoComTodosOsCampos() {
        CotacaoPrecoRegistro registro = new CotacaoPrecoRegistro(
                1L,
                2L,
                LocalDate.of(2026, 2, 3),
                new BigDecimal("120.00"),
                new BigDecimal("125.50"),
                new BigDecimal("130.00"),
                FrequenciaCotacao.DIARIA
        );

        assertNotNull(registro);
        assertEquals(1L, registro.produtoId());
        assertEquals(2L, registro.pracaId());
        assertEquals(LocalDate.of(2026, 2, 3), registro.dataCotacao());
        assertEquals(new BigDecimal("125.50"), registro.precoReferencia());
        assertEquals(FrequenciaCotacao.DIARIA, registro.frequencia());
    }

    @Test
    void devePermitirPrecoMinimoEMaximoNulos() {
        CotacaoPrecoRegistro registro = new CotacaoPrecoRegistro(
                1L,
                2L,
                LocalDate.of(2026, 2, 3),
                null,
                new BigDecimal("125.50"),
                null,
                FrequenciaCotacao.SEMANAL
        );

        assertNotNull(registro);
        assertEquals(new BigDecimal("125.50"), registro.precoReferencia());
    }

    @Test
    void deveLancarExcecaoQuandoCamposObrigatoriosForemNulos() {
        assertThrows(NullPointerException.class, () -> new CotacaoPrecoRegistro(
                null,
                2L,
                LocalDate.of(2026, 2, 3),
                null,
                new BigDecimal("125.50"),
                null,
                FrequenciaCotacao.DIARIA
        ));

        assertThrows(NullPointerException.class, () -> new CotacaoPrecoRegistro(
                1L,
                null,
                LocalDate.of(2026, 2, 3),
                null,
                new BigDecimal("125.50"),
                null,
                FrequenciaCotacao.DIARIA
        ));

        assertThrows(NullPointerException.class, () -> new CotacaoPrecoRegistro(
                1L,
                2L,
                null,
                null,
                new BigDecimal("125.50"),
                null,
                FrequenciaCotacao.DIARIA
        ));

        assertThrows(NullPointerException.class, () -> new CotacaoPrecoRegistro(
                1L,
                2L,
                LocalDate.of(2026, 2, 3),
                null,
                null,
                null,
                FrequenciaCotacao.DIARIA
        ));

        assertThrows(NullPointerException.class, () -> new CotacaoPrecoRegistro(
                1L,
                2L,
                LocalDate.of(2026, 2, 3),
                null,
                new BigDecimal("125.50"),
                null,
                null
        ));
    }

    @Test
    void deveLancarExcecaoQuandoPrecosForemNegativos() {
        assertThrows(IllegalArgumentException.class, () -> new CotacaoPrecoRegistro(
                1L,
                2L,
                LocalDate.of(2026, 2, 3),
                null,
                new BigDecimal("-1.00"),
                null,
                FrequenciaCotacao.DIARIA
        ));

        assertThrows(IllegalArgumentException.class, () -> new CotacaoPrecoRegistro(
                1L,
                2L,
                LocalDate.of(2026, 2, 3),
                new BigDecimal("-0.01"),
                new BigDecimal("10.00"),
                null,
                FrequenciaCotacao.DIARIA
        ));

        assertThrows(IllegalArgumentException.class, () -> new CotacaoPrecoRegistro(
                1L,
                2L,
                LocalDate.of(2026, 2, 3),
                null,
                new BigDecimal("10.00"),
                new BigDecimal("-5.00"),
                FrequenciaCotacao.DIARIA
        ));
    }
}
