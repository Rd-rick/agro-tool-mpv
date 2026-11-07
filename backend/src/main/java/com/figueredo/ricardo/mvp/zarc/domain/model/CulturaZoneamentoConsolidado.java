package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record CulturaZoneamentoConsolidado(
        Long culturaId,
        String nomeCultura,
        String nomeCientifico,
        String grupoAgricola,
        List<String> nomesOrigemZarc,
        Integer riscoMinimo,
        Set<Integer> niveisRiscoDisponiveis,
        List<PeriodoDecendio> janelasPlantio,
        List<SoloZoneamento> tiposSolo,
        List<CicloZoneamento> ciclos,
        List<String> portarias,
        PeriodoDecendio melhorPeriodoPlantio,
        String recomendacaoMelhorPeriodo,
        SoloZoneamento soloProjecao
) {
    public CulturaZoneamentoConsolidado {
        Objects.requireNonNull(culturaId, "culturaId é obrigatório");
        if (nomeCultura == null || nomeCultura.isBlank()) {
            throw new IllegalArgumentException("nomeCultura é obrigatório");
        }
        Objects.requireNonNull(riscoMinimo, "riscoMinimo é obrigatório");
        nomesOrigemZarc = nomesOrigemZarc != null ? List.copyOf(nomesOrigemZarc) : List.of();
        niveisRiscoDisponiveis = niveisRiscoDisponiveis != null
                ? Set.copyOf(new TreeSet<>(niveisRiscoDisponiveis))
                : Set.of();
        janelasPlantio = janelasPlantio != null ? List.copyOf(janelasPlantio) : List.of();
        tiposSolo = tiposSolo != null ? List.copyOf(tiposSolo) : List.of();
        ciclos = ciclos != null ? List.copyOf(ciclos) : List.of();
        portarias = portarias != null ? List.copyOf(portarias) : List.of();
        if (soloProjecao == null && !tiposSolo.isEmpty()) {
            soloProjecao = tiposSolo.get(0);
        }
        if (melhorPeriodoPlantio == null && !janelasPlantio.isEmpty()) {
            melhorPeriodoPlantio = janelasPlantio.stream()
                    .filter(p -> p.menorRisco() != null && p.menorRisco() > 0)
                    .min(Comparator.comparingInt(PeriodoDecendio::menorRisco)
                            .thenComparingInt(PeriodoDecendio::decendio))
                    .orElse(null);
        }
        if ((recomendacaoMelhorPeriodo == null || recomendacaoMelhorPeriodo.isBlank()) && melhorPeriodoPlantio != null) {
            String descSolo = soloProjecao != null
                    ? soloProjecao.tipoSolo()
                    : (!tiposSolo.isEmpty() ? tiposSolo.get(0).tipoSolo() : "o solo selecionado");
            recomendacaoMelhorPeriodo = melhorPeriodoPlantio.formatarRecomendacaoSolo(descSolo);
        }
    }

    public CulturaZoneamentoConsolidado(
            Long culturaId,
            String nomeCultura,
            String nomeCientifico,
            String grupoAgricola,
            List<String> nomesOrigemZarc,
            Integer riscoMinimo,
            Set<Integer> niveisRiscoDisponiveis,
            List<PeriodoDecendio> janelasPlantio,
            List<SoloZoneamento> tiposSolo,
            List<CicloZoneamento> ciclos,
            List<String> portarias,
            PeriodoDecendio melhorPeriodoPlantio,
            String recomendacaoMelhorPeriodo
    ) {
        this(
                culturaId,
                nomeCultura,
                nomeCientifico,
                grupoAgricola,
                nomesOrigemZarc,
                riscoMinimo,
                niveisRiscoDisponiveis,
                janelasPlantio,
                tiposSolo,
                ciclos,
                portarias,
                melhorPeriodoPlantio,
                recomendacaoMelhorPeriodo,
                null
        );
    }

    public CulturaZoneamentoConsolidado(
            Long culturaId,
            String nomeCultura,
            String nomeCientifico,
            String grupoAgricola,
            List<String> nomesOrigemZarc,
            Integer riscoMinimo,
            Set<Integer> niveisRiscoDisponiveis,
            List<PeriodoDecendio> janelasPlantio,
            List<SoloZoneamento> tiposSolo,
            List<CicloZoneamento> ciclos,
            List<String> portarias
    ) {
        this(
                culturaId,
                nomeCultura,
                nomeCientifico,
                grupoAgricola,
                nomesOrigemZarc,
                riscoMinimo,
                niveisRiscoDisponiveis,
                janelasPlantio,
                tiposSolo,
                ciclos,
                portarias,
                null,
                null,
                null
        );
    }
}
