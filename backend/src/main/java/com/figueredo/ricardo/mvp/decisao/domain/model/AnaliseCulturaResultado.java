package com.figueredo.ricardo.mvp.decisao.domain.model;

import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record AnaliseCulturaResultado(
        Long culturaId,
        String nomeCultura,
        String nomeCientifico,
        String grupoAgricola,
        ZoneamentoSecao zoneamento,
        MercadoSecao mercado,
        AnaliseSecao analise
) {
    public AnaliseCulturaResultado {
        Objects.requireNonNull(culturaId, "culturaId é obrigatório");
        if (nomeCultura == null || nomeCultura.isBlank()) {
            throw new IllegalArgumentException("nomeCultura é obrigatório");
        }
        Objects.requireNonNull(zoneamento, "zoneamento é obrigatório");
        Objects.requireNonNull(mercado, "mercado é obrigatório");
        Objects.requireNonNull(analise, "analise é obrigatório");
    }

    public record ZoneamentoSecao(
            Municipio municipio,
            Integer safraIni,
            Integer safraFim,
            String safra,
            TipoSoloZarc soloSelecionado,
            boolean aptaZarc,
            String situacao,
            PeriodoDecendio melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo,
            SoloZoneamento soloProjecao,
            Integer riscoMinimo,
            Set<Integer> niveisRiscoDisponiveis,
            List<String> cultivaresENomesOrigem,
            List<CicloZoneamento> ciclos,
            List<SoloZoneamento> tiposSolo,
            List<String> portarias,
            List<PeriodoDecendio> janelasPlantio,
            List<CelulaDecendioMatriz> matriz36Decendios,
            List<ZarcZoneamentoItem> combinacoesDetalhadas
    ) {
        public ZoneamentoSecao(
                Municipio municipio,
                Integer safraIni,
                Integer safraFim,
                String safra,
                TipoSoloZarc soloSelecionado,
                boolean aptaZarc,
                String situacao,
                PeriodoDecendio melhorPeriodoPlantio,
                String recomendacaoMelhorPeriodo,
                Integer riscoMinimo,
                Set<Integer> niveisRiscoDisponiveis,
                List<String> cultivaresENomesOrigem,
                List<CicloZoneamento> ciclos,
                List<SoloZoneamento> tiposSolo,
                List<String> portarias,
                List<PeriodoDecendio> janelasPlantio,
                List<CelulaDecendioMatriz> matriz36Decendios,
                List<ZarcZoneamentoItem> combinacoesDetalhadas
        ) {
            this(
                    municipio,
                    safraIni,
                    safraFim,
                    safra,
                    soloSelecionado,
                    aptaZarc,
                    situacao,
                    melhorPeriodoPlantio,
                    recomendacaoMelhorPeriodo,
                    null,
                    riscoMinimo,
                    niveisRiscoDisponiveis,
                    cultivaresENomesOrigem,
                    ciclos,
                    tiposSolo,
                    portarias,
                    janelasPlantio,
                    matriz36Decendios,
                    combinacoesDetalhadas
            );
        }
    }

    public record CelulaDecendioMatriz(
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

    public record MercadoSecao(
            String produtoReferencia,
            BigDecimal precoReferenciaAtual,
            String unidadeMedidaReferencia,
            LocalDate dataUltimaCotacao,
            String pracaReferencia,
            String fonteReferencia,
            boolean fallbackEstadual,
            String tendencia,
            BigDecimal variacao30d,
            BigDecimal variacao90d,
            BigDecimal variacao180d,
            List<ProdutoMercadoAnalise> cotacoesEpagriCepa,
            List<ProdutoMercadoAnalise> cotacoesConab,
            List<ComparacaoCulturaMunicipioItem> comparacaoDemaisCulturas
    ) {
    }

    public record ComparacaoCulturaMunicipioItem(
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

    public record AnaliseSecao(
            ScoreOportunidade scoreOportunidade,
            String comportamentoPrecosResumo,
            List<MelhorPeriodoComercializacaoItem> melhoresPeriodosComercializacao,
            List<AlertaVariacaoPreco> alertasVariacoesRelevantes,
            ProdutividadeMunicipalAnalise produtividadeMunicipal,
            List<String> avisosMetodologicos
    ) {
    }

    public record MelhorPeriodoComercializacaoItem(
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

    public record AlertaVariacaoPreco(
            String tipoAlerta,
            String severidade,
            String janelaReferencia,
            BigDecimal percentualVariacaoOuOscilacao,
            String descricao
    ) {
    }

    public record ProdutividadeMunicipalAnalise(
            boolean possuiDadosPam,
            Integer ultimoAno,
            Integer ultimoRendimentoMedioKgHa,
            Integer rendimentoMedioHistoricoKgHa,
            Integer ultimaAreaPlantadaHa,
            Integer ultimaQuantidadeProduzidaTon,
            List<HistoricoPamCultura> serieHistoricaAnual,
            String avisoCustosProducao
    ) {
    }
}
