package com.figueredo.ricardo.mvp.decisao.domain.model;

import java.util.List;
import java.util.Objects;

public record RankingOportunidadesResultado(
        String codigoIbge,
        String nomeMunicipio,
        String uf,
        String pracaRegionalMunicipio,
        Integer safraIni,
        Integer safraFim,
        List<ScoreOportunidade> oportunidades,
        List<String> avisosGerais
) {
    public RankingOportunidadesResultado {
        if (codigoIbge == null || codigoIbge.isBlank()) {
            throw new IllegalArgumentException("codigoIbge é obrigatório");
        }
        if (nomeMunicipio == null || nomeMunicipio.isBlank()) {
            throw new IllegalArgumentException("nomeMunicipio é obrigatório");
        }
        Objects.requireNonNull(uf, "uf é obrigatória");
        oportunidades = oportunidades != null ? List.copyOf(oportunidades) : List.of();
        avisosGerais = avisosGerais != null ? List.copyOf(avisosGerais) : List.of();
    }
}
