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
    private static final BigDecimal LIMITE_MAXIMO_PRECO = new BigDecimal("9999999999.99");

    public CotacaoPrecoRegistro {
        Objects.requireNonNull(produtoId, "produtoId não pode ser nulo");
        Objects.requireNonNull(pracaId, "pracaId não pode ser nulo");
        Objects.requireNonNull(dataCotacao, "dataCotacao não pode ser nula");
        Objects.requireNonNull(precoReferencia, "precoReferencia não pode ser nulo");
        Objects.requireNonNull(frequencia, "frequencia não pode ser nula");

        if (precoReferencia.compareTo(BigDecimal.ZERO) < 0 || precoReferencia.compareTo(LIMITE_MAXIMO_PRECO) > 0) {
            throw new IllegalArgumentException("precoReferencia fora do intervalo permitido");
        }
        if (precoMinimo != null && (precoMinimo.compareTo(BigDecimal.ZERO) < 0 || precoMinimo.compareTo(LIMITE_MAXIMO_PRECO) > 0)) {
            throw new IllegalArgumentException("precoMinimo fora do intervalo permitido");
        }
        if (precoMaximo != null && (precoMaximo.compareTo(BigDecimal.ZERO) < 0 || precoMaximo.compareTo(LIMITE_MAXIMO_PRECO) > 0)) {
            throw new IllegalArgumentException("precoMaximo fora do intervalo permitido");
        }
    }
}
