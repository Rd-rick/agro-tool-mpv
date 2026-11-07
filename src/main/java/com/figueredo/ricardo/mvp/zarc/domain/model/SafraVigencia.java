package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.Objects;

public record SafraVigencia(
        Integer safraIni,
        Integer safraFim
) {
    public SafraVigencia {
        Objects.requireNonNull(safraIni, "safraIni é obrigatório");
        Objects.requireNonNull(safraFim, "safraFim é obrigatório");
        if (safraFim < safraIni) {
            throw new IllegalArgumentException("safraFim não pode ser menor que safraIni");
        }
    }

    public static SafraVigencia deTexto(String safra) {
        if (safra == null || safra.isBlank()) {
            throw new IllegalArgumentException("safra é obrigatória");
        }
        String valor = safra.trim();
        if (!valor.matches("^\\d{4}/\\d{4}$")) {
            throw new IllegalArgumentException("safra deve estar no formato AAAA/AAAA");
        }
        String[] partes = valor.split("/");
        int inicio = Integer.parseInt(partes[0]);
        int fim = Integer.parseInt(partes[1]);
        return new SafraVigencia(inicio, fim);
    }

    public String formatada() {
        return safraIni + "/" + safraFim;
    }
}
