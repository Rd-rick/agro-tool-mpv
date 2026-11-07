package com.figueredo.ricardo.mvp.epagri.application.port.out;

import com.figueredo.ricardo.mvp.epagri.domain.model.CotacaoPrecoRegistro;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import com.figueredo.ricardo.mvp.epagri.domain.model.RecursoPrecoEpagri;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public interface EpagriClientPort {

    List<RecursoPrecoEpagri> descobrirRecursosDisponiveis();

    Optional<String> obterUltimoModificado(String urlArquivo);

    int processarPlanilhaStreaming(
            String urlArquivo,
            FrequenciaCotacao frequencia,
            Consumer<List<CotacaoPrecoRegistro>> consumidorLote,
            int batchSize,
            Map<String, Long> mapaProdutos,
            Map<String, Long> mapaPracas
    );
}
