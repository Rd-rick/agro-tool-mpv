package com.figueredo.ricardo.mvp.common.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@ApiResponse(HttpStatus.NOT_FOUND)
public class CulturaNaoEncontradaException extends ApiErrorException {

    private final String name = "CULTURA_NOT_FOUND";

    public CulturaNaoEncontradaException(Long culturaId) {
        super("Cultura não encontrada para o identificador: " + culturaId);
    }
}
