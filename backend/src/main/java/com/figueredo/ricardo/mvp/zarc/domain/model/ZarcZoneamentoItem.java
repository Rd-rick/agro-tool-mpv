package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.List;
import java.util.Objects;

public record ZarcZoneamentoItem(
        Long culturaId,
        String nomeCultura,
        String nomeCientifico,
        String grupoAgricola,
        String nomeOrigemZarc,
        Integer safraIni,
        Integer safraFim,
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
    public ZarcZoneamentoItem {
        Objects.requireNonNull(culturaId, "culturaId é obrigatório");
        if (nomeCultura == null || nomeCultura.isBlank()) {
            throw new IllegalArgumentException("nomeCultura é obrigatório");
        }
        Objects.requireNonNull(safraIni, "safraIni é obrigatório");
        Objects.requireNonNull(safraFim, "safraFim é obrigatório");
        if (decendios == null || decendios.size() != 36) {
            throw new IllegalArgumentException("decendios deve conter 36 elementos");
        }
        decendios = List.copyOf(decendios);
    }
}
