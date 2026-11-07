package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.pam.application.port.in.ImportarPamUseCase;
import com.figueredo.ricardo.mvp.pam.application.port.in.ImportarPamUseCase.ResultadoImportacao;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web.dto.ExecutarImportacaoPamResponse;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web.mapper.PamJobWebMapper;
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
class PamJobControllerTest {

    @Mock
    private ImportarPamUseCase useCase;

    private PamJobController controller;

    @BeforeEach
    void setUp() {
        controller = new PamJobController(useCase, new PamJobWebMapper());
    }

    @Test
    void deveExecutarAnoValidoERetornarResponseMapeado() {
        ResultadoImportacao resultado = new ResultadoImportacao(1, 295, List.of("Ano 2024: 295"));
        when(useCase.importarAno(2024)).thenReturn(resultado);

        ResponseEntity<ExecutarImportacaoPamResponse> response = controller.executarAno("2024");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().totalAnosProcessados());
        assertEquals(295, response.getBody().totalRegistrosImportados());
        verify(useCase).importarAno(2024);
    }

    @Test
    void deveRetornarBadRequestQuandoAnoForInvalido() {
        ResponseEntity<ExecutarImportacaoPamResponse> response = controller.executarAno("1900");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
