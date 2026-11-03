package com.figueredo.ricardo.mvp.zarc.application.service;

import com.figueredo.ricardo.mvp.zarc.application.port.out.ControleCargaPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.CulturaFontePort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcDatasetClientPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcPersistencePort;
import com.figueredo.ricardo.mvp.zarc.domain.model.ControleCarga;
import com.figueredo.ricardo.mvp.zarc.domain.model.RecursoZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.StatusCarga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportarZarcServiceTest {

    @Mock
    private ZarcDatasetClientPort datasetClientPort;

    @Mock
    private ZarcPersistencePort persistencePort;

    @Mock
    private ControleCargaPort controleCargaPort;

    @Mock
    private MunicipioPort municipioPort;

    @Mock
    private CulturaFontePort culturaFontePort;

    private ImportarZarcService service;

    @BeforeEach
    void setUp() {
        service = new ImportarZarcService(
                datasetClientPort,
                persistencePort,
                controleCargaPort,
                municipioPort,
                culturaFontePort,
                List.of("2024/2025"),
                1000
        );
    }

    @Test
    void deveIgnorarImportacaoQuandoArquivoNaoFoiModificado() {
        RecursoZarc recurso = new RecursoZarc(
                "rec-1",
                "Tábua de risco - Safra 2024/2025",
                "CSV",
                "http://fake.gov.br/2024-2025.csv",
                "2026-11-01T07:00:00"
        );
        ControleCarga controleAnterior = new ControleCarga(
                1L, "MAPA_ZARC", "rec-1", "2026-11-01T07:00:00",
                StatusCarga.SUCESSO, 500, null, Instant.now(), Instant.now()
        );

        when(datasetClientPort.buscarRecursosDisponiveis()).thenReturn(List.of(recurso));
        when(controleCargaPort.buscarUltimaCargaComSucesso("MAPA_ZARC", "rec-1"))
                .thenReturn(Optional.of(controleAnterior));

        var resultado = service.importarTodasSafrasConfiguradas();

        assertEquals(1, resultado.totalSafrasProcessadas());
        assertEquals(0, resultado.totalRegistrosImportados());
        verify(datasetClientPort, never()).abrirStreamCsv(anyString());
        verify(persistencePort, never()).salvarLote(anyList());
    }

    @Test
    void deveProcessarLinhasFiltrandoApenasSantaCatarinaECulturasMapeadas() {
        RecursoZarc recurso = new RecursoZarc(
                "rec-1",
                "Tábua de risco - Safra 2024/2025",
                "CSV",
                "http://fake.gov.br/2024-2025.csv",
                "2026-11-01T12:00:00"
        );

        when(datasetClientPort.buscarRecursosDisponiveis()).thenReturn(List.of(recurso));
        when(controleCargaPort.buscarUltimaCargaComSucesso("MAPA_ZARC", "rec-1"))
                .thenReturn(Optional.empty());
        when(controleCargaPort.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        when(municipioPort.mapearIbgeParaIdPorUf("SC")).thenReturn(Map.of("4204806", 100L));
        when(culturaFontePort.mapearCodigoOrigemParaIdPorFonte("MAPA_ZARC")).thenReturn(Map.of("12016720000011", 200L));

        String header = "\uFEFFNome_cultura;SafraIni;SafraFin;Cod_Cultura;Cod_Ciclo;Cod_Solo;geocodigo;UF;municipio;Cod_Clima;Nome_Clima;Cod_Outros_Manejos;Nome_Outros_Manejos;Produtividade;Cod_NM;Cod_Munic;Cod_Meso;Cod_Micro;Portaria;dec1;dec2;dec3;dec4;dec5;dec6;dec7;dec8;dec9;dec10;dec11;dec12;dec13;dec14;dec15;dec16;dec17;dec18;dec19;dec20;dec21;dec22;dec23;dec24;dec25;dec26;dec27;dec28;dec29;dec30;dec31;dec32;dec33;dec34;dec35;dec36\n";
        String linhaRS = "Soja;2024;2025;12016720000011;22;14;4316477;RS;Salvador;0;Nao;1;Sequeiro;0;0;001;01;001;Port.1;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0\n";
        String linhaSCValida = "\"Soja\";2024;2025;\"12016720000011\";22;14;\"4204806\";\"SC\";Curitibanos;0;Nao;1;Sequeiro;0;0;009764;03;009;Port.120;20;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;20;20;20;20;20;20;20;20;20\n";
        String linhaSCCulturaDesconhecida = "Outra;2024;2025;99999999999999;22;14;4204806;SC;Curitibanos;0;Nao;1;Sequeiro;0;0;009764;03;009;Port.120;20;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;0;20;20;20;20;20;20;20;20;20\n";

        String csvCompleto = header + linhaRS + linhaSCValida + linhaSCCulturaDesconhecida;
        ByteArrayInputStream is = new ByteArrayInputStream(csvCompleto.getBytes(StandardCharsets.ISO_8859_1));
        when(datasetClientPort.abrirStreamCsv(recurso.url())).thenReturn(is);

        var resultado = service.importarTodasSafrasConfiguradas();

        assertEquals(1, resultado.totalSafrasProcessadas());
        assertEquals(1, resultado.totalRegistrosImportados());
        verify(persistencePort).salvarLote(anyList());
    }
}
