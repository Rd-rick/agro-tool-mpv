package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.epagri.application.port.in.ImportarPrecosEpagriUseCase.ResultadoImportacao;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web.dto.ExecutarImportacaoEpagriResponse;
import org.springframework.stereotype.Component;

@Component
public class EpagriJobWebMapper {

    public ExecutarImportacaoEpagriResponse toResponse(ResultadoImportacao resultado) {
        return new ExecutarImportacaoEpagriResponse(
                resultado.totalFrequenciasProcessadas(),
                resultado.totalRegistrosImportados(),
                resultado.detalhes()
        );
    }
}
