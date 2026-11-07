package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.epagri.application.port.in.ImportarPrecosEpagriUseCase;
import com.figueredo.ricardo.mvp.epagri.application.port.in.ImportarPrecosEpagriUseCase.ResultadoImportacao;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web.dto.ExecutarImportacaoEpagriResponse;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web.mapper.EpagriJobWebMapper;
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
class EpagriJobControllerTest {

    @Mock
    private ImportarPrecosEpagriUseCase useCase;

    private EpagriJobController controller;

    @BeforeEach
    void setUp() {
        controller = new EpagriJobController(useCase, new EpagriJobWebMapper());
    }

    @Test
    void deveExecutarFrequenciaValidaERetornarResponseMapeado() {
        ResultadoImportacao resultado = new ResultadoImportacao(1, 120, List.of("DIARIA: 120"));
        when(useCase.importarFrequencia(FrequenciaCotacao.DIARIA)).thenReturn(resultado);

        ResponseEntity<ExecutarImportacaoEpagriResponse> response = controller.executarFrequencia("diaria");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().totalFrequenciasProcessadas());
        assertEquals(120, response.getBody().totalRegistrosImportados());
        verify(useCase).importarFrequencia(FrequenciaCotacao.DIARIA);
    }

    @Test
    void deveRetornarBadRequestQuandoFrequenciaDesconhecida() {
        ResponseEntity<ExecutarImportacaoEpagriResponse> response = controller.executarFrequencia("mensal");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
