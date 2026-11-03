package com.figueredo.ricardo.mvp.zarc.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZarcRegistroTest {

    @Test
    void deveCriarRegistroValidoCom36Decendios() {
        List<Integer> decendios = Collections.nCopies(36, 20);

        ZarcRegistro registro = new ZarcRegistro(
                1L,
                2L,
                2024,
                2025,
                "22",
                "Ciclo Médio",
                "14",
                "Solo 2",
                "1",
                "Sequeiro",
                "Port.120",
                decendios
        );

        assertNotNull(registro);
        assertEquals(36, registro.decendios().size());
        assertEquals(20, registro.decendios().getFirst());
    }

    @Test
    void deveLancarExcecaoQuandoDecendiosNaoPossuir36Elementos() {
        List<Integer> invalidos = List.of(20, 20, 30);

        assertThrows(IllegalArgumentException.class, () -> new ZarcRegistro(
                1L,
                2L,
                2024,
                2025,
                "22",
                null,
                "14",
                null,
                "1",
                "Sequeiro",
                "Port.120",
                invalidos
        ));
    }

    @Test
    void deveLancarExcecaoQuandoCamposObrigatoriosForemNulos() {
        List<Integer> decendios = Collections.nCopies(36, 20);

        assertThrows(NullPointerException.class, () -> new ZarcRegistro(
                null,
                2L,
                2024,
                2025,
                "22",
                null,
                "14",
                null,
                "1",
                "Sequeiro",
                "Port.120",
                decendios
        ));
    }

    @Test
    void deveTruncarDescricoesQueExcedamLimiteDoBancoEValidarDecendios() {
        List<Integer> decendios = Collections.nCopies(36, 30);
        String manejoLongo = "Descricao de manejo muito longa que ultrapassa cinquenta caracteres no banco";

        ZarcRegistro registro = new ZarcRegistro(
                1L,
                2L,
                2024,
                2025,
                "22",
                null,
                "14",
                null,
                "1",
                manejoLongo,
                "Port.120",
                decendios
        );

        assertEquals(50, registro.manejoDescricao().length());

        List<Integer> decendiosForaDoIntervalo = Collections.nCopies(36, 150);
        assertThrows(IllegalArgumentException.class, () -> new ZarcRegistro(
                1L, 2L, 2024, 2025, "22", null, "14", null, "1", "Sequeiro", "Port.120", decendiosForaDoIntervalo
        ));
    }
}
