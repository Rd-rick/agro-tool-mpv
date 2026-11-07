package com.figueredo.ricardo.mvp.mercado.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record ProdutoMercadoAnalise(
        Long produtoId,
        String nomeComercial,
        String especificacao,
        String unidadeComercializacao,
        BigDecimal pesoEmbalagemKg,
        String frequenciaUtilizada,
        LocalDate dataUltimaCotacao,
        BigDecimal precoAtualReferencia,
        BigDecimal variacao30DiasPercentual,
        BigDecimal variacao90DiasPercentual,
        BigDecimal variacao180DiasPercentual,
        OrigemCotacao origem,
        ResumoPeriodoMercado historico30Dias,
        ResumoPeriodoMercado historico90Dias,
        ResumoPeriodoMercado historico180Dias
) {
    public ProdutoMercadoAnalise {
        Objects.requireNonNull(produtoId, "produtoId é obrigatório");
        if (nomeComercial == null || nomeComercial.isBlank()) {
            throw new IllegalArgumentException("nomeComercial é obrigatório");
        }
        if (unidadeComercializacao == null || unidadeComercializacao.isBlank()) {
            throw new IllegalArgumentException("unidadeComercializacao é obrigatória");
        }
        Objects.requireNonNull(origem, "origem é obrigatória");
    }
}
