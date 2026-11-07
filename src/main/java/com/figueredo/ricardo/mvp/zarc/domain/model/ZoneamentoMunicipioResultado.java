package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.List;
import java.util.Objects;

public record ZoneamentoMunicipioResultado(
        Municipio municipio,
        Integer safraIni,
        Integer safraFim,
        List<CulturaZoneamentoConsolidado> culturasAptas
) {
    public ZoneamentoMunicipioResultado {
        Objects.requireNonNull(municipio, "municipio é obrigatório");
        culturasAptas = culturasAptas != null ? List.copyOf(culturasAptas) : List.of();
    }
}
