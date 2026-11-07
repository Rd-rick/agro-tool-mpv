package com.figueredo.ricardo.mvp.zarc.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.MunicipioNaoEncontradoException;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase.FiltroDetalhadoZoneamentoMunicipio;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase.FiltroZoneamentoMunicipio;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcZoneamentoQueryPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.SafraVigencia;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarZoneamentoMunicipioServiceTest {

    @Mock
    private MunicipioPort municipioPort;

    @Mock
    private ZarcZoneamentoQueryPort zarcZoneamentoQueryPort;

    @InjectMocks
    private ConsultarZoneamentoMunicipioService service;

    @Test
    void deveConsolidarCulturasAptasComJanelasRiscosSolosCiclosEMelhorPeriodo() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio(
                "Curitibanos",
                "SC",
                codigoIbge,
                7L,
                "Meio Oeste"
        );

        when(municipioPort.buscarPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(curitibanos));
        when(municipioPort.buscarIdPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(93L));

        List<Integer> decendiosMilho1 = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosMilho1.set(0, 30);
        decendiosMilho1.set(28, 20);

        List<Integer> decendiosMilho2 = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosMilho2.set(0, 20);
        decendiosMilho2.set(3, 40);

        ZarcZoneamentoItem itemMilhoSafra1 = new ZarcZoneamentoItem(
                2L,
                "Milho",
                "Zea mays",
                "GRAOS",
                "Milho 1ª safra",
                2025,
                2026,
                "1",
                "Grupo I",
                "2",
                "Tipo 2",
                "1",
                "Sequeiro",
                "Portaria 100",
                20,
                decendiosMilho1
        );

        ZarcZoneamentoItem itemMilhoSafra2 = new ZarcZoneamentoItem(
                2L,
                "Milho",
                "Zea mays",
                "GRAOS",
                "Milho 2ª safra",
                2025,
                2026,
                "2",
                "Grupo II",
                "3",
                "Tipo 3",
                "1",
                "Sequeiro",
                "Portaria 101",
                20,
                decendiosMilho2
        );

        when(zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(93L, 2025, 2026))
                .thenReturn(List.of(itemMilhoSafra1, itemMilhoSafra2));

        ZoneamentoMunicipioResultado resultado = service.consultarAptasPorMunicipioESafra(codigoIbge, 2025, 2026);

        assertEquals("Curitibanos", resultado.municipio().nome());
        assertEquals(2025, resultado.safraIni());
        assertEquals(2026, resultado.safraFim());
        assertEquals(1, resultado.culturasAptas().size());

        CulturaZoneamentoConsolidado milho = resultado.culturasAptas().get(0);
        assertEquals(2L, milho.culturaId());
        assertEquals("Milho", milho.nomeCultura());
        assertEquals(List.of("Milho 1ª safra", "Milho 2ª safra"), milho.nomesOrigemZarc());
        assertEquals(20, milho.riscoMinimo());
        assertEquals(Set.of(20, 30, 40), milho.niveisRiscoDisponiveis());
        assertEquals(2, milho.tiposSolo().size());
        assertEquals("Solo de textura média", milho.tiposSolo().get(0).tipoSolo());
        assertEquals("Textura média", milho.tiposSolo().get(0).classificacao());
        assertEquals("15% a menos de 35%", milho.tiposSolo().get(0).teorArgila());
        assertEquals(2, milho.ciclos().size());
        assertEquals(3, milho.janelasPlantio().size());
        assertNotNull(milho.melhorPeriodoPlantio());
        assertEquals(1, milho.melhorPeriodoPlantio().decendio());
        assertNotNull(milho.soloProjecao());
        assertEquals("3", milho.soloProjecao().codigo());
        assertEquals("Solo argiloso", milho.soloProjecao().tipoSolo());
        assertEquals(
                "O melhor período de plantio é de 01 a 10 de Janeiro (risco de 20%) para Solo argiloso",
                milho.recomendacaoMelhorPeriodo()
        );

        PeriodoDecendio dec1 = milho.janelasPlantio().get(0);
        assertEquals(1, dec1.decendio());
        assertEquals(1, dec1.mes());
        assertEquals("Janeiro", dec1.nomeMes());
        assertEquals(1, dec1.decendioNoMes());
        assertEquals(1, dec1.diaInicio());
        assertEquals(10, dec1.diaFim());
        assertEquals(20, dec1.menorRisco());
        assertEquals(Set.of(20, 30), dec1.riscosDisponiveis());
        assertEquals("01 a 10 de Janeiro", dec1.descricaoPeriodo());
    }

    @Test
    void deveFiltrarPorPeriodoESoloERecomendarMelhorPeriodoParaCadaCulturaIgnorandoDecendiosZero() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");

        when(municipioPort.buscarPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(curitibanos));
        when(municipioPort.buscarIdPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(93L));
        when(zarcZoneamentoQueryPort.buscarSafraMaisRecentePorMunicipio(93L))
                .thenReturn(Optional.of(new SafraVigencia(2025, 2026)));

        PeriodoPlantioMeses periodo = new PeriodoPlantioMeses(YearMonth.of(2026, 1), YearMonth.of(2026, 2));

        List<Integer> decendiosSojaSolo2 = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosSojaSolo2.set(0, 30);
        decendiosSojaSolo2.set(1, 20);
        decendiosSojaSolo2.set(27, 20);

        List<Integer> decendiosMilhoSolo3 = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosMilhoSolo3.set(0, 20);

        ZarcZoneamentoItem sojaSolo2 = new ZarcZoneamentoItem(
                1L, "Soja", "Glycine max", "GRAOS", "Soja", 2025, 2026,
                "1", "Grupo I", "2", null, "1", "Sequeiro", "Portaria 1", 20, decendiosSojaSolo2
        );
        ZarcZoneamentoItem milhoSolo3 = new ZarcZoneamentoItem(
                2L, "Milho", "Zea mays", "GRAOS", "Milho", 2025, 2026,
                "1", "Grupo I", "3", null, "1", "Sequeiro", "Portaria 2", 20, decendiosMilhoSolo3
        );

        when(zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(93L, 2025, 2026))
                .thenReturn(List.of(sojaSolo2, milhoSolo3));

        ZoneamentoMunicipioResultado resultado = service.consultarAptasPorMunicipioPeriodoESolo(
                codigoIbge,
                periodo,
                TipoSoloZarc.SOLO_TEXTURA_MEDIA
        );

        assertEquals(1, resultado.culturasAptas().size());
        CulturaZoneamentoConsolidado soja = resultado.culturasAptas().get(0);
        assertEquals("Soja", soja.nomeCultura());
        assertEquals(2, soja.janelasPlantio().size());
        assertEquals(2, soja.melhorPeriodoPlantio().decendio());
        assertEquals(20, soja.melhorPeriodoPlantio().menorRisco());
        assertEquals(
                "O melhor período de plantio é de 11 a 20 de Janeiro (risco de 20%) para Solo de textura média",
                soja.recomendacaoMelhorPeriodo()
        );
    }

    @Test
    void deveResolverSafraMaisRecenteQuandoSafraNaoForInformada() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");

        when(municipioPort.buscarPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(curitibanos));
        when(municipioPort.buscarIdPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(93L));
        when(zarcZoneamentoQueryPort.buscarSafraMaisRecentePorMunicipio(93L))
                .thenReturn(Optional.of(new SafraVigencia(2025, 2026)));
        when(zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(93L, 2025, 2026))
                .thenReturn(List.of());

        ZoneamentoMunicipioResultado resultado = service.consultarAptasPorMunicipioESafra(
                new FiltroZoneamentoMunicipio(codigoIbge, null, null)
        );

        assertEquals(2025, resultado.safraIni());
        assertEquals(2026, resultado.safraFim());
        assertTrue(resultado.culturasAptas().isEmpty());
        verify(zarcZoneamentoQueryPort).buscarSafraMaisRecentePorMunicipio(93L);
    }

    @Test
    void deveLancarMunicipioNaoEncontradoExceptionQuandoCodigoIbgeNaoExistir() {
        when(municipioPort.buscarPorCodigoIbge("4299999")).thenReturn(Optional.empty());

        assertThrows(
                MunicipioNaoEncontradoException.class,
                () -> service.consultarAptasPorMunicipioESafra("4299999", 2025, 2026)
        );
    }

    @Test
    void deveFiltrarPorCulturaRiscoMaximoESoloCodigo() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");

        when(municipioPort.buscarPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(curitibanos));
        when(municipioPort.buscarIdPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(93L));

        List<Integer> decendiosRisco20 = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosRisco20.set(27, 20);

        List<Integer> decendiosRisco40 = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosRisco40.set(27, 40);

        ZarcZoneamentoItem sojaSolo2Risco20 = new ZarcZoneamentoItem(
                1L, "Soja", "Glycine max", "GRAOS", "Soja", 2025, 2026,
                "1", "Grupo I", "2", "Tipo 2", "1", "Sequeiro", "Portaria 1", 20, decendiosRisco20
        );
        ZarcZoneamentoItem sojaSolo3Risco40 = new ZarcZoneamentoItem(
                1L, "Soja", "Glycine max", "GRAOS", "Soja", 2025, 2026,
                "2", "Grupo II", "3", "Tipo 3", "1", "Sequeiro", "Portaria 1", 40, decendiosRisco40
        );
        ZarcZoneamentoItem trigoRisco40 = new ZarcZoneamentoItem(
                4L, "Trigo", "Triticum aestivum", "GRAOS", "Trigo", 2025, 2026,
                "1", "Grupo I", "2", "Tipo 2", "1", "Sequeiro", "Portaria 2", 40, decendiosRisco40
        );

        when(zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(93L, 2025, 2026))
                .thenReturn(List.of(sojaSolo2Risco20, sojaSolo3Risco40, trigoRisco40));

        ZoneamentoMunicipioResultado filtrado = service.consultarAptasPorMunicipioComFiltros(
                new FiltroDetalhadoZoneamentoMunicipio(codigoIbge, 2025, 2026, 1L, 20, "2")
        );

        assertEquals(1, filtrado.culturasAptas().size());
        CulturaZoneamentoConsolidado soja = filtrado.culturasAptas().get(0);
        assertEquals(1L, soja.culturaId());
        assertEquals(1, soja.tiposSolo().size());
        assertEquals("2", soja.tiposSolo().get(0).codigo());
        assertEquals(Set.of(20), soja.niveisRiscoDisponiveis());
    }

    @Test
    void deveIncluirCulturasComAoMenosUmDecendioFavoravelNoPeriodoDesejado() {
        String codigoIbge = "4204806";
        Municipio curitibanos = new Municipio("Curitibanos", "SC", codigoIbge, 7L, "Meio Oeste");

        when(municipioPort.buscarPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(curitibanos));
        when(municipioPort.buscarIdPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(93L));
        when(zarcZoneamentoQueryPort.buscarSafraMaisRecentePorMunicipio(93L))
                .thenReturn(Optional.of(new SafraVigencia(2025, 2026)));

        YearMonth mesAtual = YearMonth.of(2025, 11);
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.deTexto(
                "2025-11",
                "2026-02",
                mesAtual
        );

        List<Integer> decendiosIniciaOutubroTerminaJaneiro = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosIniciaOutubroTerminaJaneiro.set(27, 20);
        decendiosIniciaOutubroTerminaJaneiro.set(33, 20);
        decendiosIniciaOutubroTerminaJaneiro.set(0, 20);

        List<Integer> decendiosIniciaJaneiroTerminaMarco = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosIniciaJaneiroTerminaMarco.set(0, 20);
        decendiosIniciaJaneiroTerminaMarco.set(3, 20);
        decendiosIniciaJaneiroTerminaMarco.set(6, 20);

        List<Integer> decendiosIniciaMaio = new ArrayList<>(Collections.nCopies(36, 0));
        decendiosIniciaMaio.set(12, 20);

        ZarcZoneamentoItem sojaIniciouOutubro = new ZarcZoneamentoItem(
                1L, "Soja", "Glycine max", "GRAOS", "Soja", 2025, 2026,
                "1", "Grupo I", "2", "Tipo 2", "1", "Sequeiro", "Portaria 1", 20, decendiosIniciaOutubroTerminaJaneiro
        );
        ZarcZoneamentoItem feijaoIniciaJaneiro = new ZarcZoneamentoItem(
                3L, "Feijão", "Phaseolus vulgaris", "GRAOS", "Feijão 2ª safra", 2025, 2026,
                "1", "Grupo I", "2", "Tipo 2", "1", "Sequeiro", "Portaria 2", 20, decendiosIniciaJaneiroTerminaMarco
        );
        ZarcZoneamentoItem trigoIniciaMaio = new ZarcZoneamentoItem(
                4L, "Trigo", "Triticum aestivum", "GRAOS", "Trigo", 2025, 2026,
                "1", "Grupo I", "2", "Tipo 2", "1", "Sequeiro", "Portaria 3", 20, decendiosIniciaMaio
        );

        when(zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(93L, 2025, 2026))
                .thenReturn(List.of(sojaIniciouOutubro, feijaoIniciaJaneiro, trigoIniciaMaio));

        ZoneamentoMunicipioResultado resultado = service.consultarAptasPorMunicipioEPeriodo(codigoIbge, periodo);

        assertEquals(2, resultado.culturasAptas().size());
        assertEquals("Feijão", resultado.culturasAptas().get(0).nomeCultura());
        assertEquals(2, resultado.culturasAptas().get(0).janelasPlantio().size());
        assertEquals("Soja", resultado.culturasAptas().get(1).nomeCultura());
        assertEquals(2, resultado.culturasAptas().get(1).janelasPlantio().size());
    }

    @Test
    void deveRejeitarMesInicioAnteriorAoMesAtual() {
        YearMonth mesAtual = YearMonth.of(2025, 11);
        assertThrows(
                IllegalArgumentException.class,
                () -> PeriodoPlantioMeses.deTexto(
                        "2025-10",
                        "2026-02",
                        mesAtual
                )
        );
        assertFalse(PeriodoPlantioMeses.identificarMesInicioPlantio(Collections.nCopies(36, 0), Set.of(20, 30, 40)).isPresent());
    }
}
