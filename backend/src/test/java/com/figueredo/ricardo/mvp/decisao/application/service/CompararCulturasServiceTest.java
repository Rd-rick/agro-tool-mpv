package com.figueredo.ricardo.mvp.decisao.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.CulturaNaoEncontradaException;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase.FiltroCompararCulturas;
import com.figueredo.ricardo.mvp.decisao.application.port.out.DecisaoHistoricoPamPort;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.mercado.application.port.in.ConsultarMercadoCulturasUseCase;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompararCulturasServiceTest {

    @Mock
    private ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;

    @Mock
    private ConsultarMercadoCulturasUseCase consultarMercadoUseCase;

    @Mock
    private CalcularRankingOportunidadesUseCase calcularRankingUseCase;

    @Mock
    private DecisaoHistoricoPamPort historicoPamPort;

    private CompararCulturasService service;

    @BeforeEach
    void setUp() {
        service = new CompararCulturasService(
                consultarZoneamentoUseCase,
                consultarMercadoUseCase,
                calcularRankingUseCase,
                historicoPamPort
        );
    }

    @Test
    void deveCompararCulturasLadoALadoIncluindoCulturaSemJanelaNoZarc() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");
        PeriodoPlantioMeses periodo = new PeriodoPlantioMeses(YearMonth.of(2025, 10), YearMonth.of(2026, 2));
        TipoSoloZarc tipoSolo = TipoSoloZarc.SOLO_TEXTURA_MEDIA;

        CulturaZoneamentoConsolidado sojaZarc = new CulturaZoneamentoConsolidado(
                1L, "Soja", "Glycine max", "GRAOS", List.of("Soja"), 20, Set.of(20),
                List.of(PeriodoDecendio.deDecendio(28, Set.of(20))),
                List.of(new SoloZoneamento("2", "Tipo 2")),
                List.of(new CicloZoneamento("1", "Grupo I")),
                List.of("Portaria Soja")
        );

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(codigoIbge, periodo))
                .thenReturn(new ZoneamentoMunicipioResultado(curitibanos, 2025, 2026, List.of(sojaZarc)));

        when(consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(1L, 6L)))
                .thenReturn(List.of(
                        new CulturaMercadoResultado(1L, "Soja", List.of()),
                        new CulturaMercadoResultado(6L, "Cebola", List.of())
                ));

        ScoreOportunidade scoreSoja = new ScoreOportunidade(
                1L, "Soja", "Glycine max", "GRAOS", 1, 82,
                new PontuacaoCriterios(31, 8, 28, 15), 20, 1, 1, 1,
                sojaZarc.melhorPeriodoPlantio(), sojaZarc.recomendacaoMelhorPeriodo(),
                sojaZarc.soloProjecao(), sojaZarc.tiposSolo(),
                "Soja industrial", new BigDecimal("125.00"), "Saca", LocalDate.of(2026, 10, 30),
                new BigDecimal("4.20"), new BigDecimal("8.00"), new BigDecimal("10.00"),
                "Meio Oeste", "EPAGRI_CEPA", false, true, 2025, 3800,
                List.of("Justificativa Soja"), List.of("Aviso")
        );

        when(calcularRankingUseCase.calcularRankingPorMunicipioEPeriodo(codigoIbge, periodo))
                .thenReturn(new RankingOportunidadesResultado(
                        codigoIbge, "Curitibanos", "SC", "Meio Oeste", 2025, 2026, List.of(scoreSoja), List.of()
                ));

        when(historicoPamPort.buscarSerieHistoricaPamPorMunicipioECulturas(codigoIbge, List.of(1L, 6L)))
                .thenReturn(Map.of(
                        1L, List.of(new HistoricoPamCultura(1L, true, 2025, 18000, 68400, 3800)),
                        6L, List.of()
                ));

        ComparacaoCulturasResultado resultado = service.compararCulturas(codigoIbge, periodo, tipoSolo, List.of(1L, 6L));

        assertEquals(2, resultado.culturasComparadas().size());
        assertEquals(TipoSoloZarc.SOLO_TEXTURA_MEDIA, resultado.soloSelecionado());
        assertEquals("Soja", resultado.culturasComparadas().get(0).nomeCultura());
        assertTrue(resultado.culturasComparadas().get(0).viabilidadeZoneamento().aptaZarc());
        assertEquals(
                "O melhor período de plantio é de 01 a 10 de Outubro (risco de 20%) para Solo de textura média",
                resultado.culturasComparadas().get(0).janelasPlantio().recomendacaoMelhorPeriodo()
        );
        assertEquals(82, resultado.culturasComparadas().get(0).scoreMotorDecisao().notaFinal());

        assertEquals("Cebola", resultado.culturasComparadas().get(1).nomeCultura());
        assertFalse(resultado.culturasComparadas().get(1).viabilidadeZoneamento().aptaZarc());
        assertFalse(resultado.culturasComparadas().get(1).scoreMotorDecisao().elegivelRanking());
    }

    @Test
    void deveValidarQuantidadeDeCulturasEIdentificadoresDuplicadosNoFiltro() {
        PeriodoPlantioMeses periodo = new PeriodoPlantioMeses(YearMonth.of(2025, 10), YearMonth.of(2026, 2));
        TipoSoloZarc tipoSolo = TipoSoloZarc.SOLO_TEXTURA_MEDIA;
        assertThrows(
                IllegalArgumentException.class,
                () -> new FiltroCompararCulturas("4204806", periodo, tipoSolo, List.of(1L))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new FiltroCompararCulturas("4204806", periodo, tipoSolo, List.of(1L, 1L))
        );
    }

    @Test
    void deveLancarCulturaNaoEncontradaExceptionQuandoAlgumaCulturaNaoExistir() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");
        PeriodoPlantioMeses periodo = new PeriodoPlantioMeses(YearMonth.of(2025, 10), YearMonth.of(2026, 2));
        TipoSoloZarc tipoSolo = TipoSoloZarc.SOLO_TEXTURA_MEDIA;

        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(codigoIbge, periodo))
                .thenReturn(new ZoneamentoMunicipioResultado(curitibanos, 2025, 2026, List.of()));
        when(consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(1L, 999L)))
                .thenReturn(List.of(new CulturaMercadoResultado(1L, "Soja", List.of())));

        assertThrows(
                CulturaNaoEncontradaException.class,
                () -> service.compararCulturas(codigoIbge, periodo, tipoSolo, List.of(1L, 999L))
        );
    }
}
