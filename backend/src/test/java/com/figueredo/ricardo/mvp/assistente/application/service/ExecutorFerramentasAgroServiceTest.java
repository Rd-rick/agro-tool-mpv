package com.figueredo.ricardo.mvp.assistente.application.service;

import tools.jackson.databind.ObjectMapper;
import com.figueredo.ricardo.mvp.assistente.domain.model.ChamadaFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.ResultadoFerramentaIa;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase.FiltroRankingOportunidades;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase.FiltroCompararCulturas;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase.FiltroAnaliseCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.AnaliseSecao;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.MercadoSecao;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.ZoneamentoSecao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase.FiltroPeriodoSoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutorFerramentasAgroServiceTest {

    @Mock
    private ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;

    @Mock
    private CalcularRankingOportunidadesUseCase calcularRankingUseCase;

    @Mock
    private ConsultarAnaliseCulturaUseCase consultarAnaliseCulturaUseCase;

    @Mock
    private CompararCulturasUseCase compararCulturasUseCase;

    private ObjectMapper objectMapper;
    private ExecutorFerramentasAgroService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new ExecutorFerramentasAgroService(
                consultarZoneamentoUseCase,
                calcularRankingUseCase,
                consultarAnaliseCulturaUseCase,
                compararCulturasUseCase,
                objectMapper
        );
    }

    @Test
    void deveRetornarQuatroDefinicoesDeFerramentas() {
        List<DefinicaoFerramentaIa> definicoes = service.obterDefinicoes();
        assertEquals(4, definicoes.size());
        assertTrue(definicoes.stream().anyMatch(d -> d.nome().equals(ExecutorFerramentasAgroService.FERRAMENTA_ZONEAMENTO)));
        assertTrue(definicoes.stream().anyMatch(d -> d.nome().equals(ExecutorFerramentasAgroService.FERRAMENTA_RANKING)));
        assertTrue(definicoes.stream().anyMatch(d -> d.nome().equals(ExecutorFerramentasAgroService.FERRAMENTA_ANALISE)));
        assertTrue(definicoes.stream().anyMatch(d -> d.nome().equals(ExecutorFerramentasAgroService.FERRAMENTA_COMPARACAO)));
    }

    @Test
    void deveExecutarZoneamentoComSucesso() {
        Municipio municipio = new Municipio("Curitibanos", "SC", "4204809", 7L, "Meio Oeste");
        ZoneamentoMunicipioResultado mockResultado = new ZoneamentoMunicipioResultado(
                municipio, 2024, 2025, List.of()
        );
        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(any(), any()))
                .thenReturn(mockResultado);

        YearMonth inicio = YearMonth.now();
        YearMonth fim = inicio.plusMonths(3);

        ChamadaFerramentaIa chamada = new ChamadaFerramentaIa(
                ExecutorFerramentasAgroService.FERRAMENTA_ZONEAMENTO,
                Map.of("codigoIbge", "4204809", "mesInicio", inicio.toString(), "mesFim", fim.toString())
        );

        ResultadoFerramentaIa resultado = service.executar(chamada);

        assertTrue(resultado.sucesso());
        assertEquals(ExecutorFerramentasAgroService.FERRAMENTA_ZONEAMENTO, resultado.nomeFerramenta());
        assertNotNull(resultado.dadosJson());
        verify(consultarZoneamentoUseCase).consultarAptasPorMunicipioEPeriodo(any(), any());
    }

    @Test
    void deveExecutarZoneamentoComSoloEspecifico() {
        Municipio municipio = new Municipio("Curitibanos", "SC", "4204809", 7L, "Meio Oeste");
        ZoneamentoMunicipioResultado mockResultado = new ZoneamentoMunicipioResultado(
                municipio, 2024, 2025, List.of()
        );
        when(consultarZoneamentoUseCase.consultarAptasPorMunicipioPeriodoESolo(any(FiltroPeriodoSoloZoneamento.class)))
                .thenReturn(mockResultado);

        YearMonth inicio = YearMonth.now();
        YearMonth fim = inicio.plusMonths(3);

        ChamadaFerramentaIa chamada = new ChamadaFerramentaIa(
                ExecutorFerramentasAgroService.FERRAMENTA_ZONEAMENTO,
                Map.of("codigoIbge", "4204809", "mesInicio", inicio.toString(), "mesFim", fim.toString(), "tipoSolo", "2")
        );

        ResultadoFerramentaIa resultado = service.executar(chamada);

        assertTrue(resultado.sucesso());
        verify(consultarZoneamentoUseCase).consultarAptasPorMunicipioPeriodoESolo(any(FiltroPeriodoSoloZoneamento.class));
    }

    @Test
    void deveExecutarRankingComSucesso() {
        RankingOportunidadesResultado mockResultado = new RankingOportunidadesResultado(
                "4204809", "Curitibanos", "SC", "Meio Oeste", 2024, 2025, List.of(), List.of()
        );
        when(calcularRankingUseCase.calcularRankingPorMunicipioESafra(any(FiltroRankingOportunidades.class)))
                .thenReturn(mockResultado);

        ChamadaFerramentaIa chamada = new ChamadaFerramentaIa(
                ExecutorFerramentasAgroService.FERRAMENTA_RANKING,
                Map.of("codigoIbge", "4204809", "safraIni", 2024, "safraFim", 2025)
        );

        ResultadoFerramentaIa resultado = service.executar(chamada);

        assertTrue(resultado.sucesso());
        assertEquals(ExecutorFerramentasAgroService.FERRAMENTA_RANKING, resultado.nomeFerramenta());
        verify(calcularRankingUseCase).calcularRankingPorMunicipioESafra(any(FiltroRankingOportunidades.class));
    }

    @Test
    void deveExecutarAnaliseCulturaComSucesso() {
        Municipio municipio = new Municipio("Curitibanos", "SC", "4204809", 7L, "Meio Oeste");
        PeriodoDecendio melhorDecendio = PeriodoDecendio.deDecendio(28, Set.of(20));
        ZoneamentoSecao zoneamento = new ZoneamentoSecao(
                municipio, 2024, 2025, "2024/2025", TipoSoloZarc.SOLO_TEXTURA_MEDIA, true,
                "Apta conforme ZARC", melhorDecendio, "Recomendacao", 20, Set.of(20),
                List.of("Soja"), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()
        );
        MercadoSecao mercado = new MercadoSecao(
                "Soja", new BigDecimal("130.00"), "Saca", LocalDate.now(), "Meio Oeste",
                "EPAGRI_CEPA", false, "ESTAVEL", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                List.of(), List.of(), List.of()
        );
        AnaliseSecao analise = new AnaliseSecao(
                null, "Resumo", List.of(), List.of(), null, List.of()
        );

        AnaliseCulturaResultado mockResultado = new AnaliseCulturaResultado(
                1L, "Soja", "Glycine max", "GRAOS", zoneamento, mercado, analise
        );
        when(consultarAnaliseCulturaUseCase.consultarAnaliseCultura(any(FiltroAnaliseCultura.class)))
                .thenReturn(mockResultado);

        YearMonth inicio = YearMonth.now();
        YearMonth fim = inicio.plusMonths(3);

        ChamadaFerramentaIa chamada = new ChamadaFerramentaIa(
                ExecutorFerramentasAgroService.FERRAMENTA_ANALISE,
                Map.of("codigoIbge", "4204809", "culturaId", 1, "mesInicio", inicio.toString(), "mesFim", fim.toString())
        );

        ResultadoFerramentaIa resultado = service.executar(chamada);

        assertTrue(resultado.sucesso());
        assertEquals(ExecutorFerramentasAgroService.FERRAMENTA_ANALISE, resultado.nomeFerramenta());
        verify(consultarAnaliseCulturaUseCase).consultarAnaliseCultura(any(FiltroAnaliseCultura.class));
    }

    @Test
    void deveExecutarComparacaoCulturasComSucesso() {
        Municipio municipio = new Municipio("Curitibanos", "SC", "4204809", 7L, "Meio Oeste");
        ComparacaoCulturasResultado mockResultado = new ComparacaoCulturasResultado(
                municipio, "Meio Oeste", null, 2024, 2025, "2024/2025", List.of(), List.of()
        );
        when(compararCulturasUseCase.compararCulturas(any(FiltroCompararCulturas.class)))
                .thenReturn(mockResultado);

        YearMonth inicio = YearMonth.now();
        YearMonth fim = inicio.plusMonths(3);

        ChamadaFerramentaIa chamada = new ChamadaFerramentaIa(
                ExecutorFerramentasAgroService.FERRAMENTA_COMPARACAO,
                Map.of("codigoIbge", "4204809", "culturaIds", List.of(1, 2), "mesInicio", inicio.toString(), "mesFim", fim.toString())
        );

        ResultadoFerramentaIa resultado = service.executar(chamada);

        assertTrue(resultado.sucesso());
        assertEquals(ExecutorFerramentasAgroService.FERRAMENTA_COMPARACAO, resultado.nomeFerramenta());
        verify(compararCulturasUseCase).compararCulturas(any(FiltroCompararCulturas.class));
    }

    @Test
    void deveRetornarErroParaFerramentaDesconhecida() {
        ChamadaFerramentaIa chamada = new ChamadaFerramentaIa("ferramenta_inexistente", Map.of());
        ResultadoFerramentaIa resultado = service.executar(chamada);

        assertFalse(resultado.sucesso());
        assertTrue(resultado.mensagemErro().contains("Ferramenta desconhecida"));
    }

    @Test
    void deveRetornarErroQuandoArgumentosForemInvalidos() {
        ChamadaFerramentaIa chamada = new ChamadaFerramentaIa(
                ExecutorFerramentasAgroService.FERRAMENTA_ZONEAMENTO,
                Map.of()
        );
        ResultadoFerramentaIa resultado = service.executar(chamada);

        assertFalse(resultado.sucesso());
        assertNotNull(resultado.mensagemErro());
    }
}
