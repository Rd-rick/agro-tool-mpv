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
}
