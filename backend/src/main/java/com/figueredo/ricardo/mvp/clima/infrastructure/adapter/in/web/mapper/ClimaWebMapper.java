package com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;
import com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web.dto.ClimaAtualResponse;
import org.springframework.stereotype.Component;

@Component
public class ClimaWebMapper {

    public ClimaAtualResponse toResponse(ClimaAtual clima) {
        if (clima == null || !clima.isDisponivel()) {
            return new ClimaAtualResponse(
                    false,
                    clima != null && clima.getMensagem() != null ? clima.getMensagem() : "Temporariamente indisponível",
                    null,
                    null,
                    null
            );
        }

        return new ClimaAtualResponse(
                true,
                null,
                clima.getTemperatura(),
                clima.getCondicao() != null ? clima.getCondicao().name() : null,
                clima.getDescricaoCondicao()
        );
    }
}
