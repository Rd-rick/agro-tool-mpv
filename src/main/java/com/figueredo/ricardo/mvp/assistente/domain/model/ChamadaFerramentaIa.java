package com.figueredo.ricardo.mvp.assistente.domain.model;

import java.util.Map;
import java.util.Objects;

public record ChamadaFerramentaIa(
        String nomeFerramenta,
        Map<String, Object> argumentos
) {
    public ChamadaFerramentaIa {
        Objects.requireNonNull(nomeFerramenta, "nomeFerramenta é obrigatório");
        argumentos = argumentos != null ? Map.copyOf(argumentos) : Map.of();
    }
}
