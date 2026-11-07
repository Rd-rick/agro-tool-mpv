package com.figueredo.ricardo.mvp.assistente.domain.model;

import java.util.Objects;

public record PerguntaProdutor(
        String texto,
        ContextoProdutor contexto
) {
    public PerguntaProdutor {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("texto da pergunta é obrigatório");
        }
        texto = texto.trim();
        Objects.requireNonNull(contexto, "contexto do produtor é obrigatório");
    }
}
