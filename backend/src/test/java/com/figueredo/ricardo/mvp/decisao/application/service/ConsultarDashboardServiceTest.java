package com.figueredo.ricardo.mvp.decisao.application.service;

import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarDashboardUseCase.FiltroDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarDashboardServiceTest {

    @Mock
    private ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;

    @Mock
    private CalcularRankingOportunidadesUseCase calcularRankingUseCase;

    private ConsultarDashboardService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarDashboardService(consultarZoneamentoUseCase, calcularRankingUseCase);
    }

    @Test
    void deveMontarDashboardComMunicipioCulturasPossiveisRankingEIndicadoresRisco() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Praça Meio Oeste");
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
                List.of(
                        PeriodoDecendio.deDecendio(28, Set.of(20)),
                        PeriodoDecendio.deDecendio(29, Set.of(20, 30))
                ),
                List.of(new SoloZoneamento("2", "Tipo 2")),
                List.of(new CicloZoneamento("1", "Grupo I")),
                List.of("Portaria 1")
        );

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(codigoIbge, periodo))
                .thenReturn(new ZoneamentoMunicipioResultado(curitibanos, 2025, 2026, List.of(sojaZarc)));

        ScoreOportunidade scoreSoja = new ScoreOportunidade(
                1L,
                "Soja",
                "Glycine max",
                "GRAOS",
                1,
                84,
                new PontuacaoCriterios(31, 8, 30, 15),
                20,
                2,
                1,
                1,
                sojaZarc.melhorPeriodoPlantio(),
                sojaZarc.recomendacaoMelhorPeriodo(),
                sojaZarc.soloProjecao(),
                sojaZarc.tiposSolo(),
                "Soja industrial",
                new BigDecimal("125.00"),
                "Saca",
                LocalDate.of(2026, 10, 30),
                new BigDecimal("4.20"),
                new BigDecimal("8.50"),
                new BigDecimal("11.00"),
                "Meio Oeste",
                "EPAGRI_CEPA",
                false,
                true,
                2025,
                3800,
                List.of("Justificativa"),
                List.of("Aviso")
        );

        when(calcularRankingUseCase.calcularRankingPorMunicipioEPeriodo(codigoIbge, periodo))
                .thenReturn(new RankingOportunidadesResultado(
                        codigoIbge,
                        "Curitibanos",
                        "SC",
                        "Meio Oeste",
                        2025,
                        2026,
                        List.of(scoreSoja),
                        List.of("Aviso geral")
                ));

        DashboardResultado resultado = service.consultarDashboard(
                new FiltroDashboard(codigoIbge, periodo, tipoSolo, new BigDecimal("50.00"))
        );

        assertEquals("Curitibanos", resultado.municipio().nome());
        assertEquals("Meio Oeste", resultado.municipio().pracaReferenciaNome());
        assertEquals("2025-10", resultado.mesInicio());
        assertEquals("2026-02", resultado.mesFim());
        assertEquals(TipoSoloZarc.SOLO_TEXTURA_MEDIA, resultado.soloSelecionado());
        assertEquals("2025/2026", resultado.safra());
        assertEquals(new BigDecimal("50.00"), resultado.areaHa());
        assertNotNull(resultado.melhorOportunidade());
        assertEquals("Soja", resultado.melhorOportunidade().nomeCultura());
        assertEquals(1, resultado.culturasPossiveis().size());
        assertEquals("01 a 10 de Outubro a 11 a 20 de Outubro (2 decêndios)", resultado.culturasPossiveis().get(0).epocaPlantioResumo());
        assertEquals(
                "O melhor período de plantio é de 01 a 10 de Outubro (risco de 20%) para Solo de textura média",
                resultado.culturasPossiveis().get(0).recomendacaoMelhorPeriodo()
        );
        assertEquals(1, resultado.rankingOportunidades().size());
        assertEquals(84, resultado.rankingOportunidades().get(0).notaFinal());
        assertEquals(1, resultado.indicadoresRisco().size());
        assertEquals("BAIXO (20%)", resultado.indicadoresRisco().get(0).classificacaoRisco());
        assertEquals(2, resultado.indicadoresRisco().get(0).decendiosComRisco20());
        assertEquals(1, resultado.indicadoresRisco().get(0).decendiosComRisco30());
    }
}
