package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.assistente.application.port.in.PerguntarAssistenteUseCase;
import com.figueredo.ricardo.mvp.assistente.domain.model.ChamadaFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.PerguntaAssistenteRequest;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.RespostaAssistenteResponse;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.mapper.AssistenteWebMapper;
import com.figueredo.ricardo.mvp.common.domain.exception.AssistenteIndisponivelException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistenteControllerTest {

    @Mock
    private PerguntarAssistenteUseCase perguntarAssistenteUseCase;

    private AssistenteWebMapper mapper;
    private AssistenteController controller;
    private Validator validator;

    @BeforeEach
    void setUp() {
        mapper = new AssistenteWebMapper();
        controller = new AssistenteController(perguntarAssistenteUseCase, mapper);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void deveResponderComSucessoStatus200() {
        java.time.YearMonth inicio = java.time.YearMonth.now();
        java.time.YearMonth fim = inicio.plusMonths(3);

        PerguntaAssistenteRequest request = new PerguntaAssistenteRequest(
                "Quais culturas têm menor risco climático?",
                "4204809",
                inicio.toString(),
                fim.toString(),
                "2",
                new BigDecimal("50.0")
        );

        RespostaAssistente respostaDominio = new RespostaAssistente(
                "A cultura com menor risco no período é o milho.",
                List.of(new ChamadaFerramentaIa("consultar_zoneamento_municipio", Map.of("codigoIbge", "4204809"))),
                List.of("ZARC - MAPA")
        );
        when(perguntarAssistenteUseCase.perguntar(any(PerguntaProdutor.class))).thenReturn(respostaDominio);

        ResponseEntity<RespostaAssistenteResponse> response = controller.perguntar(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("A cultura com menor risco no período é o milho.", response.getBody().resposta());
        assertEquals(1, response.getBody().ferramentasUtilizadas().size());
        assertEquals("consultar_zoneamento_municipio", response.getBody().ferramentasUtilizadas().get(0).ferramenta());
        assertEquals(List.of("ZARC - MAPA"), response.getBody().fontesCitadas());
        verify(perguntarAssistenteUseCase).perguntar(any(PerguntaProdutor.class));
    }

    @Test
    void deveDetectarViolacoesDeValidacaoQuandoCamposObrigatoriosForemInvalidos() {
        PerguntaAssistenteRequest requestInvalido = new PerguntaAssistenteRequest(
                "",
                "12345",
                null,
                null,
                null,
                null
        );

        var violacoes = validator.validate(requestInvalido);
        assertFalse(violacoes.isEmpty());
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("texto")));
        assertTrue(violacoes.stream().anyMatch(v -> v.getPropertyPath().toString().equals("codigoIbge")));
    }

    @Test
    void devePropagarAssistenteIndisponivelException() {
        PerguntaAssistenteRequest request = new PerguntaAssistenteRequest(
                "Pergunta teste",
                "4204809",
                null,
                null,
                null,
                null
        );

        when(perguntarAssistenteUseCase.perguntar(any(PerguntaProdutor.class)))
                .thenThrow(new AssistenteIndisponivelException("Ollama indisponível"));

        assertThrows(AssistenteIndisponivelException.class, () -> controller.perguntar(request));
    }
}
