package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ImportarZarcUseCase;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ImportarZarcUseCase.ResultadoImportacao;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto.ExecutarImportacaoZarcResponse;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.mapper.ZarcJobWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZarcJobControllerTest {

    @Mock
    private ImportarZarcUseCase importarZarcUseCase;

    private ZarcJobController controller;

    @BeforeEach
    void setUp() {
        controller = new ZarcJobController(importarZarcUseCase, new ZarcJobWebMapper());
    }

    @Test
    void deveExecutarTodasAsSafrasERetornarResponseMapeado() {
        ResultadoImportacao resultado = new ResultadoImportacao(2, 500, List.of("Safra 2025/2026: 500"));
        when(importarZarcUseCase.importarTodasSafrasConfiguradas()).thenReturn(resultado);

        ResponseEntity<ExecutarImportacaoZarcResponse> response = controller.executarTodas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().totalSafrasProcessadas());
        assertEquals(500, response.getBody().totalRegistrosImportados());
        verify(importarZarcUseCase).importarTodasSafrasConfiguradas();
    }

    @Test
    void deveRetornarBadRequestQuandoSafraForInvalida() {
        ResponseEntity<ExecutarImportacaoZarcResponse> response = controller.executarSafra("   ");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
