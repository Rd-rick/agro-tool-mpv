package com.figueredo.ricardo.mvp.common.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@ApiResponse(HttpStatus.TOO_MANY_REQUESTS)
public class AssistenteQuotaExcedidaException extends ApiErrorException {

    private final String name = "ASSISTENTE_QUOTA_EXCEDIDA";

    public AssistenteQuotaExcedidaException(String mensagem) {
        super(mensagem);
    }

    public AssistenteQuotaExcedidaException(String mensagem, Throwable cause) {
        super(mensagem);
        if (cause != null) {
            initCause(cause);
        }
    }
}
