package com.figueredo.ricardo.mvp.epagri.application.service;

import com.figueredo.ricardo.mvp.epagri.application.port.in.ImportarPrecosEpagriUseCase;
import com.figueredo.ricardo.mvp.epagri.application.port.out.CotacaoPersistencePort;
import com.figueredo.ricardo.mvp.epagri.application.port.out.EpagriClientPort;
import com.figueredo.ricardo.mvp.epagri.application.port.out.EpagriControleCargaPort;
import com.figueredo.ricardo.mvp.epagri.application.port.out.ProdutoCotacaoPort;
import com.figueredo.ricardo.mvp.epagri.application.port.out.RegiaoPracaPort;
import com.figueredo.ricardo.mvp.epagri.domain.model.ControleCargaEpagri;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import com.figueredo.ricardo.mvp.epagri.domain.model.RecursoPrecoEpagri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImportarPrecosEpagriService implements ImportarPrecosEpagriUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImportarPrecosEpagriService.class);
    private static final String FONTE_EPAGRI = "EPAGRI_CEPA";
    private static final String UF_ALVO = "SC";

    private final EpagriClientPort clientPort;
    private final CotacaoPersistencePort persistencePort;
    private final EpagriControleCargaPort controleCargaPort;
    private final ProdutoCotacaoPort produtoCotacaoPort;
    private final RegiaoPracaPort regiaoPracaPort;
    private final int batchSize;

    public ImportarPrecosEpagriService(
            EpagriClientPort clientPort,
            CotacaoPersistencePort persistencePort,
            EpagriControleCargaPort controleCargaPort,
            ProdutoCotacaoPort produtoCotacaoPort,
            RegiaoPracaPort regiaoPracaPort,
            @Value("${app.jobs.epagri.batch-size:1000}") int batchSize
    ) {
        this.clientPort = clientPort;
        this.persistencePort = persistencePort;
        this.controleCargaPort = controleCargaPort;
        this.produtoCotacaoPort = produtoCotacaoPort;
        this.regiaoPracaPort = regiaoPracaPort;
        this.batchSize = batchSize;
    }

    @Override
    public ResultadoImportacao importarTodasFrequenciasConfiguradas() {
        int totalFrequencias = 0;
        int totalRegistros = 0;
        List<String> detalhes = new ArrayList<>();

        List<RecursoPrecoEpagri> recursos = clientPort.descobrirRecursosDisponiveis();

        for (RecursoPrecoEpagri recurso : recursos) {
            try {
                int importados = processarRecurso(recurso);
                totalFrequencias++;
                totalRegistros += importados;
                detalhes.add("Frequência " + recurso.frequencia() + ": processados " + importados + " registros");
            } catch (Exception e) {
                log.error("Falha ao processar preços Epagri para {}", recurso.frequencia(), e);
                detalhes.add("Frequência " + recurso.frequencia() + ": erro - " + e.getMessage());
            }
        }

        return new ResultadoImportacao(totalFrequencias, totalRegistros, detalhes);
    }

    @Override
    public ResultadoImportacao importarFrequencia(FrequenciaCotacao frequencia) {
        List<RecursoPrecoEpagri> recursos = clientPort.descobrirRecursosDisponiveis();
        Optional<RecursoPrecoEpagri> recursoOpt = recursos.stream()
                .filter(r -> r.frequencia() == frequencia)
                .findFirst();

        if (recursoOpt.isEmpty()) {
            return new ResultadoImportacao(0, 0, List.of("Frequência " + frequencia + ": recurso não identificado"));
        }

        RecursoPrecoEpagri recurso = recursoOpt.get();
        try {
            int importados = processarRecurso(recurso);
            return new ResultadoImportacao(1, importados, List.of("Frequência " + frequencia + ": processados " + importados + " registros"));
        } catch (Exception e) {
            log.error("Falha ao processar preços Epagri para {}", frequencia, e);
            return new ResultadoImportacao(0, 0, List.of("Frequência " + frequencia + ": erro - " + e.getMessage()));
        }
    }

    private int processarRecurso(RecursoPrecoEpagri recurso) {
        Optional<ControleCargaEpagri> ultimaCarga = controleCargaPort.buscarUltimaCargaComSucesso(
                FONTE_EPAGRI,
                recurso.identificador()
        );

        if (ultimaCarga.isPresent()) {
            String ultimoModificadoRegistrado = ultimaCarga.get().getUltimoModificadoFonte();
            if (ultimoModificadoRegistrado != null && ultimoModificadoRegistrado.equalsIgnoreCase(recurso.ultimoModificado())) {
                log.info("Recurso Epagri {} não foi alterado na fonte (last_modified={}). Ignorando importação.",
                        recurso.identificador(), recurso.ultimoModificado());
                return 0;
            }
        }

        ControleCargaEpagri controle = new ControleCargaEpagri(
                FONTE_EPAGRI,
                recurso.identificador(),
                recurso.ultimoModificado()
        );
        controle = controleCargaPort.salvar(controle);

        Map<String, Long> produtos = produtoCotacaoPort.mapearNomeBrutoParaIdPorFonte(FONTE_EPAGRI);
        Map<String, Long> pracas = regiaoPracaPort.mapearNomeParaIdPorUf(UF_ALVO);

        try {
            int totalProcessados = clientPort.processarPlanilhaStreaming(
                    recurso.url(),
                    recurso.frequencia(),
                    persistencePort::salvarLote,
                    batchSize,
                    produtos,
                    pracas
            );

            controle.marcarSucesso(totalProcessados);
            controleCargaPort.salvar(controle);
            log.info("Importação de preços Epagri {} finalizada com sucesso. Registros salvos: {}",
                    recurso.frequencia(), totalProcessados);
            return totalProcessados;
        } catch (Exception e) {
            controle.marcarErro(e.getMessage());
            controleCargaPort.salvar(controle);
            throw new RuntimeException("Erro ao processar importação de preços Epagri: " + e.getMessage(), e);
        }
    }
}
