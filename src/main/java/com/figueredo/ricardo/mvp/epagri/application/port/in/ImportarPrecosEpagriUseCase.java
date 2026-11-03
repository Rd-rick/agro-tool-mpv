package com.figueredo.ricardo.mvp.epagri.application.port.in;

import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;

import java.util.List;

public interface ImportarPrecosEpagriUseCase {

    ResultadoImportacao importarTodasFrequenciasConfiguradas();

    ResultadoImportacao importarFrequencia(FrequenciaCotacao frequencia);

    record ResultadoImportacao(
            int totalFrequenciasProcessadas,
            int totalRegistrosImportados,
            List<String> detalhes
    ) {
    }
}
