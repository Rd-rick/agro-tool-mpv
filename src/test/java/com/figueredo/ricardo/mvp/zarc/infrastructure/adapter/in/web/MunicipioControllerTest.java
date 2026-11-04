package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ListarMunicipiosUseCase;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto.ListarMunicipiosResponse;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.mapper.MunicipioWebMapper;
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
class MunicipioControllerTest {

    @Mock
    private ListarMunicipiosUseCase listarMunicipiosUseCase;

    private MunicipioController controller;

    @BeforeEach
    void setUp() {
        controller = new MunicipioController(listarMunicipiosUseCase, new MunicipioWebMapper());
    }

    @Test
    void deveRetornarStatusOkComPayloadDeMunicipios() {
        List<Municipio> municipios = List.of(
                new Municipio("Abdon Batista", "SC", "4200051"),
                new Municipio("Curitibanos", "SC", "4204806")
        );
        when(listarMunicipiosUseCase.listarTodos()).thenReturn(municipios);

        ResponseEntity<ListarMunicipiosResponse> response = controller.listar();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().municipios().size());
        assertEquals("Abdon Batista", response.getBody().municipios().get(0).nome());
        assertEquals("SC", response.getBody().municipios().get(0).uf());
        assertEquals("4200051", response.getBody().municipios().get(0).codigoIbge());
        verify(listarMunicipiosUseCase).listarTodos();
    }
}
