package com.figueredo.ricardo.mvp.epagri.domain.model;

import java.util.Objects;

public record RecursoPrecoEpagri(
        String identificador,
        FrequenciaCotacao frequencia,
        String url,
        String ultimoModificado
) {
    public RecursoPrecoEpagri {
        Objects.requireNonNull(identificador, "identificador não pode ser nulo");
        Objects.requireNonNull(frequencia, "frequencia não pode ser nula");
        Objects.requireNonNull(url, "url não pode ser nula");
    }
}
