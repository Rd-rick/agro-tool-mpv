package com.figueredo.ricardo.mvp.common.infrastructure.adapter.in.web.exception;

import com.figueredo.ricardo.mvp.common.domain.exception.ApiErrorException;
import com.figueredo.ricardo.mvp.common.domain.exception.ApiResponse;
import com.figueredo.ricardo.mvp.common.infrastructure.adapter.in.web.dto.ErroApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Getter
    @ApiResponse(HttpStatus.NOT_FOUND)
    static class MunicipioNaoEncontradoFakeException extends ApiErrorException {

        private final String name = "MUNICIPIO_NOT_FOUND";

        MunicipioNaoEncontradoFakeException(String message) {
            super(message);
        }
    }

    @Getter
    static class SemAnotacaoFakeException extends ApiErrorException {

        private final String name = "CUSTOM_INTERNAL_ERROR";

        SemAnotacaoFakeException(String message) {
            super(message);
        }
    }

    @Test
    void deveTratarSubclasseDeApiErrorExceptionComAnotacaoApiResponse() {
        MunicipioNaoEncontradoFakeException ex = new MunicipioNaoEncontradoFakeException("Município não encontrado");

        ResponseEntity<ErroApiResponse> response = handler.handleApiErrorException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("MUNICIPIO_NOT_FOUND", response.getBody().error());
        assertEquals("Município não encontrado", response.getBody().message());
        assertNull(response.getBody().fields());
    }

    @Test
    void deveUsarStatusInternalServerErrorQuandoSubclasseNaoPossuirAnotacaoApiResponse() {
        SemAnotacaoFakeException ex = new SemAnotacaoFakeException("Falha inesperada");

        ResponseEntity<ErroApiResponse> response = handler.handleApiErrorException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("CUSTOM_INTERNAL_ERROR", response.getBody().error());
        assertEquals("Falha inesperada", response.getBody().message());
        assertNull(response.getBody().fields());
    }

    @Test
    void deveTratarMethodArgumentNotValidExceptionNoFormatoPadraoDeCampos() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "codigoIbge", "deve conter 7 dígitos"));
        bindingResult.addError(new FieldError("request", "safra", "formato inválido"));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<ErroApiResponse> response = handler.handleMethodArgumentNotValid(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("VALIDATION_ERROR", response.getBody().error());
        assertNull(response.getBody().message());
        assertNotNull(response.getBody().fields());
        assertEquals("deve conter 7 dígitos", response.getBody().fields().get("codigoIbge"));
        assertEquals("formato inválido", response.getBody().fields().get("safra"));
    }

    @Test
    void deveTratarConstraintViolationExceptionExtraindoNomeDoCampo() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("consultar.codigoIbge");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("código IBGE inválido");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErroApiResponse> response = handler.handleConstraintViolation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().success());
        assertEquals("VALIDATION_ERROR", response.getBody().error());
        assertNull(response.getBody().message());
        assertEquals("código IBGE inválido", response.getBody().fields().get("codigoIbge"));
    }
}
