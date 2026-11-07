package com.figueredo.ricardo.mvp.mercado.domain.model;

import java.util.List;
import java.util.Objects;

public record CulturaMercadoResultado(
        Long culturaId,
        String nomeCultura,
        List<ProdutoMercadoAnalise> produtos
) {
    public CulturaMercadoResultado {
        Objects.requireNonNull(culturaId, "culturaId é obrigatório");
        if (nomeCultura == null || nomeCultura.isBlank()) {
            throw new IllegalArgumentException("nomeCultura é obrigatório");
        }
        produtos = produtos != null ? List.copyOf(produtos) : List.of();
    }
}
