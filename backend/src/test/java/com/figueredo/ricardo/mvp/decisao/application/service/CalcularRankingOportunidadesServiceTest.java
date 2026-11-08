package com.figueredo.ricardo.mvp.decisao.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.MunicipioNaoEncontradoException;
import com.figueredo.ricardo.mvp.decisao.application.port.out.DecisaoHistoricoPamPort;
import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.mercado.application.port.in.ConsultarMercadoCulturasUseCase;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.OrigemCotacao;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.mercado.domain.model.ResumoPeriodoMercado;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalcularRankingOportunidadesServiceTest {

    @Mock
    private ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;

    @Mock
    private ConsultarMercadoCulturasUseCase consultarMercadoUseCase;

    @Mock
    private DecisaoHistoricoPamPort historicoPamPort;

    private CalcularRankingOportunidadesService service;

    @BeforeEach
    void setUp() {
        service = new CalcularRankingOportunidadesService(
                consultarZoneamentoUseCase,
                consultarMercadoUseCase,
                historicoPamPort
        );
    }

    @Test
    void deveCalcularRankingOrdenadoPorNotaFinalDecrescenteComPosicoesCorretas() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio(
                "Curitibanos",
                "SC",
                codigoIbge,
                7L,
                "Praça regional atribuída: Meio Oeste"
        );

        CulturaZoneamentoConsolidado sojaZarc = new CulturaZoneamentoConsolidado(
                1L,
                "Soja",
                "Glycine max",
                "GRAOS",
                List.of("Soja"),
                20,
                Set.of(20, 30),
                List.of(
                        PeriodoDecendio.deDecendio(28, Set.of(20)),
                        PeriodoDecendio.deDecendio(29, Set.of(20)),
                        PeriodoDecendio.deDecendio(30, Set.of(20)),
                        PeriodoDecendio.deDecendio(31, Set.of(20)),
                        PeriodoDecendio.deDecendio(32, Set.of(20)),
                        PeriodoDecendio.deDecendio(33, Set.of(20)),
                        PeriodoDecendio.deDecendio(34, Set.of(20))
                ),
                List.of(new SoloZoneamento("2", "Tipo 2"), new SoloZoneamento("3", "Tipo 3")),
                List.of(new CicloZoneamento("1", "Grupo I"), new CicloZoneamento("2", "Grupo II")),
                List.of("Portaria Soja")
        );

        CulturaZoneamentoConsolidado feijaoZarc = new CulturaZoneamentoConsolidado(
                3L,
                "Feijão",
                "Phaseolus vulgaris",
                "GRAOS",
                List.of("Feijão"),
                30,
                Set.of(30, 40),
                List.of(
                        PeriodoDecendio.deDecendio(25, Set.of(30)),
                        PeriodoDecendio.deDecendio(26, Set.of(30)),
                        PeriodoDecendio.deDecendio(27, Set.of(30))
                ),
                List.of(new SoloZoneamento("2", "Tipo 2")),
                List.of(new CicloZoneamento("1", "Grupo I")),
                List.of("Portaria Feijão")
        );

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioESafra(codigoIbge, 2025, 2026))
                .thenReturn(new ZoneamentoMunicipioResultado(
                        curitibanos,
                        2025,
                        2026,
                        List.of(feijaoZarc, sojaZarc)
                ));

        LocalDate dataCotacao = LocalDate.of(2026, 10, 30);

        ProdutoMercadoAnalise sojaProduto = criarProdutoAnalise(
                10L,
                "Soja industrial",
                new BigDecimal("125.00"),
                new BigDecimal("4.20"),
                new BigDecimal("8.50"),
                new BigDecimal("11.00"),
                dataCotacao,
                false,
                "Meio Oeste"
        );

        ProdutoMercadoAnalise feijaoProduto = criarProdutoAnalise(
                30L,
                "Feijão preto",
                new BigDecimal("180.00"),
                new BigDecimal("-2.50"),
                new BigDecimal("-4.00"),
                new BigDecimal("-6.00"),
                dataCotacao,
                false,
                "Meio Oeste"
        );

        when(consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(3L, 1L)))
                .thenReturn(List.of(
                        new CulturaMercadoResultado(1L, "Soja", List.of(sojaProduto)),
                        new CulturaMercadoResultado(3L, "Feijão", List.of(feijaoProduto))
                ));

        when(historicoPamPort.buscarUltimoHistoricoPamPorMunicipioECulturas(codigoIbge, List.of(3L, 1L)))
                .thenReturn(Map.of(
                        1L, new HistoricoPamCultura(1L, true, 2025, 15000, 57000, 3800),
                        3L, new HistoricoPamCultura(3L, true, 2025, 2000, 3600, 1800)
                ));

        RankingOportunidadesResultado ranking = service.calcularRankingPorMunicipioESafra(codigoIbge, 2025, 2026);

        assertEquals("Curitibanos", ranking.nomeMunicipio());
        assertEquals("Meio Oeste", ranking.pracaRegionalMunicipio());
        assertEquals(2025, ranking.safraIni());
        assertEquals(2026, ranking.safraFim());
        assertEquals(2, ranking.oportunidades().size());

        ScoreOportunidade primeiro = ranking.oportunidades().get(0);
        ScoreOportunidade segundo = ranking.oportunidades().get(1);

        assertEquals(1, primeiro.posicaoRanking());
        assertEquals("Soja", primeiro.nomeCultura());
        assertEquals(2, segundo.posicaoRanking());
        assertEquals("Feijão", segundo.nomeCultura());
        assertTrue(primeiro.notaFinal() > segundo.notaFinal());
        assertEquals("Soja", ranking.melhorOportunidade().nomeCultura());
    }

    @Test
    void deveCalcularRankingComPeriodoESoloObrigatoriosERecomendarMelhorPeriodoPorCultura() {
        String codigoIbge = "4204806";
        PeriodoPlantioMeses periodo = new PeriodoPlantioMeses(YearMonth.of(2025, 1), YearMonth.of(2025, 2));
        Municipio curitibanos = new Municipio(
                "Curitibanos",
                "SC",
                codigoIbge,
                7L,
                "Praça regional atribuída: Meio Oeste"
        );

        CulturaZoneamentoConsolidado sojaZarc = new CulturaZoneamentoConsolidado(
                1L,
                "Soja",
                "Glycine max",
                "GRAOS",
                List.of("Soja"),
                20,
                Set.of(20, 30),
                List.of(
                        PeriodoDecendio.deDecendio(1, Set.of(20)),
                        PeriodoDecendio.deDecendio(2, Set.of(30))
                ),
                List.of(SoloZoneamento.deTipoSoloZarc(TipoSoloZarc.SOLO_TEXTURA_MEDIA)),
                List.of(new CicloZoneamento("1", "Grupo I")),
                List.of("Portaria Soja"),
                PeriodoDecendio.deDecendio(1, Set.of(20)),
                "De 01 a 10 de Janeiro (risco de 20%) para Solo de textura média"
        );

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioPeriodoESolo(
                codigoIbge,
                periodo,
                TipoSoloZarc.SOLO_TEXTURA_MEDIA
        )).thenReturn(new ZoneamentoMunicipioResultado(
                curitibanos,
                2025,
                2026,
                List.of(sojaZarc)
        ));

        LocalDate dataCotacao = LocalDate.of(2026, 10, 30);
        ProdutoMercadoAnalise sojaProduto = criarProdutoAnalise(
                10L,
                "Soja industrial",
                new BigDecimal("125.00"),
                new BigDecimal("4.20"),
                new BigDecimal("8.50"),
                new BigDecimal("11.00"),
                dataCotacao,
                false,
                "Meio Oeste"
        );

        when(consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(1L)))
                .thenReturn(List.of(new CulturaMercadoResultado(1L, "Soja", List.of(sojaProduto))));
        when(historicoPamPort.buscarUltimoHistoricoPamPorMunicipioECulturas(codigoIbge, List.of(1L)))
                .thenReturn(Map.of(1L, new HistoricoPamCultura(1L, true, 2025, 15000, 57000, 3800)));

        RankingOportunidadesResultado ranking = service.calcularRankingPorMunicipioPeriodoESolo(
                codigoIbge,
                periodo,
                TipoSoloZarc.SOLO_TEXTURA_MEDIA
        );

        assertEquals(1, ranking.oportunidades().size());
        assertEquals("Soja", ranking.melhorOportunidade().nomeCultura());
        assertEquals(
                "De 01 a 10 de Janeiro (risco de 20%) para Solo de textura média",
                ranking.melhorOportunidade().recomendacaoMelhorPeriodo()
        );
    }

    @Test
    void deveIncluirCulturasComDecendioNoPeriodoConsultadoECalcularPontuacaoNoRanking() {
        String codigoIbge = "4204806";
        PeriodoPlantioMeses periodo = new PeriodoPlantioMeses(YearMonth.of(2025, 11), YearMonth.of(2026, 2));
        Municipio curitibanos = new Municipio(
                "Curitibanos",
                "SC",
                codigoIbge,
                7L,
                "Praça regional atribuída: Meio Oeste"
        );

        CulturaZoneamentoConsolidado sojaComDecendioNoPeriodo = new CulturaZoneamentoConsolidado(
                1L,
                "Soja",
                "Glycine max",
                "GRAOS",
                List.of("Soja"),
                20,
                Set.of(20),
                List.of(
                        PeriodoDecendio.deDecendio(34, Set.of(20)),
                        PeriodoDecendio.deDecendio(1, Set.of(20))
                ),
                List.of(new SoloZoneamento("2", "Tipo 2")),
                List.of(new CicloZoneamento("1", "Grupo I")),
                List.of("Portaria Soja")
        );

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(codigoIbge, periodo))
                .thenReturn(new ZoneamentoMunicipioResultado(
                        curitibanos,
                        2025,
                        2026,
                        List.of(sojaComDecendioNoPeriodo)
                ));

        LocalDate dataCotacao = LocalDate.of(2026, 10, 30);
        ProdutoMercadoAnalise sojaProduto = criarProdutoAnalise(
                10L,
                "Soja industrial",
                new BigDecimal("125.00"),
                new BigDecimal("4.20"),
                new BigDecimal("8.50"),
                new BigDecimal("11.00"),
                dataCotacao,
                false,
                "Meio Oeste"
        );

        when(consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(1L)))
                .thenReturn(List.of(new CulturaMercadoResultado(1L, "Soja", List.of(sojaProduto))));
        when(historicoPamPort.buscarUltimoHistoricoPamPorMunicipioECulturas(codigoIbge, List.of(1L)))
                .thenReturn(Map.of(1L, new HistoricoPamCultura(1L, true, 2025, 15000, 57000, 3800)));

        RankingOportunidadesResultado ranking = service.calcularRankingPorMunicipioEPeriodo(codigoIbge, periodo);

        assertEquals(1, ranking.oportunidades().size());
        ScoreOportunidade oportunidade = ranking.oportunidades().get(0);
        assertEquals(1, oportunidade.posicaoRanking());
        assertEquals("Soja", oportunidade.nomeCultura());
        assertEquals(2, oportunidade.quantidadeDecendiosFavoraveis());
        assertTrue(oportunidade.notaFinal() > 0);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHouverCulturasAptasNoZarc() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioESafra(codigoIbge, 2025, 2026))
                .thenReturn(new ZoneamentoMunicipioResultado(curitibanos, 2025, 2026, List.of()));

        RankingOportunidadesResultado resultado = service.calcularRankingPorMunicipioESafra(codigoIbge, 2025, 2026);

        assertTrue(resultado.oportunidades().isEmpty());
        assertEquals(1, resultado.avisosGerais().size());
        verifyNoInteractions(consultarMercadoUseCase, historicoPamPort);
    }

    @Test
    void devePropagarMunicipioNaoEncontradoExceptionQuandoMunicipioNaoExistir() {
        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioESafra("4299999", 2025, 2026))
                .thenThrow(new MunicipioNaoEncontradoException("4299999"));

        assertThrows(
                MunicipioNaoEncontradoException.class,
                () -> service.calcularRankingPorMunicipioESafra("4299999", 2025, 2026)
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
                "Observação"
        );

        ResumoPeriodoMercado r30 = new ResumoPeriodoMercado(
                30, dataUltima.minusDays(30), dataUltima, precoAtual, precoAtual, precoAtual, precoAtual, precoAtual, var30, List.of()
        );
        ResumoPeriodoMercado r90 = new ResumoPeriodoMercado(
                90, dataUltima.minusDays(90), dataUltima, precoAtual, precoAtual, precoAtual, precoAtual, precoAtual, var90, List.of()
        );
        ResumoPeriodoMercado r180 = new ResumoPeriodoMercado(
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
                r30,
                r90,
                r180
        );
    }
}
