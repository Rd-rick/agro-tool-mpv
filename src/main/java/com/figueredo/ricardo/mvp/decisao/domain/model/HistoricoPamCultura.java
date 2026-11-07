package com.figueredo.ricardo.mvp.decisao.domain.model;

import java.util.Objects;

public record HistoricoPamCultura(
        Long culturaId,
        boolean possuiHistorico,
        Integer ultimoAno,
        Integer areaPlantadaHa,
        Integer quantidadeProduzidaTon,
        Integer rendimentoMedioKgHa
) {
    public HistoricoPamCultura {
        Objects.requireNonNull(culturaId, "culturaId é obrigatório");
    }

    public static HistoricoPamCultura semHistorico(Long culturaId) {
        return new HistoricoPamCultura(culturaId, false, null, null, null, null);
    }

    public boolean possuiProdutividadeApurada() {
        return possuiHistorico && rendimentoMedioKgHa != null && rendimentoMedioKgHa > 0;
    }
}
