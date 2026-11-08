package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto;

import java.util.List;

public record RespostaAssistenteResponse(
        String resposta,
        List<ChamadaFerramentaResponse> ferramentasUtilizadas,
        List<String> fontesCitadas,
        ContextoProdutorResponse contexto
) {
    public RespostaAssistenteResponse(
            String resposta,
            List<ChamadaFerramentaResponse> ferramentasUtilizadas,
            List<String> fontesCitadas
    ) {
        this(resposta, ferramentasUtilizadas, fontesCitadas, null);
    }
}
