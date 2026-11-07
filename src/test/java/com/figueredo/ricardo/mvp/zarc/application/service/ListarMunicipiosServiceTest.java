package com.figueredo.ricardo.mvp.zarc.application.service;

import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarMunicipiosServiceTest {

    @Mock
    private MunicipioPort municipioPort;

    @InjectMocks
    private ListarMunicipiosService service;

    @Test
    void deveRetornarListaDeMunicipiosOrdenadosPorNome() {
        List<Municipio> esperado = List.of(
                new Municipio("Abdon Batista", "SC", "4200051", 7L, "Praça regional atribuída por microrregião IBGE 009 (Curitibanos) -> Meio Oeste (Epagri/Cepa)"),
                new Municipio("Curitibanos", "SC", "4204806", 7L, "Praça regional atribuída por microrregião IBGE 009 (Curitibanos) -> Meio Oeste (Epagri/Cepa)")
        );
        when(municipioPort.listarTodosOrdenadosPorNome()).thenReturn(esperado);

        List<Municipio> resultado = service.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Abdon Batista", resultado.get(0).nome());
        assertEquals("SC", resultado.get(0).uf());
        assertEquals("4200051", resultado.get(0).codigoIbge());
        assertEquals(7L, resultado.get(0).pracaReferenciaId());
        assertEquals("Curitibanos", resultado.get(1).nome());
        assertEquals(7L, resultado.get(1).pracaReferenciaId());
        assertEquals("Praça regional atribuída por microrregião IBGE 009 (Curitibanos) -> Meio Oeste (Epagri/Cepa)", resultado.get(1).observacaoPraca());
        verify(municipioPort).listarTodosOrdenadosPorNome();
    }
}
