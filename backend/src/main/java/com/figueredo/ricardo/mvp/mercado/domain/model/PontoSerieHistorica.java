package com.figueredo.ricardo.mvp.mercado.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record PontoSerieHistorica(
        LocalDate data,
        BigDecimal precoMinimo,
        BigDecimal precoReferencia,
        BigDecimal precoMaximo
) {
    public PontoSerieHistorica {
        Objects.requireNonNull(data, "data é obrigatória");
        Objects.requireNonNull(precoReferencia, "precoReferencia é obrigatório");
    }
}
