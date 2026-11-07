package com.figueredo.ricardo.mvp.pam.application.service;

import com.figueredo.ricardo.mvp.pam.application.port.out.PamControleCargaPort;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamCulturaFontePort;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamDatasetClientPort;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamMunicipioPort;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamPersistencePort;
import com.figueredo.ricardo.mvp.pam.domain.model.ControleCargaPam;
import com.figueredo.ricardo.mvp.pam.domain.model.PamProducaoRegistro;
import com.figueredo.ricardo.mvp.pam.domain.model.RecursoPam;
import com.figueredo.ricardo.mvp.pam.domain.model.StatusCargaPam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportarPamServiceTest {

    @Mock
    private PamDatasetClientPort datasetClientPort;

    @Mock
    private PamPersistencePort persistencePort;

    @Mock
    private PamControleCargaPort controleCargaPort;

    @Mock
    private PamMunicipioPort municipioPort;

    @Mock
    private PamCulturaFontePort culturaFontePort;

    private ImportarPamService service;

    @BeforeEach
    void setUp() {
        service = new ImportarPamService(
                datasetClientPort,
                persistencePort,
                controleCargaPort,
                municipioPort,
                culturaFontePort,
                List.of(2024),
                1000
        );
    }

    @Test
    void deveIgnorarImportacaoQuandoPeriodoNaoFoiModificadoNaFonte() {
        RecursoPam recurso = new RecursoPam("PAM_5457_SC_2024", 2024, "23/10/2026");
        ControleCargaPam controleAnterior = new ControleCargaPam(
                1L,
                "IBGE_PAM",
                "PAM_5457_SC_2024",
                "23/10/2026",
                StatusCargaPam.SUCESSO,
                1200,
                null,
                Instant.now(),
                Instant.now()
        );

        when(datasetClientPort.buscarRecursosDisponiveis()).thenReturn(List.of(recurso));
        when(controleCargaPort.buscarUltimaCargaComSucesso("IBGE_PAM", "PAM_5457_SC_2024"))
                .thenReturn(Optional.of(controleAnterior));

        var resultado = service.importarTodosAnosConfigurados();

        assertEquals(1, resultado.totalAnosProcessados());
        assertEquals(0, resultado.totalRegistrosImportados());
        verify(datasetClientPort, never()).abrirStreamValoresAno(anyInt(), anyCollection());
        verify(persistencePort, never()).salvarLote(anyList());
    }

    @Test
    void deveAgruparVariaveisPorMunicipioECulturaEDescartarSemProducao() {
        RecursoPam recurso = new RecursoPam("PAM_5457_SC_2024", 2024, "23/10/2026");

        when(datasetClientPort.buscarRecursosDisponiveis()).thenReturn(List.of(recurso));
        when(controleCargaPort.buscarUltimaCargaComSucesso("IBGE_PAM", "PAM_5457_SC_2024"))
                .thenReturn(Optional.empty());
        when(controleCargaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        when(municipioPort.mapearIbgeParaIdPorUf("SC")).thenReturn(Map.of(
                "4204806", 100L,
                "4200051", 101L
        ));
        when(culturaFontePort.mapearCodigoOrigemParaIdPorFonte("IBGE_PAM")).thenReturn(Map.of(
                "40124", 200L,
                "40100", 201L
        ));

        String jsonSidra = """
                [
                  {"NC":"6","D1C":"4204806","D2C":"8331","D3C":"2024","D4C":"40124","V":"24100"},
                  {"NC":"6","D1C":"4204806","D2C":"8331","D3C":"2024","D4C":"40100","V":"-"},
                  {"NC":"6","D1C":"4204806","D2C":"216","D3C":"2024","D4C":"40124","V":"24000"},
                  {"NC":"6","D1C":"4204806","D2C":"216","D3C":"2024","D4C":"40100","V":"-"},
                  {"NC":"6","D1C":"4204806","D2C":"214","D3C":"2024","D4C":"40124","V":"69408"},
                  {"NC":"6","D1C":"4204806","D2C":"214","D3C":"2024","D4C":"40100","V":"..."},
                  {"NC":"6","D1C":"4204806","D2C":"112","D3C":"2024","D4C":"40124","V":"2880"},
                  {"NC":"6","D1C":"4204806","D2C":"112","D3C":"2024","D4C":"40100","V":"-"},
                  {"NC":"6","D1C":"4204806","D2C":"215","D3C":"2024","D4C":"40124","V":"138816"},
                  {"NC":"6","D1C":"4204806","D2C":"215","D3C":"2024","D4C":"40100","V":"-"},
                  {"NC":"6","D1C":"4200051","D2C":"8331","D3C":"2024","D4C":"40124","V":"3270"},
                  {"NC":"6","D1C":"4200051","D2C":"216","D3C":"2024","D4C":"40124","V":"3270"},
                  {"NC":"6","D1C":"4200051","D2C":"214","D3C":"2024","D4C":"40124","V":"9810"},
                  {"NC":"6","D1C":"4200051","D2C":"112","D3C":"2024","D4C":"40124","V":"3000"},
                  {"NC":"6","D1C":"4200051","D2C":"215","D3C":"2024","D4C":"40124","V":"19620"}
                ]
                """;

        ByteArrayInputStream is = new ByteArrayInputStream(jsonSidra.getBytes(StandardCharsets.UTF_8));
        when(datasetClientPort.abrirStreamValoresAno(eq(2024), anyCollection())).thenReturn(is);
        when(persistencePort.salvarLote(anyList())).thenReturn(2);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PamProducaoRegistro>> captor = ArgumentCaptor.forClass(List.class);

        var resultado = service.importarAno(2024);

        assertEquals(1, resultado.totalAnosProcessados());
        assertEquals(2, resultado.totalRegistrosImportados());

        verify(persistencePort).salvarLote(captor.capture());
        List<PamProducaoRegistro> salvos = captor.getValue();
        assertEquals(2, salvos.size());

        PamProducaoRegistro curitibanosSoja = salvos.getFirst();
        assertEquals(100L, curitibanosSoja.municipioId());
        assertEquals(200L, curitibanosSoja.culturaCodigoFonteId());
        assertEquals(2024, curitibanosSoja.ano());
        assertEquals(24100, curitibanosSoja.areaPlantadaHa());
        assertEquals(24000, curitibanosSoja.areaColhidaHa());
        assertEquals(69408, curitibanosSoja.quantidadeProduzidaTon());
        assertEquals(2880, curitibanosSoja.rendimentoMedioKgHa());
        assertEquals(new BigDecimal("138816"), curitibanosSoja.valorProducaoMilReais());
    }

    @Test
    void deveMarcarControleCargaComoErroQuandoFalharAberturaDeStream() {
        RecursoPam recurso = new RecursoPam("PAM_5457_SC_2024", 2024, "23/10/2026");

        when(datasetClientPort.buscarRecursosDisponiveis()).thenReturn(List.of(recurso));
        when(controleCargaPort.buscarUltimaCargaComSucesso("IBGE_PAM", "PAM_5457_SC_2024"))
                .thenReturn(Optional.empty());
        when(controleCargaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(municipioPort.mapearIbgeParaIdPorUf("SC")).thenReturn(Map.of("4204806", 100L));
        when(culturaFontePort.mapearCodigoOrigemParaIdPorFonte("IBGE_PAM")).thenReturn(Map.of("40124", 200L));
        when(datasetClientPort.abrirStreamValoresAno(eq(2024), anyCollection()))
                .thenThrow(new IllegalStateException("Falha HTTP 500"));

        ArgumentCaptor<ControleCargaPam> controleCaptor = ArgumentCaptor.forClass(ControleCargaPam.class);

        var resultado = service.importarAno(2024);

        assertEquals(0, resultado.totalAnosProcessados());
        assertEquals(0, resultado.totalRegistrosImportados());
        verify(controleCargaPort, org.mockito.Mockito.times(2)).salvar(controleCaptor.capture());
        assertEquals(StatusCargaPam.ERRO, controleCaptor.getAllValues().getLast().getStatus());
    }
}
