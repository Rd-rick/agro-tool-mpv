package com.figueredo.ricardo.mvp.common.infrastructure.adapter.in.web.exception;

import com.figueredo.ricardo.mvp.common.domain.exception.ApiErrorException;
import com.figueredo.ricardo.mvp.common.domain.exception.ApiResponse;
import com.figueredo.ricardo.mvp.common.infrastructure.adapter.in.web.dto.ErroApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    private static final String BAD_REQUEST_ERROR = "BAD_REQUEST_ERROR";

    @ExceptionHandler(ApiErrorException.class)
    public ResponseEntity<ErroApiResponse> handleApiErrorException(Throwable ex) {
        HttpStatus code = HttpStatus.INTERNAL_SERVER_ERROR;

        if (ex.getClass().getAnnotation(ApiResponse.class) != null) {
            code = ex.getClass().getAnnotation(ApiResponse.class).value();
        }

        ApiErrorException apiException = (ApiErrorException) ex;
        String message = apiException.getMessage() != null ? apiException.getMessage() : "";

        return ResponseEntity.status(code)
                .body(ErroApiResponse.ofError(apiException.getName(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApiResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fields.putIfAbsent(
                    fieldError.getField(),
                    fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Valor inválido"
            );
        }
        return ResponseEntity.badRequest()
                .body(ErroApiResponse.ofValidation(VALIDATION_ERROR, fields));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroApiResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String campo = extrairNomeCampo(violation.getPropertyPath().toString());
            fields.putIfAbsent(
                    campo,
                    violation.getMessage() != null ? violation.getMessage() : "Valor inválido"
            );
        }
        return ResponseEntity.badRequest()
                .body(ErroApiResponse.ofValidation(VALIDATION_ERROR, fields));
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErroApiResponse> handleBadRequestExceptions(Exception ex) {
        String mensagem = ex.getMessage() != null ? ex.getMessage() : "";
        return ResponseEntity.badRequest()
                .body(ErroApiResponse.ofError(BAD_REQUEST_ERROR, mensagem));
    }

    private String extrairNomeCampo(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return "parametro";
        }
        int ultimoPonto = propertyPath.lastIndexOf('.');
        return ultimoPonto >= 0 && ultimoPonto < propertyPath.length() - 1
                ? propertyPath.substring(ultimoPonto + 1)
                : propertyPath;
    }
}
