package com.figueredo.ricardo.mvp.epagri.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record CotacaoPrecoRegistro(
        Long produtoId,
        Long pracaId,
        LocalDate dataCotacao,
        BigDecimal precoMinimo,
        BigDecimal precoReferencia,
        BigDecimal precoMaximo,
        FrequenciaCotacao frequencia
) {
    public CotacaoPrecoRegistro {
        Objects.requireNonNull(produtoId, "produtoId não pode ser nulo");
        Objects.requireNonNull(pracaId, "pracaId não pode ser nulo");
        Objects.requireNonNull(dataCotacao, "dataCotacao não pode ser nula");
        Objects.requireNonNull(precoReferencia, "precoReferencia não pode ser nulo");
        Objects.requireNonNull(frequencia, "frequencia não pode ser nula");

        if (precoReferencia.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("precoReferencia não pode ser negativo");
        }
        if (precoMinimo != null && precoMinimo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("precoMinimo não pode ser negativo");
        }
        if (precoMaximo != null && precoMaximo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("precoMaximo não pode ser negativo");
        }
    }
}
