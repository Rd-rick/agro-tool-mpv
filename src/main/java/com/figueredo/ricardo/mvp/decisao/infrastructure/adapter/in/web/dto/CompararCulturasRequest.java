package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CompararCulturasRequest(
        @NotBlank(message = "codigoIbge é obrigatório")
        @Pattern(regexp = "^42\\d{5}$", message = "codigoIbge deve ser um código IBGE catarinense de 7 dígitos iniciado por 42")
        String codigoIbge,

        @NotBlank(message = "mesInicio é obrigatório")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "mesInicio deve estar no formato AAAA-MM")
        String mesInicio,

        @NotBlank(message = "mesFim é obrigatório")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "mesFim deve estar no formato AAAA-MM")
        String mesFim,

        @Pattern(regexp = "^([123]|SOLO_ARENOSO|SOLO_TEXTURA_MEDIA|SOLO_ARGILOSO)?$", message = "soloCodigo deve ser 1 (Solo arenoso), 2 (Solo de textura média) ou 3 (Solo argiloso)")
        String soloCodigo,

        @NotEmpty(message = "culturaIds é obrigatório")
        @Size(min = 2, max = 3, message = "Selecione de 2 a 3 culturas para comparação")
        List<@Positive(message = "culturaId deve ser positivo") Long> culturaIds
) {
}
