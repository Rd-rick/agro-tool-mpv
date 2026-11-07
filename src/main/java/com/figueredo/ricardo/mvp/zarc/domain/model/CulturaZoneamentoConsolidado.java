package com.figueredo.ricardo.mvp.zarc.domain.model;

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
        List<String> portarias
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
    }
}
