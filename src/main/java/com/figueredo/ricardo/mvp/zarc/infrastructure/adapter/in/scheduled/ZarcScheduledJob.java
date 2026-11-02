package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.scheduled;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ImportarZarcUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.jobs.zarc.enabled", havingValue = "true", matchIfMissing = true)
public class ZarcScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(ZarcScheduledJob.class);

    private final ImportarZarcUseCase importarZarcUseCase;

    public ZarcScheduledJob(ImportarZarcUseCase importarZarcUseCase) {
        this.importarZarcUseCase = importarZarcUseCase;
    }

    @Scheduled(cron = "${app.jobs.zarc.cron:0 0 3 * * ?}")
    public void executar() {
        log.info("Iniciando execução agendada do Job ZARC");
        try {
            var resultado = importarZarcUseCase.importarTodasSafrasConfiguradas();
            log.info("Job ZARC concluído. Total safras: {}, Total registros: {}",
                    resultado.totalSafrasProcessadas(), resultado.totalRegistrosImportados());
        } catch (Exception e) {
            log.error("Erro na execução agendada do Job ZARC", e);
        }
    }
}
