package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarDashboardUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarDashboardUseCase.FiltroDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.CulturaPossivelDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.IndicadorRiscoCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.MunicipioDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.ConsultarDashboardRequest;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper.DashboardWebMapper;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private ConsultarDashboardUseCase consultarDashboardUseCase;

    private DashboardController controller;

    @BeforeEach
    void setUp() {
        controller = new DashboardController(consultarDashboardUseCase, new DashboardWebMapper());
    }

    @Test
    void deveRetornarDashboardCompletoParaMunicipioPeriodoSoloEArea() {
        String codigoIbge = "4204806";
        BigDecimal areaHa = new BigDecimal("120.50");
        YearMonth mesInicio = YearMonth.now();
        YearMonth mesFim = mesInicio.plusMonths(3);
        ConsultarDashboardRequest request = new ConsultarDashboardRequest(
                codigoIbge,
                mesInicio.toString(),
                mesFim.toString(),
                "2",
                areaHa
        );
        FiltroDashboard filtro = new FiltroDashboard(
                codigoIbge,
                new PeriodoPlantioMeses(mesInicio, mesFim),
                TipoSoloZarc.SOLO_TEXTURA_MEDIA,
                areaHa
        );

        MunicipioDashboard municipio = new MunicipioDashboard(
                codigoIbge, "Curitibanos", "SC", 7L, "Meio Oeste", "Praça Meio Oeste"
        );
        PeriodoDecendio melhorDecendio = PeriodoDecendio.deDecendio(28, Set.of(20));
        String recomendacao = melhorDecendio.formatarRecomendacaoSolo(TipoSoloZarc.SOLO_TEXTURA_MEDIA.getTipoSolo());
        CulturaPossivelDashboard culturaPossivel = new CulturaPossivelDashboard(
                1L, "Soja", "Glycine max", "GRAOS", "Apta conforme condições do ZARC",
                "01 a 10 de Outubro (1 decêndio)",
                melhorDecendio,
                recomendacao,
                20, Set.of(20),
                List.of(melhorDecendio),
                List.of(new SoloZoneamento("2", "Tipo 2")),
                List.of(new CicloZoneamento("1", "Grupo I"))
        );
        ScoreOportunidade score = new ScoreOportunidade(
                1L, "Soja", "Glycine max", "GRAOS", 1, 80,
                new PontuacaoCriterios(30, 8, 17, 10, 15), 20, 1, 1, 1,
                melhorDecendio, recomendacao,
                "Soja industrial", new BigDecimal("125.00"), "Saca",
                LocalDate.of(2026, 10, 30), new BigDecimal("4.20"), new BigDecimal("6.10"), new BigDecimal("9.00"),
                "Meio Oeste", "EPAGRI_CEPA", false, true, 2025, 3800,
                List.of("Justificativa"), List.of("Aviso")
        );
        IndicadorRiscoCultura indicador = new IndicadorRiscoCultura(
                1L, "Soja", 20, "BAIXO (20%)", Set.of(20), 1, 1, 0, 0
        );

        when(consultarDashboardUseCase.consultarDashboard(filtro))
                .thenReturn(new DashboardResultado(
                        municipio,
                        mesInicio.toString(),
                        mesFim.toString(),
                        TipoSoloZarc.SOLO_TEXTURA_MEDIA,
                        2025,
                        2026,
                        "2025/2026",
                        areaHa,
                        score,
                        List.of(culturaPossivel),
                        List.of(score),
                        List.of(indicador),
                        List.of("Aviso geral")
                ));

        ResponseEntity<DashboardResponse> response = controller.consultarDashboard(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Curitibanos", response.getBody().municipio().nome());
        assertEquals(mesInicio.toString(), response.getBody().mesInicio());
        assertEquals(mesFim.toString(), response.getBody().mesFim());
        assertEquals("2", response.getBody().soloSelecionado().codigo());
        assertEquals("Solo de textura média", response.getBody().soloSelecionado().tipoSolo());
        assertEquals("2025/2026", response.getBody().safra());
        assertEquals(areaHa, response.getBody().areaHa());
        assertNotNull(response.getBody().melhorOportunidade());
        assertEquals("Soja", response.getBody().melhorOportunidade().nomeCultura());
        assertEquals(1, response.getBody().culturasPossiveis().size());
        assertEquals(recomendacao, response.getBody().culturasPossiveis().get(0).recomendacaoMelhorPeriodo());
        assertEquals(1, response.getBody().rankingOportunidades().size());
        assertEquals(80, response.getBody().rankingOportunidades().get(0).notaFinal());
        assertEquals(1, response.getBody().indicadoresRisco().size());
        verify(consultarDashboardUseCase).consultarDashboard(filtro);
    }

    @Test
    void deveRetornarDashboardQuandoSoloNaoForInformadoExibindoTodosOsSolosESoloProjecao() {
        String codigoIbge = "4204806";
        YearMonth mesInicio = YearMonth.now();
        YearMonth mesFim = mesInicio.plusMonths(3);
        BigDecimal areaHa = new BigDecimal("10.00");
        ConsultarDashboardRequest request = new ConsultarDashboardRequest(
                codigoIbge,
                mesInicio.toString(),
                mesFim.toString(),
                null,
                areaHa
        );
        FiltroDashboard filtro = new FiltroDashboard(
                codigoIbge,
                new PeriodoPlantioMeses(mesInicio, mesFim),
                null,
                areaHa
        );

        MunicipioDashboard municipio = new MunicipioDashboard(
                codigoIbge, "Curitibanos", "SC", 7L, "Meio Oeste", "Praça Meio Oeste"
        );
        PeriodoDecendio melhorDecendio = PeriodoDecendio.deDecendio(28, Set.of(20));
        SoloZoneamento solo2 = new SoloZoneamento("2", "Tipo 2");
        SoloZoneamento solo3 = new SoloZoneamento("3", "Tipo 3");
        String recomendacao = melhorDecendio.formatarRecomendacaoSolo(solo2.tipoSolo());
        CulturaPossivelDashboard culturaPossivel = new CulturaPossivelDashboard(
                1L, "Soja", "Glycine max", "GRAOS", "Apta conforme condições do ZARC",
                "01 a 10 de Outubro (1 decêndio)",
                melhorDecendio,
                recomendacao,
                solo2,
                20, Set.of(20),
                List.of(melhorDecendio),
                List.of(solo2, solo3),
                List.of(new CicloZoneamento("1", "Grupo I"))
        );
        ScoreOportunidade score = new ScoreOportunidade(
                1L, "Soja", "Glycine max", "GRAOS", 1, 80,
                new PontuacaoCriterios(30, 8, 17, 10, 15), 20, 1, 2, 1,
                melhorDecendio, recomendacao, solo2, List.of(solo2, solo3),
                "Soja industrial", new BigDecimal("125.00"), "Saca",
                LocalDate.of(2026, 10, 30), new BigDecimal("4.20"), new BigDecimal("6.10"), new BigDecimal("9.00"),
                "Meio Oeste", "EPAGRI_CEPA", false, true, 2025, 3800,
                List.of("Justificativa"), List.of("Aviso")
        );
        IndicadorRiscoCultura indicador = new IndicadorRiscoCultura(
                1L, "Soja", 20, "BAIXO (20%)", Set.of(20), 1, 1, 0, 0
        );

        when(consultarDashboardUseCase.consultarDashboard(filtro))
                .thenReturn(new DashboardResultado(
                        municipio,
                        mesInicio.toString(),
                        mesFim.toString(),
                        null,
                        2025,
                        2026,
                        "2025/2026",
                        areaHa,
                        score,
                        List.of(culturaPossivel),
                        List.of(score),
                        List.of(indicador),
                        List.of("Aviso geral")
                ));

        ResponseEntity<DashboardResponse> response = controller.consultarDashboard(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Curitibanos", response.getBody().municipio().nome());
        org.junit.jupiter.api.Assertions.assertNull(response.getBody().soloSelecionado());
        assertEquals(2, response.getBody().culturasPossiveis().get(0).solos().size());
        assertNotNull(response.getBody().culturasPossiveis().get(0).soloProjecao());
        assertEquals("2", response.getBody().culturasPossiveis().get(0).soloProjecao().codigo());
        assertNotNull(response.getBody().rankingOportunidades().get(0).soloProjecao());
        assertEquals("2", response.getBody().rankingOportunidades().get(0).soloProjecao().codigo());
        assertEquals(2, response.getBody().rankingOportunidades().get(0).solosAptos().size());
        verify(consultarDashboardUseCase).consultarDashboard(filtro);
    }
}
