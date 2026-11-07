package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.scheduled;

import com.figueredo.ricardo.mvp.epagri.application.port.in.ImportarPrecosEpagriUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.jobs.epagri.enabled", havingValue = "true", matchIfMissing = true)
public class EpagriPrecosJobScheduler {

    private static final Logger log = LoggerFactory.getLogger(EpagriPrecosJobScheduler.class);

    private final ImportarPrecosEpagriUseCase importarPrecosEpagriUseCase;

    public EpagriPrecosJobScheduler(ImportarPrecosEpagriUseCase importarPrecosEpagriUseCase) {
        this.importarPrecosEpagriUseCase = importarPrecosEpagriUseCase;
    }

    @Scheduled(cron = "${app.jobs.epagri.cron:0 30 3 * * ?}")
    public void executarAgendado() {
        log.info("Iniciando execução agendada do job de preços Epagri/Cepa");
        try {
            var resultado = importarPrecosEpagriUseCase.importarTodasFrequenciasConfiguradas();
            log.info("Execução agendada Epagri concluída: {} frequências processadas, {} registros inseridos/atualizados",
                    resultado.totalFrequenciasProcessadas(), resultado.totalRegistrosImportados());
        } catch (Exception e) {
            log.error("Erro na execução agendada do job Epagri", e);
        }
    }
}
