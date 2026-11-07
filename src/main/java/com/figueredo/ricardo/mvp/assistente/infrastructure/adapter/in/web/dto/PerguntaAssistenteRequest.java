package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PerguntaAssistenteRequest(
        @NotBlank(message = "texto da pergunta é obrigatório")
        @Size(max = 2000, message = "texto não pode exceder 2000 caracteres")
        String texto,

        @NotBlank(message = "codigoIbge é obrigatório")
        @Pattern(regexp = "^42\\d{5}$", message = "codigoIbge deve ser um código IBGE de SC com 7 dígitos")
        String codigoIbge,

        String mesInicio,
        String mesFim,
        String tipoSolo,
        BigDecimal areaHectares
) {
}
