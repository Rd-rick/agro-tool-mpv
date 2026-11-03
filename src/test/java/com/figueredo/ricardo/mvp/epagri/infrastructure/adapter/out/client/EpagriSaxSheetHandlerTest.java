package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.client;

import com.figueredo.ricardo.mvp.epagri.domain.model.CotacaoPrecoRegistro;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class EpagriSaxSheetHandlerTest {

    @Test
    void deveProcessarLinhasFiltrandoProdutosEPracasMapeados() {
        List<CotacaoPrecoRegistro> registrosSalvos = new ArrayList<>();
        Map<String, Long> mapaProdutos = Map.of("Soja industrial - sc 60 kg", 10L);
        Map<String, Long> mapaPracas = Map.of("Alto Vale do Itajaí", 20L);

        EpagriSaxSheetHandler handler = new EpagriSaxSheetHandler(
                FrequenciaCotacao.DIARIA,
                registrosSalvos::addAll,
                100,
                mapaProdutos,
                mapaPracas
        );

        handler.startRow(0);
        handler.cell("A1", "Título do Arquivo", null);
        handler.endRow(0);

        handler.startRow(2);
        handler.cell("A3", "Ano / Mês / Dia", null);
        handler.cell("B3", "Mês Preço", null);
        handler.cell("C3", "dia", null);
        handler.cell("D3", "Produto", null);
        handler.cell("E3", "nom_praca", null);
        handler.cell("F3", "Val. Minimo", null);
        handler.cell("G3", "Val. Mais Comum", null);
        handler.cell("H3", "Val. Maximo", null);
        handler.endRow(2);

        handler.startRow(3);
        handler.cell("A4", "2026", null);
        handler.cell("B4", "fevereiro", null);
        handler.cell("C4", "3", null);
        handler.cell("D4", "Boi gordo - arroba", null);
        handler.cell("E4", "Alto Vale do Itajaí", null);
        handler.cell("F4", "180", null);
        handler.cell("G4", "190", null);
        handler.cell("H4", "200", null);
        handler.endRow(3);

        handler.startRow(4);
        handler.cell("A5", "2026", null);
        handler.cell("B5", "março", null);
        handler.cell("C5", "10", null);
        handler.cell("D5", "Soja industrial - sc 60 kg", null);
        handler.cell("E5", "Alto Vale do Itajaí", null);
        handler.cell("F5", "120,50", null);
        handler.cell("G5", "125,00", null);
        handler.cell("H5", "130,00", null);
        handler.endRow(4);

        handler.finalizar();

        assertEquals(1, handler.getTotalProcessados());
        assertFalse(registrosSalvos.isEmpty());
        CotacaoPrecoRegistro reg = registrosSalvos.getFirst();
        assertEquals(10L, reg.produtoId());
        assertEquals(20L, reg.pracaId());
        assertEquals(LocalDate.of(2026, 3, 10), reg.dataCotacao());
        assertEquals(new BigDecimal("125.00"), reg.precoReferencia());
        assertEquals(new BigDecimal("120.50"), reg.precoMinimo());
        assertEquals(new BigDecimal("130.00"), reg.precoMaximo());
        assertEquals(FrequenciaCotacao.DIARIA, reg.frequencia());
    }

    @Test
    void deveProcessarCotacoesComFrequenciaSemanal() {
        List<CotacaoPrecoRegistro> registrosSalvos = new ArrayList<>();
        Map<String, Long> mapaProdutos = Map.of("Milho-amarelo - sc 60 kg", 15L);
        Map<String, Long> mapaPracas = Map.of("Oeste", 25L);

        EpagriSaxSheetHandler handler = new EpagriSaxSheetHandler(
                FrequenciaCotacao.SEMANAL,
                registrosSalvos::addAll,
                100,
                mapaProdutos,
                mapaPracas
        );

        handler.startRow(6);
        handler.cell("A7", "Ano / Mês / Dia", null);
        handler.cell("B7", "Mês Preço", null);
        handler.cell("C7", "dia", null);
        handler.cell("D7", "Produto", null);
        handler.cell("E7", "nom_praca", null);
        handler.cell("F7", "Val. Minimo", null);
        handler.cell("G7", "Val. Mais Comum", null);
        handler.cell("H7", "Val. Maximo", null);
        handler.endRow(6);

        handler.startRow(7);
        handler.cell("A8", "2026", null);
        handler.cell("B8", "fevereiro", null);
        handler.cell("C8", "8", null);
        handler.cell("D8", "Milho-amarelo - sc 60 kg", null);
        handler.cell("E8", "Oeste", null);
        handler.cell("F8", "60,00", null);
        handler.cell("G8", "65,50", null);
        handler.cell("H8", "70,00", null);
        handler.endRow(7);

        handler.finalizar();

        assertEquals(1, handler.getTotalProcessados());
        assertFalse(registrosSalvos.isEmpty());
        CotacaoPrecoRegistro reg = registrosSalvos.getFirst();
        assertEquals(15L, reg.produtoId());
        assertEquals(25L, reg.pracaId());
        assertEquals(LocalDate.of(2026, 2, 8), reg.dataCotacao());
        assertEquals(new BigDecimal("65.50"), reg.precoReferencia());
        assertEquals(new BigDecimal("60.00"), reg.precoMinimo());
        assertEquals(new BigDecimal("70.00"), reg.precoMaximo());
        assertEquals(FrequenciaCotacao.SEMANAL, reg.frequencia());
    }

    @Test
    void deveSuportarMesNumericoEMilharFormatadoPadraoAmericanoEBrasileiro() {
        List<CotacaoPrecoRegistro> registrosSalvos = new ArrayList<>();
        Map<String, Long> mapaProdutos = Map.of("Soja industrial - sc 60 kg", 10L);
        Map<String, Long> mapaPracas = Map.of("Oeste", 25L);

        EpagriSaxSheetHandler handler = new EpagriSaxSheetHandler(
                FrequenciaCotacao.DIARIA,
                registrosSalvos::addAll,
                100,
                mapaProdutos,
                mapaPracas
        );

        handler.startRow(0);
        handler.cell("A1", "Ano", null);
        handler.cell("B1", "Mês", null);
        handler.cell("C1", "Dia", null);
        handler.cell("D1", "Produto", null);
        handler.cell("E1", "nom_praca", null);
        handler.cell("F1", "Val. Minimo", null);
        handler.cell("G1", "Val. Mais Comum", null);
        handler.cell("H1", "Val. Maximo", null);
        handler.cell("I1", "Grupo Produto", null);
        handler.endRow(0);

        handler.startRow(1);
        handler.cell("A2", "2026", null);
        handler.cell("B2", "03", null);
        handler.cell("C2", "15", null);
        handler.cell("D2", "Soja industrial - sc 60 kg", null);
        handler.cell("E2", "Oeste", null);
        handler.cell("F2", "1.100,25", null);
        handler.cell("G2", "1,250.50", null);
        handler.cell("H2", "1.300,75", null);
        handler.cell("I2", "Grãos", null);
        handler.endRow(1);

        handler.finalizar();

        assertEquals(1, handler.getTotalProcessados());
        CotacaoPrecoRegistro reg = registrosSalvos.getFirst();
        assertEquals(LocalDate.of(2026, 3, 15), reg.dataCotacao());
        assertEquals(new BigDecimal("1100.25"), reg.precoMinimo());
        assertEquals(new BigDecimal("1250.50"), reg.precoReferencia());
        assertEquals(new BigDecimal("1300.75"), reg.precoMaximo());
    }
}
