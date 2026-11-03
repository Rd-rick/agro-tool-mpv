package com.figueredo.ricardo.mvp.epagri.application.service;

import com.figueredo.ricardo.mvp.epagri.application.port.out.CotacaoPersistencePort;
import com.figueredo.ricardo.mvp.epagri.application.port.out.EpagriClientPort;
import com.figueredo.ricardo.mvp.epagri.application.port.out.EpagriControleCargaPort;
import com.figueredo.ricardo.mvp.epagri.application.port.out.ProdutoCotacaoPort;
import com.figueredo.ricardo.mvp.epagri.application.port.out.RegiaoPracaPort;
import com.figueredo.ricardo.mvp.epagri.domain.model.ControleCargaEpagri;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import com.figueredo.ricardo.mvp.epagri.domain.model.RecursoPrecoEpagri;
import com.figueredo.ricardo.mvp.epagri.domain.model.StatusCarga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportarPrecosEpagriServiceTest {

    @Mock
    private EpagriClientPort clientPort;

    @Mock
    private CotacaoPersistencePort persistencePort;

    @Mock
    private EpagriControleCargaPort controleCargaPort;

    @Mock
    private ProdutoCotacaoPort produtoCotacaoPort;

    @Mock
    private RegiaoPracaPort regiaoPracaPort;

    private ImportarPrecosEpagriService service;

    @BeforeEach
    void setUp() {
        service = new ImportarPrecosEpagriService(
                clientPort,
                persistencePort,
                controleCargaPort,
                produtoCotacaoPort,
                regiaoPracaPort,
                1000
        );
    }

    @Test
    void deveIgnorarImportacaoQuandoArquivoNaoFoiModificadoNaFonte() {
        RecursoPrecoEpagri recurso = new RecursoPrecoEpagri(
                "Historico_precos_diario",
                FrequenciaCotacao.DIARIA,
                "https://fake.epagri.sc.gov.br/diario.xlsx",
                "Fri, 23 Oct 2026 20:06:13 GMT"
        );

        when(clientPort.descobrirRecursosDisponiveis()).thenReturn(List.of(recurso));

        ControleCargaEpagri cargaExistente = new ControleCargaEpagri(
                1L,
                "EPAGRI_CEPA",
                "Historico_precos_diario",
                "Fri, 23 Oct 2026 20:06:13 GMT",
                StatusCarga.SUCESSO,
                5000,
                null,
                OffsetDateTime.now().minusDays(1),
                OffsetDateTime.now().minusDays(1)
        );

        when(controleCargaPort.buscarUltimaCargaComSucesso("EPAGRI_CEPA", "Historico_precos_diario"))
                .thenReturn(Optional.of(cargaExistente));

        var resultado = service.importarTodasFrequenciasConfiguradas();

        assertEquals(1, resultado.totalFrequenciasProcessadas());
        assertEquals(0, resultado.totalRegistrosImportados());
        verify(clientPort, never()).processarPlanilhaStreaming(any(), any(), any(), anyInt(), any(), any());
    }

    @Test
    void deveProcessarComSucessoQuandoArquivoForNovoOuModificado() {
        RecursoPrecoEpagri recurso = new RecursoPrecoEpagri(
                "Historico_precos_diario",
                FrequenciaCotacao.DIARIA,
                "https://fake.epagri.sc.gov.br/diario.xlsx",
                "Fri, 23 Oct 2026 20:06:13 GMT"
        );

        when(clientPort.descobrirRecursosDisponiveis()).thenReturn(List.of(recurso));
        when(controleCargaPort.buscarUltimaCargaComSucesso("EPAGRI_CEPA", "Historico_precos_diario"))
                .thenReturn(Optional.empty());
        when(controleCargaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        when(produtoCotacaoPort.mapearNomeBrutoParaIdPorFonte("EPAGRI_CEPA"))
                .thenReturn(Map.of("Soja industrial - sc 60 kg", 1L));
        when(regiaoPracaPort.mapearNomeParaIdPorUf("SC"))
                .thenReturn(Map.of("Oeste", 2L));

        when(clientPort.processarPlanilhaStreaming(
                eq(recurso.url()),
                eq(FrequenciaCotacao.DIARIA),
                any(),
                eq(1000),
                any(),
                any()
        )).thenReturn(1500);

        var resultado = service.importarFrequencia(FrequenciaCotacao.DIARIA);

        assertEquals(1, resultado.totalFrequenciasProcessadas());
        assertEquals(1500, resultado.totalRegistrosImportados());
        verify(controleCargaPort, org.mockito.Mockito.times(2)).salvar(any());
    }
}
