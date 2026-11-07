package com.figueredo.ricardo.mvp.mercado.domain.model;

public record OrigemCotacao(
        String fonte,
        Long pracaId,
        String pracaNome,
        boolean fallbackEstadual,
        String observacaoOrigem
) {
    public OrigemCotacao {
        if (fonte == null || fonte.isBlank()) {
            throw new IllegalArgumentException("fonte é obrigatória");
        }
        if (pracaNome == null || pracaNome.isBlank()) {
            throw new IllegalArgumentException("pracaNome é obrigatório");
        }
    }
}
