package com.figueredo.ricardo.mvp.common.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@ApiResponse(HttpStatus.NOT_FOUND)
public class MunicipioNaoEncontradoException extends ApiErrorException {

    private final String name = "MUNICIPIO_NOT_FOUND";

    public MunicipioNaoEncontradoException(String codigoIbge) {
        super("Município não encontrado para o código IBGE: " + codigoIbge);
    }
}
