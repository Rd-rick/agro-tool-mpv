package com.figueredo.ricardo.mvp.pam.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

public record PamProducaoRegistro(
        Long municipioId,
        Long culturaCodigoFonteId,
        Integer ano,
        Integer areaPlantadaHa,
        Integer areaColhidaHa,
        Integer quantidadeProduzidaTon,
        Integer rendimentoMedioKgHa,
        BigDecimal valorProducaoMilReais
) {
    private static final int ANO_MINIMO = 1974;
    private static final int ANO_MAXIMO = 2100;

    public PamProducaoRegistro {
        Objects.requireNonNull(municipioId, "municipioId é obrigatório");
        Objects.requireNonNull(culturaCodigoFonteId, "culturaCodigoFonteId é obrigatório");
        Objects.requireNonNull(ano, "ano é obrigatório");

        if (ano < ANO_MINIMO || ano > ANO_MAXIMO) {
            throw new IllegalArgumentException("ano fora do intervalo permitido: " + ano);
        }
        if (areaPlantadaHa != null && areaPlantadaHa < 0) {
            throw new IllegalArgumentException("areaPlantadaHa não pode ser negativa");
        }
        if (areaColhidaHa != null && areaColhidaHa < 0) {
            throw new IllegalArgumentException("areaColhidaHa não pode ser negativa");
        }
        if (quantidadeProduzidaTon != null && quantidadeProduzidaTon < 0) {
            throw new IllegalArgumentException("quantidadeProduzidaTon não pode ser negativa");
        }
        if (rendimentoMedioKgHa != null && rendimentoMedioKgHa < 0) {
            throw new IllegalArgumentException("rendimentoMedioKgHa não pode ser negativo");
        }
        if (valorProducaoMilReais != null && valorProducaoMilReais.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("valorProducaoMilReais não pode ser negativo");
        }
    }

    public boolean possuiProducaoOuPlantio() {
        return maiorQueZero(areaPlantadaHa)
                || maiorQueZero(areaColhidaHa)
                || maiorQueZero(quantidadeProduzidaTon)
                || maiorQueZero(rendimentoMedioKgHa)
                || (valorProducaoMilReais != null && valorProducaoMilReais.compareTo(BigDecimal.ZERO) > 0);
    }

    private static boolean maiorQueZero(Integer valor) {
        return valor != null && valor > 0;
    }
}
