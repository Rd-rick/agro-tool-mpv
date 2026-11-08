package com.figueredo.ricardo.mvp.decisao.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record ProjecaoProducaoEconomica(
        BigDecimal areaInformadaHa,
        Integer rendimentoHistoricoKgHa,
        BigDecimal pesoEmbalagemKg,
        String unidadeComercializacao,
        BigDecimal produtividadeEstimadaPorHa,
        BigDecimal producaoTotalEstimada,
        BigDecimal precoReferencia,
        BigDecimal receitaBrutaPorHa,
        BigDecimal receitaBrutaTotal,
        StatusProjecaoProducao status,
        String avisoMetodologico
) {
    public ProjecaoProducaoEconomica {
        Objects.requireNonNull(status, "status é obrigatório");
        Objects.requireNonNull(avisoMetodologico, "avisoMetodologico é obrigatório");
    }

    public static ProjecaoProducaoEconomica calculada(
            BigDecimal areaInformadaHa,
            Integer rendimentoHistoricoKgHa,
            BigDecimal pesoEmbalagemKg,
            String unidadeComercializacao,
            BigDecimal produtividadeEstimadaPorHa,
            BigDecimal producaoTotalEstimada,
            BigDecimal precoReferencia,
            BigDecimal receitaBrutaPorHa,
            BigDecimal receitaBrutaTotal,
            String avisoMetodologico
    ) {
        return new ProjecaoProducaoEconomica(
                areaInformadaHa,
                rendimentoHistoricoKgHa,
                pesoEmbalagemKg,
                unidadeComercializacao,
                produtividadeEstimadaPorHa,
                producaoTotalEstimada,
                precoReferencia,
                receitaBrutaPorHa,
                receitaBrutaTotal,
                StatusProjecaoProducao.CALCULADO_COM_SUCESSO,
                avisoMetodologico
        );
    }

    public static ProjecaoProducaoEconomica indisponivel(
            BigDecimal areaInformadaHa,
            StatusProjecaoProducao status,
            String avisoMetodologico
    ) {
        return new ProjecaoProducaoEconomica(
                areaInformadaHa,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                status,
                avisoMetodologico
        );
    }
}
