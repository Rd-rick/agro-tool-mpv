package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

public record ContextoProdutorResponse(
        String codigoIbge,
        String nomeMunicipio,
        String mesInicio,
        String mesFim,
        String tipoSolo,
        BigDecimal areaHectares
) {
}
