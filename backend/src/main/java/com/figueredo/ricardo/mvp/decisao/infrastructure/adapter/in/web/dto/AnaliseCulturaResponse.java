package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record AnaliseCulturaResponse(
        Long culturaId,
        String nomeCultura,
        String nomeCientifico,
        String grupoAgricola,
        ZoneamentoAnaliseResponse zoneamento,
        MercadoAnaliseResponse mercado,
        AnaliseDetalhadaResponse analise
) {
    public record ZoneamentoAnaliseResponse(
            MunicipioResumoResponse municipio,
            Integer safraIni,
            Integer safraFim,
            String safra,
            SoloItemResponse soloSelecionado,
            boolean aptaZarc,
            String situacao,
            JanelaPlantioDetalhadaResponse melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo,
            SoloItemResponse soloProjecao,
            Integer riscoMinimo,
            Set<Integer> niveisRiscoDisponiveis,
            List<String> cultivaresENomesOrigem,
            List<CicloItemResponse> ciclos,
            List<SoloItemResponse> tiposSolo,
            List<String> portarias,
            List<JanelaPlantioDetalhadaResponse> janelasPlantio,
            List<DecendioMatrizResponse> matrizDecendios,
            List<CombinacaoZarcDetalhadaResponse> combinacoesDetalhadas
    ) {
    }

    public record MunicipioResumoResponse(
            String codigoIbge,
            String nome,
            String uf,
            Long pracaReferenciaId,
            String observacaoPraca
    ) {
    }

    public record CicloItemResponse(
            String codigo,
            String descricao
    ) {
    }

    public record SoloItemResponse(
            String codigo,
            String tipoSolo,
            String classificacao,
            String teorArgila,
            String descricao
    ) {
    }

    public record JanelaPlantioDetalhadaResponse(
            int decendio,
            int mes,
            String nomeMes,
            int decendioNoMes,
            int diaInicio,
            int diaFim,
            String descricaoPeriodo,
            Integer menorRisco,
            Set<Integer> riscosDisponiveis
    ) {
    }

    public record DecendioMatrizResponse(
            int decendio,
            int mes,
            String nomeMes,
            int decendioNoMes,
            int diaInicio,
            int diaFim,
            String descricaoPeriodo,
            boolean favoravel,
            Integer menorRisco,
            Set<Integer> riscosDisponiveis
    ) {
    }

    public record CombinacaoZarcDetalhadaResponse(
            String nomeOrigemZarc,
            String cicloCodigo,
            String cicloDescricao,
            String soloCodigo,
            String soloDescricao,
            String manejoCodigo,
            String manejoDescricao,
            String portaria,
            Integer riscoMinimo,
            List<Integer> decendios
    ) {
    }

    public record MercadoAnaliseResponse(
            String produtoReferencia,
            BigDecimal precoReferenciaAtual,
            String unidadeMedida,
            LocalDate dataUltimaCotacao,
            String pracaReferencia,
            String fonteReferencia,
            boolean fallbackEstadual,
            String tendencia,
            BigDecimal variacao30d,
            BigDecimal variacao90d,
            BigDecimal variacao180d,
            List<ProdutoFonteCotacaoResponse> cotacoesEpagriCepa,
            List<ProdutoFonteCotacaoResponse> cotacoesConab,
            List<ComparacaoCulturaMunicipioResponse> comparacaoOutrasCulturas
    ) {
    }

    public record ProdutoFonteCotacaoResponse(
            Long produtoId,
            String nomeComercial,
            String especificacao,
            String unidadeComercializacao,
            BigDecimal pesoEmbalagemKg,
            String frequenciaUtilizada,
            LocalDate dataUltimaCotacao,
            BigDecimal precoAtualReferencia,
            BigDecimal variacao30d,
            BigDecimal variacao90d,
            BigDecimal variacao180d,
            OrigemCotacaoResponse origem,
            SeriePeriodoResponse serie30Dias,
            SeriePeriodoResponse serie90Dias,
            SeriePeriodoResponse serie180Dias
    ) {
    }

    public record OrigemCotacaoResponse(
            String fonte,
            Long pracaId,
            String pracaNome,
            boolean fallbackEstadual,
            String observacaoOrigem
    ) {
    }

    public record SeriePeriodoResponse(
            int diasJanela,
            LocalDate dataInicio,
            LocalDate dataFim,
            BigDecimal precoInicial,
            BigDecimal precoFinal,
            BigDecimal precoMinimoPeriodo,
            BigDecimal precoMaximoPeriodo,
            BigDecimal precoMedioPeriodo,
            BigDecimal variacaoPercentual,
            List<PontoGraficoPrecoResponse> pontos
    ) {
    }

    public record PontoGraficoPrecoResponse(
            LocalDate data,
            BigDecimal precoMinimo,
            BigDecimal precoReferencia,
            BigDecimal precoMaximo
    ) {
    }

    public record ComparacaoCulturaMunicipioResponse(
            Long culturaId,
            String nomeCultura,
            int posicaoRanking,
            int notaFinal,
            Integer riscoMinimoZarc,
            String recomendacaoMelhorPeriodo,
            String produtoReferencia,
            BigDecimal precoAtualReferencia,
            String unidadeMedida,
            String praca,
            String fonte,
            BigDecimal variacao30d,
            BigDecimal variacao90d,
            BigDecimal variacao180d
    ) {
    }

    public record AnaliseDetalhadaResponse(
            ScoreResumoAnaliseResponse scoreOportunidade,
            String comportamentoPrecosUltimosMeses,
            List<MelhorPeriodoComercializacaoResponse> melhoresPeriodosComercializacao,
            List<AlertaVariacaoResponse> alertasVariacoesRelevantes,
            ProdutividadeMunicipalResponse produtividadeMunicipal,
            List<String> avisosMetodologicos
    ) {
    }

    public record ScoreResumoAnaliseResponse(
            int posicaoRanking,
            int notaFinal,
            int segurancaClimatica,
            int amplitudeJanela,
            int dinamicaPrecos,
            int qualidadeInformacao,
            JanelaPlantioDetalhadaResponse melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo,
            List<String> justificativas
    ) {
    }

    public record MelhorPeriodoComercializacaoResponse(
            int ano,
            int mes,
            String nomeMes,
            BigDecimal precoMedioReferencia,
            BigDecimal precoMaximoObservado,
            int quantidadeCotacoes,
            String produto,
            String unidadeMedida,
            String fonte
    ) {
    }

    public record AlertaVariacaoResponse(
            String tipoAlerta,
            String severidade,
            String janelaReferencia,
            BigDecimal percentualVariacaoOuOscilacao,
            String descricao
    ) {
    }

    public record ProdutividadeMunicipalResponse(
            boolean possuiDadosPam,
            Integer ultimoAno,
            Integer ultimoRendimentoMedioKgHa,
            Integer rendimentoMedioHistoricoKgHa,
            Integer ultimaAreaPlantadaHa,
            Integer ultimaQuantidadeProduzidaTon,
            List<RegistroAnualPamResponse> serieHistoricaAnual,
            String avisoCustosProducao
    ) {
    }

    public record RegistroAnualPamResponse(
            Integer ano,
            Integer areaPlantadaHa,
            Integer quantidadeProduzidaTon,
            Integer rendimentoMedioKgHa
    ) {
    }
}
