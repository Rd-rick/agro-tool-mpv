package com.figueredo.ricardo.mvp.pam.application.port.in;

import java.util.List;

public interface ImportarPamUseCase {

    ResultadoImportacao importarTodosAnosConfigurados();

    ResultadoImportacao importarAno(int ano);

    record ResultadoImportacao(
            int totalAnosProcessados,
            int totalRegistrosImportados,
            List<String> detalhes
    ) {}
}
