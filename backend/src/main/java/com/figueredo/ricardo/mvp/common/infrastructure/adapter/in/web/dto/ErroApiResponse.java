package com.figueredo.ricardo.mvp.common.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroApiResponse(
        boolean success,
        String error,
        String message,
        Map<String, String> fields
) {

    public static ErroApiResponse ofError(String error, String message) {
        return new ErroApiResponse(false, error, message, null);
    }

    public static ErroApiResponse ofValidation(String error, Map<String, String> fields) {
        return new ErroApiResponse(false, error, null, fields);
    }
}
