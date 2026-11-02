package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.List;
import java.util.Objects;

public record ZarcRegistro(
        Long municipioId,
        Long culturaCodigoFonteId,
        Integer safraIni,
        Integer safraFim,
        String cicloCodigo,
        String cicloDescricao,
        String soloCodigo,
        String soloDescricao,
        String manejoCodigo,
        String manejoDescricao,
        String portaria,
        List<Integer> decendios
) {
    public ZarcRegistro {
        Objects.requireNonNull(municipioId, "municipioId é obrigatório");
        Objects.requireNonNull(culturaCodigoFonteId, "culturaCodigoFonteId é obrigatório");
        Objects.requireNonNull(safraIni, "safraIni é obrigatório");
        Objects.requireNonNull(safraFim, "safraFim é obrigatório");
        if (cicloCodigo == null || cicloCodigo.isBlank()) {
            throw new IllegalArgumentException("cicloCodigo é obrigatório");
        }
        if (soloCodigo == null || soloCodigo.isBlank()) {
            throw new IllegalArgumentException("soloCodigo é obrigatório");
        }
        if (manejoCodigo == null || manejoCodigo.isBlank()) {
            throw new IllegalArgumentException("manejoCodigo é obrigatório");
        }
        if (portaria == null || portaria.isBlank()) {
            throw new IllegalArgumentException("portaria é obrigatório");
        }
        if (decendios == null || decendios.size() != 36) {
            throw new IllegalArgumentException("decendios deve conter exatamente 36 elementos");
        }
        decendios = List.copyOf(decendios);
    }
}
