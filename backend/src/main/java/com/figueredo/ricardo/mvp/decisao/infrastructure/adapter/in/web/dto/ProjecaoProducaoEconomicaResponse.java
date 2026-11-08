package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record ProjecaoProducaoEconomicaResponse(
        BigDecimal areaInformadaHa,
        Integer rendimentoHistoricoKgHa,
        BigDecimal pesoEmbalagemKg,
        String unidadeComercializacao,
        BigDecimal produtividadeEstimadaPorHa,
        BigDecimal producaoTotalEstimada,
        BigDecimal precoReferencia,
        BigDecimal receitaBrutaPorHa,
        BigDecimal receitaBrutaTotal,
        String status,
        String avisoMetodologico
) {
}
