package com.figueredo.ricardo.mvp.decisao.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.CulturaNaoEncontradaException;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.out.DecisaoHistoricoPamPort;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.mercado.application.port.in.ConsultarMercadoCulturasUseCase;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.OrigemCotacao;
import com.figueredo.ricardo.mvp.mercado.domain.model.PontoSerieHistorica;
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
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarAnaliseCulturaServiceTest {

    @Mock
    private ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;

    @Mock
    private ConsultarMercadoCulturasUseCase consultarMercadoUseCase;

    @Mock
    private CalcularRankingOportunidadesUseCase calcularRankingUseCase;

    @Mock
    private DecisaoHistoricoPamPort historicoPamPort;

    private ConsultarAnaliseCulturaService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarAnaliseCulturaService(
                consultarZoneamentoUseCase,
                consultarMercadoUseCase,
                calcularRankingUseCase,
                historicoPamPort
        );
    }

    @Test
    void deveRetornarAnaliseCompletaCom3SecoesMatriz36DecendiosAlertasEProdutividade() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");
        PeriodoPlantioMeses periodo = new PeriodoPlantioMeses(YearMonth.of(2025, 10), YearMonth.of(2026, 2));
        TipoSoloZarc tipoSolo = TipoSoloZarc.SOLO_TEXTURA_MEDIA;

        CulturaZoneamentoConsolidado sojaZarc = new CulturaZoneamentoConsolidado(
                1L,
                "Soja",
                "Glycine max",
                "GRAOS",
                List.of("Soja"),
                20,
                Set.of(20, 30),
                List.of(PeriodoDecendio.deDecendio(28, Set.of(20))),
                List.of(new SoloZoneamento("2", "Tipo 2")),
                List.of(new CicloZoneamento("1", "Grupo I")),
                List.of("Portaria Soja")
        );

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(codigoIbge, periodo))
                .thenReturn(new ZoneamentoMunicipioResultado(curitibanos, 2025, 2026, List.of(sojaZarc)));

        List<Integer> decendios = new ArrayList<>(Collections.nCopies(36, 0));
        decendios.set(27, 20);
        ZarcZoneamentoItem itemDetalhado = new ZarcZoneamentoItem(
                1L, "Soja", "Glycine max", "GRAOS", "Soja", 2025, 2026,
                "1", "Grupo I", "2", "Tipo 2", "1", "Sequeiro", "Portaria Soja", 20, decendios
        );
        when(consultarZoneamentoUseCase.consultarItensAptosPorMunicipioSafraECultura(codigoIbge, 2025, 2026, 1L))
                .thenReturn(List.of(itemDetalhado));

        LocalDate dataCotacao = LocalDate.of(2026, 10, 30);
        List<PontoSerieHistorica> pontos = List.of(
                new PontoSerieHistorica(LocalDate.of(2026, 8, 15), new BigDecimal("110.00"), new BigDecimal("112.00"), new BigDecimal("114.00")),
                new PontoSerieHistorica(LocalDate.of(2026, 10, 30), new BigDecimal("124.00"), new BigDecimal("126.00"), new BigDecimal("128.00"))
        );

        ResumoPeriodoMercado r30 = new ResumoPeriodoMercado(
                30, dataCotacao.minusDays(30), dataCotacao,
                new BigDecimal("120.00"), new BigDecimal("126.00"),
                new BigDecimal("120.00"), new BigDecimal("126.00"), new BigDecimal("123.00"),
                new BigDecimal("5.00"), pontos
        );
        ResumoPeriodoMercado r90 = new ResumoPeriodoMercado(
                90, dataCotacao.minusDays(90), dataCotacao,
                new BigDecimal("112.00"), new BigDecimal("126.00"),
                new BigDecimal("110.00"), new BigDecimal("128.00"), new BigDecimal("119.00"),
                new BigDecimal("12.50"), pontos
        );
        ResumoPeriodoMercado r180 = new ResumoPeriodoMercado(
                180, dataCotacao.minusDays(180), dataCotacao,
                new BigDecimal("110.00"), new BigDecimal("126.00"),
                new BigDecimal("110.00"), new BigDecimal("128.00"), new BigDecimal("119.00"),
                new BigDecimal("14.55"), pontos
        );

        ProdutoMercadoAnalise prodEpagri = new ProdutoMercadoAnalise(
                10L, "Soja industrial", "industrial", "Saca", new BigDecimal("60.00"),
                "DIARIA", dataCotacao, new BigDecimal("126.00"),
                new BigDecimal("5.00"), new BigDecimal("12.50"), new BigDecimal("14.55"),
                new OrigemCotacao("EPAGRI_CEPA", 7L, "Meio Oeste", false, "Praça local"),
                r30, r90, r180
        );

        when(consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(1L)))
                .thenReturn(List.of(new CulturaMercadoResultado(1L, "Soja", List.of(prodEpagri))));

        ScoreOportunidade scoreSoja = new ScoreOportunidade(
                1L, "Soja", "Glycine max", "GRAOS", 1, 86,
                new PontuacaoCriterios(31, 10, 20, 10, 15), 20, 1, 1, 1,
                sojaZarc.melhorPeriodoPlantio(), sojaZarc.recomendacaoMelhorPeriodo(),
                "Soja industrial", new BigDecimal("126.00"), "Saca", dataCotacao,
                new BigDecimal("5.00"), new BigDecimal("12.50"), new BigDecimal("14.55"),
                "Meio Oeste", "EPAGRI_CEPA", false, true, 2025, 3800,
                List.of("Justificativa Soja"), List.of("Aviso Soja")
        );
        ScoreOportunidade scoreMilho = new ScoreOportunidade(
                2L, "Milho", "Zea mays", "GRAOS", 2, 74,
                new PontuacaoCriterios(29, 10, 15, 5, 15), 20, 4, 1, 1,
                "Milho amarelo", new BigDecimal("65.00"), "Saca", dataCotacao,
                new BigDecimal("2.10"), new BigDecimal("4.00"), new BigDecimal("5.00"),
                "Meio Oeste", "EPAGRI_CEPA", false, true, 2025, 7200,
                List.of("Justificativa Milho"), List.of("Aviso Milho")
        );

        when(calcularRankingUseCase.calcularRankingPorMunicipioEPeriodo(codigoIbge, periodo, BigDecimal.ONE))
                .thenReturn(new RankingOportunidadesResultado(
                        codigoIbge, "Curitibanos", "SC", "Meio Oeste", 2025, 2026,
                        List.of(scoreSoja, scoreMilho), List.of("Aviso")
                ));

        when(historicoPamPort.buscarSerieHistoricaPamPorMunicipioECulturas(codigoIbge, List.of(1L)))
                .thenReturn(Map.of(1L, List.of(
                        new HistoricoPamCultura(1L, true, 2025, 18000, 68400, 3800),
                        new HistoricoPamCultura(1L, true, 2024, 17000, 61200, 3600)
                )));

        AnaliseCulturaResultado resultado = service.consultarAnaliseCultura(codigoIbge, 1L, periodo, tipoSolo);

        assertEquals("Soja", resultado.nomeCultura());
        assertTrue(resultado.zoneamento().aptaZarc());
        assertEquals("2025/2026", resultado.zoneamento().safra());
        assertEquals(TipoSoloZarc.SOLO_TEXTURA_MEDIA, resultado.zoneamento().soloSelecionado());
        assertEquals(
                "De 01 a 10 de Outubro (risco de 20%) para Solo de textura média",
                resultado.zoneamento().recomendacaoMelhorPeriodo()
        );
        assertEquals(36, resultado.zoneamento().matriz36Decendios().size());
        assertTrue(resultado.zoneamento().matriz36Decendios().get(27).favoravel());
        assertFalse(resultado.zoneamento().matriz36Decendios().get(0).favoravel());
        assertNull(resultado.zoneamento().matriz36Decendios().get(0).menorRisco());

        assertEquals("VALORIZACAO", resultado.mercado().tendencia());
        assertEquals(1, resultado.mercado().cotacoesEpagriCepa().size());
        assertEquals(1, resultado.mercado().comparacaoDemaisCulturas().size());
        assertEquals("Milho", resultado.mercado().comparacaoDemaisCulturas().get(0).nomeCultura());

        assertEquals(2, resultado.analise().melhoresPeriodosComercializacao().size());
        assertEquals(10, resultado.analise().melhoresPeriodosComercializacao().get(0).mes());
        assertFalse(resultado.analise().alertasVariacoesRelevantes().isEmpty());
        assertTrue(resultado.analise().produtividadeMunicipal().possuiDadosPam());
        assertEquals(3700, resultado.analise().produtividadeMunicipal().rendimentoMedioHistoricoKgHa());
        assertTrue(resultado.analise().produtividadeMunicipal().avisoCustosProducao().contains("custos de produção"));
    }

    @Test
    void deveLancarCulturaNaoEncontradaExceptionQuandoCulturaNaoExistir() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");
        PeriodoPlantioMeses periodo = new PeriodoPlantioMeses(YearMonth.of(2025, 10), YearMonth.of(2026, 2));
        TipoSoloZarc tipoSolo = TipoSoloZarc.SOLO_TEXTURA_MEDIA;

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(codigoIbge, periodo))
                .thenReturn(new ZoneamentoMunicipioResultado(curitibanos, 2025, 2026, List.of()));
        when(consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(999L)))
                .thenReturn(List.of());

        assertThrows(
                CulturaNaoEncontradaException.class,
                () -> service.consultarAnaliseCultura(codigoIbge, 999L, periodo, tipoSolo)
        );
    }
}
