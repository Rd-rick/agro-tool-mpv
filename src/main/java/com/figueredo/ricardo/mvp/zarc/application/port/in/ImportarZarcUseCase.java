package com.figueredo.ricardo.mvp.zarc.application.port.in;

import java.util.List;

public interface ImportarZarcUseCase {

    record ResultadoImportacao(
            int totalSafrasProcessadas,
            int totalRegistrosImportados,
            List<String> detalhes
    ) {}

    ResultadoImportacao importarTodasSafrasConfiguradas();

    ResultadoImportacao importarSafra(String safraNome);
}
