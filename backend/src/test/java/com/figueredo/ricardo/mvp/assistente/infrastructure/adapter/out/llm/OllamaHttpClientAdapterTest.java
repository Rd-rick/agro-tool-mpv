package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.out.llm;

import tools.jackson.databind.ObjectMapper;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AgroToolFunctionsExecutor;
import com.figueredo.ricardo.mvp.assistente.application.service.ExecutorFerramentasAgroService;
import com.figueredo.ricardo.mvp.assistente.application.service.PromptConsultivoBuilder;
import com.figueredo.ricardo.mvp.assistente.domain.model.ChamadaFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.ContextoProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import com.figueredo.ricardo.mvp.assistente.domain.model.ResultadoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.infrastructure.config.AssistenteAiProperties;
import com.figueredo.ricardo.mvp.common.domain.exception.AssistenteIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaHttpClientAdapterTest {

    @Mock
    private AgroToolFunctionsExecutor toolExecutor;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpResponse<String> httpResponse1;

    @Mock
    private HttpResponse<String> httpResponse2;

    private ObjectMapper objectMapper;
    private PromptConsultivoBuilder promptBuilder;
    private AssistenteAiProperties properties;
    private OllamaHttpClientAdapter adapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        promptBuilder = new PromptConsultivoBuilder();
        properties = new AssistenteAiProperties();
        properties.getOllama().setBaseUrl("http://localhost:11434");
        properties.getOllama().setModel("llama3.1");
        properties.getOllama().setTimeoutSeconds(30);

        adapter = new OllamaHttpClientAdapter(
                properties,
                toolExecutor,
                promptBuilder,
                objectMapper,
                httpClient
        );
    }

    @Test
    void deveResponderDiretamenteSemChamadaDeFerramentas() throws Exception {
        String jsonRespostaOllama = """
                {
                  "model": "llama3.1",
                  "message": {
                    "role": "assistant",
                    "content": "Olá produtor! Em Santa Catarina, a época recomendada para a soja inicia em outubro."
                  },
                  "done": true
                }
                """;

        when(httpResponse1.statusCode()).thenReturn(200);
        when(httpResponse1.body()).thenReturn(jsonRespostaOllama);
        doReturn(httpResponse1).when(httpClient).send(any(HttpRequest.class), any());

        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Quando plantar soja em Curitibanos?", contexto);

        RespostaAssistente resposta = adapter.enviarMensagem("System prompt", pergunta, List.of());

        assertNotNull(resposta);
        assertTrue(resposta.textoResposta().contains("Olá produtor!"));
        assertTrue(resposta.ferramentasExecutadas().isEmpty());
    }

    @Test
    void deveExecutarCicloDeFunctionCallingComSucesso() throws Exception {
        String respostaTurno1 = """
                {
                  "model": "llama3.1",
                  "message": {
                    "role": "assistant",
                    "content": "",
                    "tool_calls": [
                      {
                        "function": {
                          "name": "consultar_zoneamento_municipio",
                          "arguments": {
                            "codigoIbge": "4204809"
                          }
                        }
                      }
                    ]
                  },
                  "done": true
                }
                """;

        String respostaTurno2 = """
                {
                  "model": "llama3.1",
                  "message": {
                    "role": "assistant",
                    "content": "Com base nos dados oficiais do ZARC (MAPA), o município de Curitibanos possui soja e milho como culturas aptas."
                  },
                  "done": true
                }
                """;

        when(httpResponse1.statusCode()).thenReturn(200);
        when(httpResponse1.body()).thenReturn(respostaTurno1);
        when(httpResponse2.statusCode()).thenReturn(200);
        when(httpResponse2.body()).thenReturn(respostaTurno2);

        doReturn(httpResponse1).doReturn(httpResponse2).when(httpClient).send(any(HttpRequest.class), any());

        when(toolExecutor.executar(any(ChamadaFerramentaIa.class)))
                .thenReturn(ResultadoFerramentaIa.comSucesso(
                        ExecutorFerramentasAgroService.FERRAMENTA_ZONEAMENTO,
                        "{\"culturasAptas\": [\"Soja\", \"Milho\"]}"
                ));

        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Quais culturas posso plantar em Curitibanos?", contexto);

        List<DefinicaoFerramentaIa> ferramentas = List.of(
                new DefinicaoFerramentaIa("consultar_zoneamento_municipio", "desc", Map.of())
        );

        RespostaAssistente resposta = adapter.enviarMensagem("System prompt", pergunta, ferramentas);

        assertNotNull(resposta);
        assertEquals("Com base nos dados oficiais do ZARC (MAPA), o município de Curitibanos possui soja e milho como culturas aptas.", resposta.textoResposta());
        assertEquals(1, resposta.ferramentasExecutadas().size());
        assertEquals("consultar_zoneamento_municipio", resposta.ferramentasExecutadas().get(0).nomeFerramenta());
        assertTrue(resposta.fontesConsultadas().contains("ZARC - MAPA"));
        verify(toolExecutor).executar(any(ChamadaFerramentaIa.class));
    }

    @Test
    void deveLancarAssistenteIndisponivelExceptionQuandoOllamaRetornarErroHttp() throws Exception {
        when(httpResponse1.statusCode()).thenReturn(500);
        when(httpResponse1.body()).thenReturn("Internal Server Error");
        doReturn(httpResponse1).when(httpClient).send(any(HttpRequest.class), any());

        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Pergunta teste", contexto);

        assertThrows(
                AssistenteIndisponivelException.class,
                () -> adapter.enviarMensagem("System prompt", pergunta, List.of())
        );
    }

    @Test
    void deveLancarAssistenteIndisponivelExceptionQuandoOcorrerErroDeConexao() throws Exception {
        doThrow(new IOException("Connection refused")).when(httpClient).send(any(HttpRequest.class), any());

        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Pergunta teste", contexto);

        assertThrows(
                AssistenteIndisponivelException.class,
                () -> adapter.enviarMensagem("System prompt", pergunta, List.of())
        );
    }
}
