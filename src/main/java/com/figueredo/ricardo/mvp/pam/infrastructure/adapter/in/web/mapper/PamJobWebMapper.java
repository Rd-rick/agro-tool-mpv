package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.pam.application.port.in.ImportarPamUseCase.ResultadoImportacao;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web.dto.ExecutarImportacaoPamResponse;
import org.springframework.stereotype.Component;

@Component
public class PamJobWebMapper {

    public ExecutarImportacaoPamResponse toResponse(ResultadoImportacao resultado) {
        return new ExecutarImportacaoPamResponse(
                resultado.totalAnosProcessados(),
                resultado.totalRegistrosImportados(),
                resultado.detalhes()
        );
    }
}
