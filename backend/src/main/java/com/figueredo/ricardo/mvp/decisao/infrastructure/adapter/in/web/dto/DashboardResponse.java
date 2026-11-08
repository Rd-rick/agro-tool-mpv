package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record DashboardResponse(
        MunicipioDashboardResponse municipio,
        String mesInicio,
        String mesFim,
        SoloResumoResponse soloSelecionado,
        Integer safraIni,
        Integer safraFim,
        String safra,
        BigDecimal areaHa,
        OportunidadeDashboardResponse melhorOportunidade,
        List<CulturaPossivelDashboardResponse> culturasPossiveis,
        List<OportunidadeDashboardResponse> rankingOportunidades,
        List<IndicadorRiscoDashboardResponse> indicadoresRisco,
        List<String> avisosGerais
) {
    public record MunicipioDashboardResponse(
            String codigoIbge,
            String nome,
            String uf,
            Long pracaReferenciaId,
            String pracaReferenciaNome,
            String observacaoRegional
    ) {
    }

    public record CulturaPossivelDashboardResponse(
            Long culturaId,
            String nomeCultura,
            String nomeCientifico,
            String grupoAgricola,
            String situacao,
            String epocaPlantio,
            JanelaPlantioResumoResponse melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo,
            SoloResumoResponse soloProjecao,
            Integer riscoMinimo,
            Set<Integer> niveisRiscoDisponiveis,
            List<JanelaPlantioResumoResponse> janelasPlantio,
            List<SoloResumoResponse> solos,
            List<CicloResumoResponse> ciclos
    ) {
    }

    public record JanelaPlantioResumoResponse(
            int decendio,
            int mes,
            String nomeMes,
            String descricaoPeriodo,
            Integer menorRisco,
            Set<Integer> riscosDisponiveis
    ) {
    }

    public record SoloResumoResponse(
            String codigo,
            String tipoSolo,
            String classificacao,
            String teorArgila,
            String aguaDisponivel,
            String descricao
    ) {
    }

    public record CicloResumoResponse(
            String codigo,
            String descricao
    ) {
    }

    public record OportunidadeDashboardResponse(
            Long culturaId,
            String nomeCultura,
            String nomeCientifico,
            String grupoAgricola,
            int posicaoRanking,
            int notaFinal,
            CriteriosPontuacaoResponse criteriosPontuacao,
            Integer riscoMinimoZarc,
            int quantidadeDecendiosFavoraveis,
            int quantidadeSolosAptos,
            int quantidadeCiclosAptos,
            JanelaPlantioResumoResponse melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo,
            SoloResumoResponse soloProjecao,
            List<SoloResumoResponse> solosAptos,
            String produtoReferencia,
            BigDecimal precoAtualReferencia,
            String unidadeMedida,
            LocalDate dataUltimaCotacao,
            BigDecimal variacao30d,
            BigDecimal variacao90d,
            BigDecimal variacao180d,
            String praca,
            String fonte,
            boolean fallbackEstadual,
            boolean possuiHistoricoPam,
            Integer ultimoAnoPam,
            Integer rendimentoMedioPamKgHa,
            ProjecaoProducaoEconomicaResponse projecaoEconomica,
            List<String> justificativas,
            List<String> avisosMetodologicos
    ) {
    }

    public record CriteriosPontuacaoResponse(
            int segurancaClimatica,
            int amplitudeJanela,
            int dinamicaPrecos,
            int potencialReceita,
            int qualidadeInformacao,
            int notaFinal
    ) {
    }

    public record IndicadorRiscoDashboardResponse(
            Long culturaId,
            String nomeCultura,
            Integer riscoMinimo,
            String classificacaoRisco,
            Set<Integer> niveisRiscoDisponiveis,
            int totalDecendiosFavoraveis,
            int decendiosComRisco20,
            int decendiosComRisco30,
            int decendiosComRisco40
    ) {
    }
}
