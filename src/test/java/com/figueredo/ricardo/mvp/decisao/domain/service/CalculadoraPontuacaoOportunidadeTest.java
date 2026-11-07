package com.figueredo.ricardo.mvp.decisao.domain.service;

import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.OrigemCotacao;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.mercado.domain.model.ResumoPeriodoMercado;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraPontuacaoOportunidadeTest {

    private final CalculadoraPontuacaoOportunidade calculadora = new CalculadoraPontuacaoOportunidade();

    @Test
    void deveCalcularPontuacaoMaximaDe100PontosEmCenarioIdealDeZarcMercadoEPam() {
        LocalDate dataRef = LocalDate.of(2026, 10, 30);

        CulturaZoneamentoConsolidado sojaZarc = criarCulturaZarc(
                1L,
                "Soja",
                20,
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12),
                3,
                3
        );

        ProdutoMercadoAnalise sojaMercado = criarProdutoAnalise(
                10L,
                "Soja industrial",
                new BigDecimal("125.00"),
                new BigDecimal("5.50"),
                new BigDecimal("9.20"),
                new BigDecimal("12.40"),
                dataRef,
                false,
                "Meio Oeste"
        );

        CulturaMercadoResultado mercado = new CulturaMercadoResultado(1L, "Soja", List.of(sojaMercado));
        HistoricoPamCultura pam = new HistoricoPamCultura(1L, true, 2025, 18000, 68400, 3800);

        ScoreOportunidade score = calculadora.calcular(sojaZarc, mercado, pam, dataRef);

        assertEquals(35, score.pontosPorCriterio().segurancaClimatica());
        assertEquals(20, score.pontosPorCriterio().janelaPlantio());
        assertEquals(30, score.pontosPorCriterio().dinamicaPrecos());
        assertEquals(15, score.pontosPorCriterio().qualidadeInformacao());
        assertEquals(100, score.notaFinal());
        assertEquals("Soja industrial", score.produtoReferencia());
        assertEquals("Meio Oeste", score.pracaReferencia());
        assertFalse(score.fallbackEstadual());
        assertTrue(score.possuiHistoricoPam());
        assertEquals(4, score.justificativas().size());
        assertEquals(2, score.avisosMetodologicos().size());
    }

    @Test
    void naoDeveAlterarPontuacaoQuandoPrecoNominalBrutoForDiferenteComMesmaVariacaoPercentual() {
        LocalDate dataRef = LocalDate.of(2026, 10, 30);

        CulturaZoneamentoConsolidado zarcMilho = criarCulturaZarc(
                2L,
                "Milho",
                20,
                List.of(1, 2, 3, 4, 5, 6),
                2,
                2
        );

        CulturaZoneamentoConsolidado zarcFeijao = criarCulturaZarc(
                3L,
                "Feijão",
                20,
                List.of(1, 2, 3, 4, 5, 6),
                2,
                2
        );

        ProdutoMercadoAnalise produtoPrecoBaixo = criarProdutoAnalise(
                20L,
                "Milho amarelo",
                new BigDecimal("55.00"),
                new BigDecimal("3.20"),
                new BigDecimal("4.50"),
                new BigDecimal("6.10"),
                dataRef,
                false,
                "Meio Oeste"
        );

        ProdutoMercadoAnalise produtoPrecoAlto = criarProdutoAnalise(
                30L,
                "Feijão preto",
                new BigDecimal("320.00"),
                new BigDecimal("3.20"),
                new BigDecimal("4.50"),
                new BigDecimal("6.10"),
                dataRef,
                false,
                "Meio Oeste"
        );

        HistoricoPamCultura pamMilho = new HistoricoPamCultura(2L, true, 2025, 5000, 40000, 8000);
        HistoricoPamCultura pamFeijao = new HistoricoPamCultura(3L, true, 2025, 2000, 4000, 2000);

        ScoreOportunidade scoreMilho = calculadora.calcular(
                zarcMilho,
                new CulturaMercadoResultado(2L, "Milho", List.of(produtoPrecoBaixo)),
                pamMilho,
                dataRef
        );

        ScoreOportunidade scoreFeijao = calculadora.calcular(
                zarcFeijao,
                new CulturaMercadoResultado(3L, "Feijão", List.of(produtoPrecoAlto)),
                pamFeijao,
                dataRef
        );

        assertEquals(
                scoreMilho.pontosPorCriterio().dinamicaPrecos(),
                scoreFeijao.pontosPorCriterio().dinamicaPrecos()
        );
        assertEquals(scoreMilho.notaFinal(), scoreFeijao.notaFinal());
    }

    @Test
    void deveAplicarDiferencaSuaveDeUmPontoQuandoCotacaoVierDeFallbackEstadual() {
        LocalDate dataRef = LocalDate.of(2026, 10, 30);

        CulturaZoneamentoConsolidado trigoZarc = criarCulturaZarc(
                4L,
                "Trigo",
                30,
                List.of(13, 14, 15, 16, 17),
                2,
                2
        );

        ProdutoMercadoAnalise prodRegional = criarProdutoAnalise(
                40L,
                "Trigo superior PH78",
                new BigDecimal("78.00"),
                new BigDecimal("2.00"),
                new BigDecimal("3.00"),
                new BigDecimal("4.00"),
                dataRef,
                false,
                "Meio Oeste"
        );

        ProdutoMercadoAnalise prodFallback = criarProdutoAnalise(
                40L,
                "Trigo superior PH78",
                new BigDecimal("78.00"),
                new BigDecimal("2.00"),
                new BigDecimal("3.00"),
                new BigDecimal("4.00"),
                dataRef,
                true,
                "Média Estadual SC"
        );

        HistoricoPamCultura pam = new HistoricoPamCultura(4L, true, 2025, 3000, 9000, 3000);

        ScoreOportunidade scoreRegional = calculadora.calcular(
                trigoZarc,
                new CulturaMercadoResultado(4L, "Trigo", List.of(prodRegional)),
                pam,
                dataRef
        );

        ScoreOportunidade scoreFallback = calculadora.calcular(
                trigoZarc,
                new CulturaMercadoResultado(4L, "Trigo", List.of(prodFallback)),
                pam,
                dataRef
        );

        assertEquals(15, scoreRegional.pontosPorCriterio().qualidadeInformacao());
        assertEquals(14, scoreFallback.pontosPorCriterio().qualidadeInformacao());
        assertEquals(1, scoreRegional.notaFinal() - scoreFallback.notaFinal());
        assertTrue(scoreFallback.fallbackEstadual());
        assertTrue(scoreFallback.avisosMetodologicos().stream()
                .anyMatch(aviso -> aviso.contains("referência estadual")));
    }

    @Test
    void deveZerarDinamicaDePrecosEGerarAvisosQuandoCulturaNaoTiverCotacaoNemPam() {
        LocalDate dataRef = LocalDate.of(2026, 10, 30);

        CulturaZoneamentoConsolidado aveiaZarc = criarCulturaZarc(
                8L,
                "Aveia",
                40,
                List.of(15, 16),
                3,
                3
        );

        ScoreOportunidade score = calculadora.calcular(
                aveiaZarc,
                new CulturaMercadoResultado(8L, "Aveia", List.of()),
                HistoricoPamCultura.semHistorico(8L),
                dataRef
        );

        assertEquals(15, score.pontosPorCriterio().segurancaClimatica());
        assertEquals(4, score.pontosPorCriterio().janelaPlantio());
        assertEquals(0, score.pontosPorCriterio().dinamicaPrecos());
        assertEquals(0, score.pontosPorCriterio().qualidadeInformacao());
        assertEquals(19, score.notaFinal());
        assertNull(score.precoAtualReferencia());
        assertTrue(score.avisosMetodologicos().stream()
                .anyMatch(a -> a.contains("sem cotação ativa na base da Epagri/Cepa")));
        assertTrue(score.avisosMetodologicos().stream()
                .anyMatch(a -> a.contains("Sem histórico de produção municipal registrado no IBGE/PAM")));
    }

    private CulturaZoneamentoConsolidado criarCulturaZarc(
            Long culturaId,
            String nome,
            int riscoMinimo,
            List<Integer> decendios,
            int qtdSolos,
            int qtdCiclos
    ) {
        List<PeriodoDecendio> janelas = decendios.stream()
                .map(d -> PeriodoDecendio.deDecendio(d, Set.of(riscoMinimo)))
                .toList();

        List<SoloZoneamento> solos = List.of(
                new SoloZoneamento("1", "Tipo 1"),
                new SoloZoneamento("2", "Tipo 2"),
                new SoloZoneamento("3", "Tipo 3")
        ).subList(0, qtdSolos);

        List<CicloZoneamento> ciclos = List.of(
                new CicloZoneamento("1", "Grupo I"),
                new CicloZoneamento("2", "Grupo II"),
                new CicloZoneamento("3", "Grupo III")
        ).subList(0, qtdCiclos);

        return new CulturaZoneamentoConsolidado(
                culturaId,
                nome,
                nome + " cientifico",
                "GRAOS",
                List.of(nome),
                riscoMinimo,
                Set.of(riscoMinimo),
                janelas,
                solos,
                ciclos,
                List.of("Portaria 1")
        );
    }

    private ProdutoMercadoAnalise criarProdutoAnalise(
            Long produtoId,
            String nomeComercial,
            BigDecimal precoAtual,
            BigDecimal var30,
            BigDecimal var90,
            BigDecimal var180,
            LocalDate dataUltima,
            boolean fallbackEstadual,
            String nomePraca
    ) {
        OrigemCotacao origem = new OrigemCotacao(
                "EPAGRI_CEPA",
                fallbackEstadual ? null : 7L,
                nomePraca,
                fallbackEstadual,
                "Observação origem"
        );

        ResumoPeriodoMercado resumo30 = new ResumoPeriodoMercado(
                30, dataUltima.minusDays(30), dataUltima, precoAtual, precoAtual, precoAtual, precoAtual, precoAtual, var30, List.of()
        );
        ResumoPeriodoMercado resumo90 = new ResumoPeriodoMercado(
                90, dataUltima.minusDays(90), dataUltima, precoAtual, precoAtual, precoAtual, precoAtual, precoAtual, var90, List.of()
        );
        ResumoPeriodoMercado resumo180 = new ResumoPeriodoMercado(
                180, dataUltima.minusDays(180), dataUltima, precoAtual, precoAtual, precoAtual, precoAtual, precoAtual, var180, List.of()
        );

        return new ProdutoMercadoAnalise(
                produtoId,
                nomeComercial,
                "especificacao",
                "Saca",
                new BigDecimal("60.00"),
                "DIARIA",
                dataUltima,
                precoAtual,
                var30,
                var90,
                var180,
                origem,
                resumo30,
                resumo90,
                resumo180
        );
    }
}
