package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ImportarZarcUseCase.ResultadoImportacao;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto.ExecutarImportacaoZarcResponse;
import org.springframework.stereotype.Component;

@Component
public class ZarcJobWebMapper {

    public ExecutarImportacaoZarcResponse toResponse(ResultadoImportacao resultado) {
        return new ExecutarImportacaoZarcResponse(
                resultado.totalSafrasProcessadas(),
                resultado.totalRegistrosImportados(),
                resultado.detalhes()
        );
    }
}
