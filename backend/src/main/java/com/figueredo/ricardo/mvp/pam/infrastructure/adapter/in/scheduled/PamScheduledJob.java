package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.scheduled;

import com.figueredo.ricardo.mvp.pam.application.port.in.ImportarPamUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.jobs.pam.enabled", havingValue = "true", matchIfMissing = true)
public class PamScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(PamScheduledJob.class);

    private final ImportarPamUseCase importarPamUseCase;

    public PamScheduledJob(ImportarPamUseCase importarPamUseCase) {
        this.importarPamUseCase = importarPamUseCase;
    }

    @Scheduled(cron = "${app.jobs.pam.cron:0 0 4 * * ?}")
    public void executar() {
        log.info("Iniciando execução agendada do Job IBGE/PAM");
        try {
            var resultado = importarPamUseCase.importarTodosAnosConfigurados();
            log.info("Job IBGE/PAM concluído. Total anos: {}, Total registros: {}",
                    resultado.totalAnosProcessados(), resultado.totalRegistrosImportados());
        } catch (Exception e) {
            log.error("Erro na execução agendada do Job IBGE/PAM", e);
        }
    }
}
