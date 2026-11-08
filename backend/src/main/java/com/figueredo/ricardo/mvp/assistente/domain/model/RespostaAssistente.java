package com.figueredo.ricardo.mvp.assistente.domain.model;

import java.util.List;
import java.util.Objects;

public record RespostaAssistente(
        String textoResposta,
        List<ChamadaFerramentaIa> ferramentasExecutadas,
        List<String> fontesConsultadas,
        ContextoProdutor contextoConsolidado
) {
    public RespostaAssistente(
            String textoResposta,
            List<ChamadaFerramentaIa> ferramentasExecutadas,
            List<String> fontesConsultadas
    ) {
        this(textoResposta, ferramentasExecutadas, fontesConsultadas, null);
    }

    public RespostaAssistente {
        Objects.requireNonNull(textoResposta, "textoResposta é obrigatório");
        ferramentasExecutadas = ferramentasExecutadas != null ? List.copyOf(ferramentasExecutadas) : List.of();
        fontesConsultadas = fontesConsultadas != null ? List.copyOf(fontesConsultadas) : List.of();
    }
}
