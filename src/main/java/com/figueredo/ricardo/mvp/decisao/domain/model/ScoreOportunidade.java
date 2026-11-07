package com.figueredo.ricardo.mvp.decisao.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public record ScoreOportunidade(
        Long culturaId,
        String nomeCultura,
        String nomeCientifico,
        String grupoAgricola,
        int posicaoRanking,
        int notaFinal,
        PontuacaoCriterios pontosPorCriterio,
        Integer riscoMinimoZarc,
        int quantidadeDecendiosFavoraveis,
        int quantidadeSolosAptos,
        int quantidadeCiclosAptos,
        String produtoReferencia,
        BigDecimal precoAtualReferencia,
        String unidadeComercializacao,
        LocalDate dataUltimaCotacao,
        BigDecimal variacao30DiasPercentual,
        BigDecimal variacao90DiasPercentual,
        BigDecimal variacao180DiasPercentual,
        String pracaReferencia,
        String fonteReferencia,
        boolean fallbackEstadual,
        boolean possuiHistoricoPam,
        Integer ultimoAnoPam,
        Integer rendimentoMedioPamKgHa,
        List<String> justificativas,
        List<String> avisosMetodologicos
) {
    public ScoreOportunidade {
        Objects.requireNonNull(culturaId, "culturaId é obrigatório");
        if (nomeCultura == null || nomeCultura.isBlank()) {
            throw new IllegalArgumentException("nomeCultura é obrigatório");
        }
        if (posicaoRanking < 0) {
            throw new IllegalArgumentException("posicaoRanking não pode ser negativa");
        }
        if (notaFinal < 0 || notaFinal > 100) {
            throw new IllegalArgumentException("notaFinal deve estar entre 0 e 100");
        }
        Objects.requireNonNull(pontosPorCriterio, "pontosPorCriterio é obrigatório");
        Objects.requireNonNull(riscoMinimoZarc, "riscoMinimoZarc é obrigatório");
        justificativas = justificativas != null ? List.copyOf(justificativas) : List.of();
        avisosMetodologicos = avisosMetodologicos != null ? List.copyOf(avisosMetodologicos) : List.of();
    }

    public ScoreOportunidade comPosicaoRanking(int novaPosicao) {
        if (novaPosicao <= 0) {
            throw new IllegalArgumentException("novaPosicao deve ser maior que zero");
        }
        return new ScoreOportunidade(
                culturaId,
                nomeCultura,
                nomeCientifico,
                grupoAgricola,
                novaPosicao,
                notaFinal,
                pontosPorCriterio,
                riscoMinimoZarc,
                quantidadeDecendiosFavoraveis,
                quantidadeSolosAptos,
                quantidadeCiclosAptos,
                produtoReferencia,
                precoAtualReferencia,
                unidadeComercializacao,
                dataUltimaCotacao,
                variacao30DiasPercentual,
                variacao90DiasPercentual,
                variacao180DiasPercentual,
                pracaReferencia,
                fonteReferencia,
                fallbackEstadual,
                possuiHistoricoPam,
                ultimoAnoPam,
                rendimentoMedioPamKgHa,
                justificativas,
                avisosMetodologicos
        );
    }
}
