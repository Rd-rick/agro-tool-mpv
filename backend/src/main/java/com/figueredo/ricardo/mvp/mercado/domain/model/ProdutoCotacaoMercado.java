package com.figueredo.ricardo.mvp.mercado.domain.model;

import java.math.BigDecimal;

public record ProdutoCotacaoMercado(
        Long id,
        Long culturaId,
        String nomeComercial,
        String especificacao,
        String unidadeComercializacao,
        BigDecimal pesoEmbalagemKg,
        String fonte
) {
}
