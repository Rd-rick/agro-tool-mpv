package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ConsultarAnaliseCulturaRequest(
        @NotBlank(message = "codigoIbge é obrigatório")
        @Pattern(regexp = "^42\\d{5}$", message = "codigoIbge deve ser um código IBGE catarinense de 7 dígitos iniciado por 42")
        String codigoIbge,

        @NotNull(message = "culturaId é obrigatório")
        @Positive(message = "culturaId deve ser positivo")
        Long culturaId,

        @NotBlank(message = "mesInicio é obrigatório")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "mesInicio deve estar no formato AAAA-MM")
        String mesInicio,

        @NotBlank(message = "mesFim é obrigatório")
        @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "mesFim deve estar no formato AAAA-MM")
        String mesFim,

        @Pattern(regexp = "^([123]|1[1-6]|SOLO_ARENOSO|SOLO_TEXTURA_MEDIA|SOLO_ARGILOSO|SOLO_AD[1-6]|AD[1-6])?$", message = "soloCodigo deve ser 1 a 3 (textura do solo) ou 11 a 16 (classes de água disponível AD1 a AD6)")
        String soloCodigo,

        @Positive(message = "areaHa deve ser positiva")
        @Max(value = 1000000, message = "areaHa deve ser no máximo 1000000")
        BigDecimal areaHa
) {
        public ConsultarAnaliseCulturaRequest(
                String codigoIbge,
                Long culturaId,
                String mesInicio,
                String mesFim,
                String soloCodigo
        ) {
                this(codigoIbge, culturaId, mesInicio, mesFim, soloCodigo, BigDecimal.ONE);
        }
}
