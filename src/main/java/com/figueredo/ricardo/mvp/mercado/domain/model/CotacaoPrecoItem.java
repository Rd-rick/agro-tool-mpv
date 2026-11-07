package com.figueredo.ricardo.mvp.mercado.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CotacaoPrecoItem(
        Long produtoId,
        Long pracaId,
        LocalDate dataCotacao,
        BigDecimal precoMinimo,
        BigDecimal precoReferencia,
        BigDecimal precoMaximo,
        String frequencia
) {
}
