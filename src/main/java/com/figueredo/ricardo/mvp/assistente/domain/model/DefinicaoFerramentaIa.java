package com.figueredo.ricardo.mvp.assistente.domain.model;

import java.util.Map;
import java.util.Objects;

public record DefinicaoFerramentaIa(
        String nome,
        String descricao,
        Map<String, Object> schemaParametrosJson
) {
    public DefinicaoFerramentaIa {
        Objects.requireNonNull(nome, "nome é obrigatório");
        Objects.requireNonNull(descricao, "descricao é obrigatória");
        schemaParametrosJson = schemaParametrosJson != null ? Map.copyOf(schemaParametrosJson) : Map.of();
    }
}
