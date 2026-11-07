package com.figueredo.ricardo.mvp.assistente.domain.model;

import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.math.BigDecimal;

public record ContextoProdutor(
        String codigoIbge,
        PeriodoPlantioMeses periodoPlantio,
        TipoSoloZarc tipoSolo,
        BigDecimal areaHectares
) {
    public ContextoProdutor {
        if (codigoIbge == null || codigoIbge.isBlank()) {
            throw new IllegalArgumentException("codigoIbge é obrigatório");
        }
        codigoIbge = codigoIbge.trim();
        if (!codigoIbge.matches("^42\\d{5}$")) {
            throw new IllegalArgumentException("codigoIbge deve ser um código IBGE de SC com 7 dígitos");
        }
        if (areaHectares != null && areaHectares.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("areaHectares deve ser maior que zero quando informada");
        }
    }
}
