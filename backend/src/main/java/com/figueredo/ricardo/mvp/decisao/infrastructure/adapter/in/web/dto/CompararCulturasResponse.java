package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record CompararCulturasResponse(
        MunicipioComparadorResponse municipio,
        SoloComparadoResponse soloSelecionado,
        Integer safraIni,
        Integer safraFim,
        String safra,
        List<CulturaComparadaResponse> culturas,
        List<String> avisosComparacao
) {
    public record MunicipioComparadorResponse(
            String codigoIbge,
            String nome,
            String uf,
            Long pracaReferenciaId,
            String pracaRegionalMunicipio,
            String observacaoPraca
    ) {
    }

    public record CulturaComparadaResponse(
            Long culturaId,
            String nomeCultura,
            String nomeCientifico,
            String grupoAgricola,
            ViabilidadeZoneamentoResponse viabilidadeZoneamento,
            JanelaPlantioComparadaResponse janelasPlantio,
            RiscoClimaticoComparadoResponse riscoClimatico,
            PrecosEVariacaoComparadaResponse precosEVariacao,
            ScoreComparadoResponse scoreMotorDecisao,
            IndicadoresProdutivosResponse indicadoresProdutivos
    ) {
    }

    public record ViabilidadeZoneamentoResponse(
            boolean aptaZarc,
            String situacao,
            int quantidadeSolosAptos,
            int quantidadeCiclosAptos,
            List<SoloComparadoResponse> tiposSolo,
            List<CicloComparadoResponse> ciclos
    ) {
    }

    public record SoloComparadoResponse(
            String codigo,
            String tipoSolo,
            String classificacao,
            String teorArgila,
            String aguaDisponivel,
            String descricao
    ) {
    }

    public record CicloComparadoResponse(
            String codigo,
            String descricao
    ) {
    }

    public record JanelaPlantioComparadaResponse(
            int quantidadeDecendiosFavoraveis,
            String epocaPlantioResumo,
            DecendioFavoravelComparadoResponse melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo,
            SoloComparadoResponse soloProjecao,
            List<DecendioFavoravelComparadoResponse> periodosFavoraveis
    ) {
    }

    public record DecendioFavoravelComparadoResponse(
            int decendio,
            int mes,
            String nomeMes,
            String descricaoPeriodo,
            Integer menorRisco,
            Set<Integer> riscosDisponiveis
    ) {
    }

    public record RiscoClimaticoComparadoResponse(
            Integer riscoMinimo,
            String classificacaoRisco,
            Set<Integer> niveisRiscoDisponiveis,
            int decendiosRisco20,
            int decendiosRisco30,
            int decendiosRisco40
    ) {
    }

    public record PrecosEVariacaoComparadaResponse(
            String produtoReferencia,
            BigDecimal precoAtualReferencia,
            String unidadeMedida,
            LocalDate dataUltimaCotacao,
            String pracaReferencia,
            String fonteReferencia,
            boolean fallbackEstadual,
            BigDecimal variacao30d,
            BigDecimal variacao90d,
            BigDecimal variacao180d,
            List<CotacaoFonteResumoResponse> cotacoesEpagriCepa,
            List<CotacaoFonteResumoResponse> cotacoesConab
    ) {
    }

    public record CotacaoFonteResumoResponse(
            Long produtoId,
            String nomeComercial,
            String especificacao,
            String unidadeComercializacao,
            BigDecimal pesoEmbalagemKg,
            String fonte,
            String praca,
            boolean fallbackEstadual,
            LocalDate dataUltimaCotacao,
            BigDecimal precoAtualReferencia,
            BigDecimal variacao30d,
            BigDecimal variacao90d,
            BigDecimal variacao180d
    ) {
    }

    public record ScoreComparadoResponse(
            boolean elegivelRanking,
            Integer posicaoRankingMunicipio,
            int notaFinal,
            int segurancaClimatica,
            int amplitudeJanela,
            int dinamicaPrecos,
            int potencialReceita,
            int qualidadeInformacao,
            List<String> justificativas
    ) {
    }

    public record IndicadoresProdutivosResponse(
            boolean possuiHistoricoPam,
            Integer ultimoAnoPam,
            Integer ultimoRendimentoMedioKgHa,
            Integer rendimentoMedioHistoricoKgHa,
            Integer ultimaAreaPlantadaHa,
            Integer ultimaQuantidadeProduzidaTon,
            String avisoPotencialEconomico
    ) {
    }
}
