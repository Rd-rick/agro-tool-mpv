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
    private static final int MAX_CODIGO_LEN = 20;
    private static final int MAX_DESCRICAO_LEN = 100;
    private static final int MAX_MANEJO_DESC_LEN = 50;
    private static final int MAX_PORTARIA_LEN = 100;

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
        for (Integer risco : decendios) {
            if (risco == null || risco < 0 || risco > 100) {
                throw new IllegalArgumentException("decendios deve conter valores entre 0 e 100");
            }
        }

        cicloCodigo = truncar(cicloCodigo.trim(), MAX_CODIGO_LEN);
        cicloDescricao = truncarOpcional(cicloDescricao, MAX_DESCRICAO_LEN);
        soloCodigo = truncar(soloCodigo.trim(), MAX_CODIGO_LEN);
        soloDescricao = truncarOpcional(soloDescricao, MAX_DESCRICAO_LEN);
        manejoCodigo = truncar(manejoCodigo.trim(), MAX_CODIGO_LEN);
        manejoDescricao = truncarOpcional(manejoDescricao, MAX_MANEJO_DESC_LEN);
        portaria = truncar(portaria.trim(), MAX_PORTARIA_LEN);
        decendios = List.copyOf(decendios);
    }

    private static String truncar(String valor, int limite) {
        return valor.length() > limite ? valor.substring(0, limite) : valor;
    }

    private static String truncarOpcional(String valor, int limite) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        if (limpo.isEmpty()) {
            return null;
        }
        return truncar(limpo, limite);
    }
}
