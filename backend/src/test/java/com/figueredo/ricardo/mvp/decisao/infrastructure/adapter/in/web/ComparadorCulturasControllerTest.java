package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase.FiltroCompararCulturas;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.IndicadoresProdutivosComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ItemComparacaoCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.JanelaPlantioComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.MercadoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.RiscoClimaticoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ScoreMotorDecisaoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ViabilidadeZoneamentoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasRequest;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper.ComparadorCulturasWebMapper;
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
class ComparadorCulturasControllerTest {

    @Mock
    private CompararCulturasUseCase compararCulturasUseCase;

    private ComparadorCulturasController controller;

    @BeforeEach
    void setUp() {
        controller = new ComparadorCulturasController(compararCulturasUseCase, new ComparadorCulturasWebMapper());
    }

    @Test
    void deveRetornarComparacaoDeCulturasComStatusOk() {
        String codigoIbge = "4204806";
        List<Long> culturaIds = List.of(1L, 2L);
        YearMonth mesInicio = YearMonth.now();
        YearMonth mesFim = mesInicio.plusMonths(3);
        CompararCulturasRequest request = new CompararCulturasRequest(
                codigoIbge,
                mesInicio.toString(),
                mesFim.toString(),
                "2",
                culturaIds
        );
        FiltroCompararCulturas filtro = new FiltroCompararCulturas(
                codigoIbge,
                new PeriodoPlantioMeses(mesInicio, mesFim),
                TipoSoloZarc.SOLO_TEXTURA_MEDIA,
                culturaIds
        );

        Municipio municipio = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");
        ItemComparacaoCultura soja = criarItem(1L, "Soja", 1, 85, new BigDecimal("125.00"));
        ItemComparacaoCultura milho = criarItem(2L, "Milho", 2, 76, new BigDecimal("65.00"));

        when(compararCulturasUseCase.compararCulturas(filtro))
                .thenReturn(new ComparacaoCulturasResultado(
                        municipio,
                        "Meio Oeste",
                        TipoSoloZarc.SOLO_TEXTURA_MEDIA,
                        2025,
                        2026,
                        "2025/2026",
                        List.of(soja, milho),
                        List.of("Aviso comparação")
                ));

        ResponseEntity<CompararCulturasResponse> response = controller.compararCulturas(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2025/2026", response.getBody().safra());
        assertEquals("2", response.getBody().soloSelecionado().codigo());
        assertEquals(2, response.getBody().culturas().size());
        assertEquals("Soja", response.getBody().culturas().get(0).nomeCultura());
        assertEquals("Milho", response.getBody().culturas().get(1).nomeCultura());
        verify(compararCulturasUseCase).compararCulturas(filtro);
    }

    @Test
    void deveRetornarComparacaoDeCulturasSemSoloInformadoComStatusOk() {
        String codigoIbge = "4204806";
        List<Long> culturaIds = List.of(1L, 2L);
        YearMonth mesInicio = YearMonth.now();
        YearMonth mesFim = mesInicio.plusMonths(3);
        CompararCulturasRequest request = new CompararCulturasRequest(
                codigoIbge,
                mesInicio.toString(),
                mesFim.toString(),
                null,
                culturaIds
        );
        FiltroCompararCulturas filtro = new FiltroCompararCulturas(
                codigoIbge,
                new PeriodoPlantioMeses(mesInicio, mesFim),
                null,
                culturaIds
        );

        Municipio municipio = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");
        ItemComparacaoCultura soja = criarItem(1L, "Soja", 1, 85, new BigDecimal("125.00"));
        ItemComparacaoCultura milho = criarItem(2L, "Milho", 2, 76, new BigDecimal("65.00"));

        when(compararCulturasUseCase.compararCulturas(filtro))
                .thenReturn(new ComparacaoCulturasResultado(
                        municipio,
                        "Meio Oeste",
                        null,
                        2025,
                        2026,
                        "2025/2026",
                        List.of(soja, milho),
                        List.of("Aviso comparação")
                ));

        ResponseEntity<CompararCulturasResponse> response = controller.compararCulturas(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("2025/2026", response.getBody().safra());
        org.junit.jupiter.api.Assertions.assertNull(response.getBody().soloSelecionado());
        assertEquals(2, response.getBody().culturas().size());
        verify(compararCulturasUseCase).compararCulturas(filtro);
    }

    private ItemComparacaoCultura criarItem(
            Long id,
            String nome,
            int posicao,
            int nota,
            BigDecimal preco
    ) {
        PeriodoDecendio melhor = PeriodoDecendio.deDecendio(28, Set.of(20));
        return new ItemComparacaoCultura(
                id,
                nome,
                null,
                "GRAOS",
                new ViabilidadeZoneamentoComparacao(true, "Apta", 2, 2, List.of(), List.of()),
                new JanelaPlantioComparacao(
                        6,
                        "Out a Dez",
                        melhor,
                        melhor.formatarRecomendacaoSolo("Solo de textura média"),
                        List.of(melhor)
                ),
                new RiscoClimaticoComparacao(20, "BAIXO (20%)", Set.of(20), 6, 0, 0),
                new MercadoComparacao(
                        nome, preco, "Saca", LocalDate.of(2026, 10, 30),
                        "Meio Oeste", "EPAGRI_CEPA", false,
                        new BigDecimal("3.00"), new BigDecimal("5.00"), new BigDecimal("8.00"),
                        List.of(), List.of()
                ),
                new ScoreMotorDecisaoComparacao(true, posicao, nota, new PontuacaoCriterios(30, 12, 28, 15), List.of()),
                new IndicadoresProdutivosComparacao(true, 2025, 3800, 3700, 15000, 57000, "Aviso")
        );
    }
}
