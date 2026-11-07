package com.figueredo.ricardo.mvp.decisao.domain.model;

import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record DashboardResultado(
        MunicipioDashboard municipio,
        String mesInicio,
        String mesFim,
        TipoSoloZarc soloSelecionado,
        Integer safraIni,
        Integer safraFim,
        String safra,
        BigDecimal areaHa,
        ScoreOportunidade melhorOportunidade,
        List<CulturaPossivelDashboard> culturasPossiveis,
        List<ScoreOportunidade> rankingOportunidades,
        List<IndicadorRiscoCultura> indicadoresRisco,
        List<String> avisosGerais
) {
    public DashboardResultado {
        Objects.requireNonNull(municipio, "municipio é obrigatório");
        culturasPossiveis = culturasPossiveis != null ? List.copyOf(culturasPossiveis) : List.of();
        rankingOportunidades = rankingOportunidades != null ? List.copyOf(rankingOportunidades) : List.of();
        indicadoresRisco = indicadoresRisco != null ? List.copyOf(indicadoresRisco) : List.of();
        avisosGerais = avisosGerais != null ? List.copyOf(avisosGerais) : List.of();
        if (melhorOportunidade == null && !rankingOportunidades.isEmpty()) {
            melhorOportunidade = rankingOportunidades.get(0);
        }
    }

    public record MunicipioDashboard(
            String codigoIbge,
            String nome,
            String uf,
            Long pracaReferenciaId,
            String pracaReferenciaNome,
            String observacaoRegional
    ) {
    }

    public record CulturaPossivelDashboard(
            Long culturaId,
            String nomeCultura,
            String nomeCientifico,
            String grupoAgricola,
            String situacao,
            String epocaPlantioResumo,
            PeriodoDecendio melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo,
            SoloZoneamento soloProjecao,
            Integer riscoMinimo,
            Set<Integer> niveisRiscoDisponiveis,
            List<PeriodoDecendio> janelasPlantio,
            List<SoloZoneamento> solos,
            List<CicloZoneamento> ciclos
    ) {
        public CulturaPossivelDashboard(
                Long culturaId,
                String nomeCultura,
                String nomeCientifico,
                String grupoAgricola,
                String situacao,
                String epocaPlantioResumo,
                PeriodoDecendio melhorPeriodoPlantio,
                String recomendacaoMelhorPeriodo,
                Integer riscoMinimo,
                Set<Integer> niveisRiscoDisponiveis,
                List<PeriodoDecendio> janelasPlantio,
                List<SoloZoneamento> solos,
                List<CicloZoneamento> ciclos
        ) {
            this(
                    culturaId,
                    nomeCultura,
                    nomeCientifico,
                    grupoAgricola,
                    situacao,
                    epocaPlantioResumo,
                    melhorPeriodoPlantio,
                    recomendacaoMelhorPeriodo,
                    null,
                    riscoMinimo,
                    niveisRiscoDisponiveis,
                    janelasPlantio,
                    solos,
                    ciclos
            );
        }
    }

    public record IndicadorRiscoCultura(
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
