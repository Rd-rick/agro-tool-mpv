package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PerguntaAssistenteRequest(
        @NotBlank(message = "texto da pergunta é obrigatório")
        @Size(max = 2000, message = "texto não pode exceder 2000 caracteres")
        String texto,

        String codigoIbge,
        String nomeMunicipio,
        String mesInicio,
        String mesFim,
        String tipoSolo,
        BigDecimal areaHectares
) {
}
