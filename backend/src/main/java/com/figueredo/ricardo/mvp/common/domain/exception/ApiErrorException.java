package com.figueredo.ricardo.mvp.common.domain.exception;

import lombok.Getter;

@Getter
public class ApiErrorException extends RuntimeException {

    protected String name = "INTERNAL_ERROR";

    public ApiErrorException(String message) {
        super(message);
    }
}
