package com.figueredo.ricardo.mvp.assistente.application.service;

import com.figueredo.ricardo.mvp.assistente.application.port.out.AgroToolFunctionsExecutor;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AssistenteLlmPort;
import com.figueredo.ricardo.mvp.assistente.domain.model.ContextoProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistenteIaServiceTest {

    @Mock
    private AgroToolFunctionsExecutor toolExecutor;

    @Mock
    private AssistenteLlmPort llmPort;

    private PromptConsultivoBuilder promptBuilder;
    private AssistenteIaService service;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptConsultivoBuilder();
        service = new AssistenteIaService(promptBuilder, toolExecutor, llmPort);
    }

    @Test
    void deveDelegarPerguntaParaPortaLlmComSystemPromptEContexto() {
        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Qual a melhor cultura para minha região?", contexto);

        List<DefinicaoFerramentaIa> ferramentas = List.of(
                new DefinicaoFerramentaIa("teste_tool", "desc", Map.of())
        );
        when(toolExecutor.obterDefinicoes()).thenReturn(ferramentas);

        RespostaAssistente mockResposta = new RespostaAssistente(
                "A soja é recomendada pelo ZARC...",
                List.of(),
                List.of("ZARC - MAPA")
        );
        when(llmPort.enviarMensagem(any(), eq(pergunta), eq(ferramentas)))
                .thenReturn(mockResposta);

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertEquals("A soja é recomendada pelo ZARC...", resposta.textoResposta());
        assertEquals(List.of("ZARC - MAPA"), resposta.fontesConsultadas());
        verify(toolExecutor).obterDefinicoes();
        verify(llmPort).enviarMensagem(any(), eq(pergunta), eq(ferramentas));
    }
}
