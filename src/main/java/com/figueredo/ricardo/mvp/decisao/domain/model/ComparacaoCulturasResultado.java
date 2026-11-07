package com.figueredo.ricardo.mvp.decisao.domain.model;

import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record ComparacaoCulturasResultado(
        Municipio municipio,
        String pracaRegionalMunicipio,
        TipoSoloZarc soloSelecionado,
        Integer safraIni,
        Integer safraFim,
        String safra,
        List<ItemComparacaoCultura> culturasComparadas,
        List<String> avisosComparacao
) {
    public ComparacaoCulturasResultado {
        Objects.requireNonNull(municipio, "municipio é obrigatório");
        culturasComparadas = culturasComparadas != null ? List.copyOf(culturasComparadas) : List.of();
        avisosComparacao = avisosComparacao != null ? List.copyOf(avisosComparacao) : List.of();
    }

    public record ItemComparacaoCultura(
            Long culturaId,
            String nomeCultura,
            String nomeCientifico,
            String grupoAgricola,
            ViabilidadeZoneamentoComparacao viabilidadeZoneamento,
            JanelaPlantioComparacao janelasPlantio,
            RiscoClimaticoComparacao riscoClimatico,
            MercadoComparacao precosEVariacao,
            ScoreMotorDecisaoComparacao scoreMotorDecisao,
            IndicadoresProdutivosComparacao indicadoresProdutivos
    ) {
    }

    public record ViabilidadeZoneamentoComparacao(
            boolean aptaZarc,
            String situacao,
            int quantidadeSolosAptos,
            int quantidadeCiclosAptos,
            List<SoloZoneamento> tiposSolo,
            List<CicloZoneamento> ciclos
    ) {
    }

    public record JanelaPlantioComparacao(
            int quantidadeDecendiosFavoraveis,
            String epocaPlantioResumo,
            PeriodoDecendio melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo,
            SoloZoneamento soloProjecao,
            List<PeriodoDecendio> periodosFavoraveis
    ) {
        public JanelaPlantioComparacao(
                int quantidadeDecendiosFavoraveis,
                String epocaPlantioResumo,
                PeriodoDecendio melhorPeriodoPlantio,
                String recomendacaoMelhorPeriodo,
                List<PeriodoDecendio> periodosFavoraveis
        ) {
            this(
                    quantidadeDecendiosFavoraveis,
                    epocaPlantioResumo,
                    melhorPeriodoPlantio,
                    recomendacaoMelhorPeriodo,
                    null,
                    periodosFavoraveis
            );
        }
    }

    public record RiscoClimaticoComparacao(
            Integer riscoMinimo,
            String classificacaoRisco,
            Set<Integer> niveisRiscoDisponiveis,
            int decendiosRisco20,
            int decendiosRisco30,
            int decendiosRisco40
    ) {
    }

    public record MercadoComparacao(
            String produtoReferencia,
            BigDecimal precoAtualReferencia,
            String unidadeComercializacao,
            LocalDate dataUltimaCotacao,
            String pracaReferencia,
            String fonteReferencia,
            boolean fallbackEstadual,
            BigDecimal variacao30d,
            BigDecimal variacao90d,
            BigDecimal variacao180d,
            List<ProdutoMercadoAnalise> cotacoesEpagriCepa,
            List<ProdutoMercadoAnalise> cotacoesConab
    ) {
    }

    public record ScoreMotorDecisaoComparacao(
            boolean elegivelRanking,
            Integer posicaoRankingMunicipio,
            int notaFinal,
            PontuacaoCriterios pontosPorCriterio,
            List<String> justificativas
    ) {
    }

    public record IndicadoresProdutivosComparacao(
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
