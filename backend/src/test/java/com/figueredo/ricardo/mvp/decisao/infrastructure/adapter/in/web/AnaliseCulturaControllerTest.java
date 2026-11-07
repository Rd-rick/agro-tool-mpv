package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase.FiltroAnaliseCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.AnaliseSecao;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.MercadoSecao;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.ProdutividadeMunicipalAnalise;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.ZoneamentoSecao;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.ConsultarAnaliseCulturaRequest;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper.AnaliseCulturaWebMapper;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
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
class AnaliseCulturaControllerTest {

    @Mock
    private ConsultarAnaliseCulturaUseCase consultarAnaliseCulturaUseCase;

    private AnaliseCulturaController controller;

    @BeforeEach
    void setUp() {
        controller = new AnaliseCulturaController(consultarAnaliseCulturaUseCase, new AnaliseCulturaWebMapper());
    }

    @Test
    void deveRetornarAnaliseDaCulturaComStatusOk() {
        String codigoIbge = "4204806";
        YearMonth mesInicio = YearMonth.now();
        YearMonth mesFim = mesInicio.plusMonths(3);
        ConsultarAnaliseCulturaRequest request = new ConsultarAnaliseCulturaRequest(
                codigoIbge,
                1L,
                mesInicio.toString(),
                mesFim.toString(),
                "2"
        );
        FiltroAnaliseCultura filtro = new FiltroAnaliseCultura(
                codigoIbge,
                1L,
                new PeriodoPlantioMeses(mesInicio, mesFim),
                TipoSoloZarc.SOLO_TEXTURA_MEDIA
        );

        Municipio municipio = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");
        PeriodoDecendio melhorDecendio = PeriodoDecendio.deDecendio(28, Set.of(20));
        String recomendacao = melhorDecendio.formatarRecomendacaoSolo(TipoSoloZarc.SOLO_TEXTURA_MEDIA.getTipoSolo());
        ZoneamentoSecao zoneamento = new ZoneamentoSecao(
                municipio,
                2025,
                2026,
                "2025/2026",
                TipoSoloZarc.SOLO_TEXTURA_MEDIA,
                true,
                "Apta conforme condições do ZARC",
                melhorDecendio,
                recomendacao,
                20,
                Set.of(20),
                List.of("Soja"),
                List.of(),
                List.of(),
                List.of("Portaria 1"),
                List.of(melhorDecendio),
                List.of(),
                List.of()
        );
        MercadoSecao mercado = new MercadoSecao(
                "Soja industrial", new BigDecimal("125.00"), "Saca", LocalDate.of(2026, 10, 30),
                "Meio Oeste", "EPAGRI_CEPA", false, "VALORIZACAO",
                new BigDecimal("4.20"), new BigDecimal("8.00"), new BigDecimal("12.00"),
                List.of(), List.of(), List.of()
        );
        AnaliseSecao analise = new AnaliseSecao(
                null, "Resumo de comportamento", List.of(), List.of(),
                new ProdutividadeMunicipalAnalise(true, 2025, 3800, 3700, 18000, 68400, List.of(), "Aviso custos"),
                List.of("Aviso metodológico")
        );

        when(consultarAnaliseCulturaUseCase.consultarAnaliseCultura(filtro))
                .thenReturn(new AnaliseCulturaResultado(1L, "Soja", "Glycine max", "GRAOS", zoneamento, mercado, analise));

        ResponseEntity<AnaliseCulturaResponse> response = controller.consultarAnaliseCultura(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Soja", response.getBody().nomeCultura());
        assertEquals("Curitibanos", response.getBody().zoneamento().municipio().nome());
        assertEquals("2025/2026", response.getBody().zoneamento().safra());
        assertEquals("2", response.getBody().zoneamento().soloSelecionado().codigo());
        assertEquals(recomendacao, response.getBody().zoneamento().recomendacaoMelhorPeriodo());
        assertEquals("VALORIZACAO", response.getBody().mercado().tendencia());
        assertEquals(3700, response.getBody().analise().produtividadeMunicipal().rendimentoMedioHistoricoKgHa());
        verify(consultarAnaliseCulturaUseCase).consultarAnaliseCultura(filtro);
    }

    @Test
    void deveRetornarAnaliseDaCulturaSemSoloInformadoComStatusOk() {
        String codigoIbge = "4204806";
        YearMonth mesInicio = YearMonth.now();
        YearMonth mesFim = mesInicio.plusMonths(3);
        ConsultarAnaliseCulturaRequest request = new ConsultarAnaliseCulturaRequest(
                codigoIbge,
                1L,
                mesInicio.toString(),
                mesFim.toString(),
                null
        );
        FiltroAnaliseCultura filtro = new FiltroAnaliseCultura(
                codigoIbge,
                1L,
                new PeriodoPlantioMeses(mesInicio, mesFim),
                null
        );

        Municipio municipio = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");
        PeriodoDecendio melhorDecendio = PeriodoDecendio.deDecendio(28, Set.of(20));
        String recomendacao = melhorDecendio.formatarRecomendacaoSolo(TipoSoloZarc.SOLO_ARGILOSO.getTipoSolo());
        ZoneamentoSecao zoneamento = new ZoneamentoSecao(
                municipio,
                2025,
                2026,
                "2025/2026",
                null,
                true,
                "Apta conforme condições do ZARC",
                melhorDecendio,
                recomendacao,
                com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento.deTipoSoloZarc(TipoSoloZarc.SOLO_ARGILOSO),
                20,
                Set.of(20),
                List.of("Soja"),
                List.of(),
                List.of(com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento.deTipoSoloZarc(TipoSoloZarc.SOLO_ARGILOSO)),
                List.of("Portaria 1"),
                List.of(melhorDecendio),
                List.of(),
                List.of()
        );
        MercadoSecao mercado = new MercadoSecao(
                "Soja industrial", new BigDecimal("125.00"), "Saca", LocalDate.of(2026, 10, 30),
                "Meio Oeste", "EPAGRI_CEPA", false, "VALORIZACAO",
                new BigDecimal("4.20"), new BigDecimal("8.00"), new BigDecimal("12.00"),
                List.of(), List.of(), List.of()
        );
        AnaliseSecao analise = new AnaliseSecao(
                null, "Resumo de comportamento", List.of(), List.of(),
                new ProdutividadeMunicipalAnalise(true, 2025, 3800, 3700, 18000, 68400, List.of(), "Aviso custos"),
                List.of("Aviso metodológico")
        );

        when(consultarAnaliseCulturaUseCase.consultarAnaliseCultura(filtro))
                .thenReturn(new AnaliseCulturaResultado(1L, "Soja", "Glycine max", "GRAOS", zoneamento, mercado, analise));

        ResponseEntity<AnaliseCulturaResponse> response = controller.consultarAnaliseCultura(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Soja", response.getBody().nomeCultura());
        org.junit.jupiter.api.Assertions.assertNull(response.getBody().zoneamento().soloSelecionado());
        assertNotNull(response.getBody().zoneamento().soloProjecao());
        assertEquals("3", response.getBody().zoneamento().soloProjecao().codigo());
        verify(consultarAnaliseCulturaUseCase).consultarAnaliseCultura(filtro);
    }
}
