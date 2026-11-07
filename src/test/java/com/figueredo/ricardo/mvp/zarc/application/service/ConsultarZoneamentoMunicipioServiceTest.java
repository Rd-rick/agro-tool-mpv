package com.figueredo.ricardo.mvp.zarc.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.MunicipioNaoEncontradoException;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase.FiltroZoneamentoMunicipio;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcZoneamentoQueryPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.SafraVigencia;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void deveConsolidarCulturasAptasComJanelasRiscosSolosECiclos() {
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
        assertEquals(2, milho.ciclos().size());
        assertEquals(3, milho.janelasPlantio().size());

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

        PeriodoDecendio dec4 = milho.janelasPlantio().get(1);
        assertEquals(4, dec4.decendio());
        assertEquals(2, dec4.mes());
        assertEquals("Fevereiro", dec4.nomeMes());
        assertEquals(1, dec4.decendioNoMes());
        assertEquals(1, dec4.diaInicio());
        assertEquals(10, dec4.diaFim());
        assertEquals(40, dec4.menorRisco());

        PeriodoDecendio dec29 = milho.janelasPlantio().get(2);
        assertEquals(29, dec29.decendio());
        assertEquals(10, dec29.mes());
        assertEquals("Outubro", dec29.nomeMes());
        assertEquals(2, dec29.decendioNoMes());
        assertEquals(11, dec29.diaInicio());
        assertEquals(20, dec29.diaFim());
        assertEquals(20, dec29.menorRisco());
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
}
