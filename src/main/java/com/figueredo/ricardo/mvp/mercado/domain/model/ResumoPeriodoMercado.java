package com.figueredo.ricardo.mvp.mercado.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record ResumoPeriodoMercado(
        int dias,
        LocalDate dataInicio,
        LocalDate dataFim,
        BigDecimal precoInicial,
        BigDecimal precoFinal,
        BigDecimal precoMinimoPeriodo,
        BigDecimal precoMaximoPeriodo,
        BigDecimal precoMedioPeriodo,
        BigDecimal variacaoPercentual,
        List<PontoSerieHistorica> serieHistorica
) {
    public ResumoPeriodoMercado {
        if (dias <= 0) {
            throw new IllegalArgumentException("dias deve ser positivo");
        }
        Objects.requireNonNull(dataInicio, "dataInicio é obrigatória");
        Objects.requireNonNull(dataFim, "dataFim é obrigatória");
        serieHistorica = serieHistorica != null ? List.copyOf(serieHistorica) : List.of();
    }
}
