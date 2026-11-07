package com.figueredo.ricardo.mvp.common.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@ApiResponse(HttpStatus.SERVICE_UNAVAILABLE)
public class AssistenteIndisponivelException extends ApiErrorException {

    private final String name = "ASSISTENTE_INDISPONIVEL";

    public AssistenteIndisponivelException(String mensagem) {
        super(mensagem);
    }

    public AssistenteIndisponivelException(String mensagem, Throwable cause) {
        super(mensagem);
        if (cause != null) {
            initCause(cause);
        }
    }
}
