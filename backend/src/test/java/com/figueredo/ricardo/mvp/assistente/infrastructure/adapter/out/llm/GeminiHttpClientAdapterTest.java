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
import com.figueredo.ricardo.mvp.common.domain.exception.AssistenteQuotaExcedidaException;
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
class GeminiHttpClientAdapterTest {

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
    private GeminiHttpClientAdapter adapter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        promptBuilder = new PromptConsultivoBuilder();
        properties = new AssistenteAiProperties();
        properties.setProvider("gemini");
        properties.getGemini().setApiKey("test-api-key");
        properties.getGemini().setModel("gemini-1.5-flash");
        properties.getGemini().setTimeoutSeconds(30);

        adapter = new GeminiHttpClientAdapter(
                properties,
                toolExecutor,
                promptBuilder,
                objectMapper,
                httpClient
        );
    }

    @Test
    void deveResponderDiretamenteSemChamadaDeFerramentas() throws Exception {
        String jsonRespostaGemini = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Olá produtor! O Zoneamento Agrícola para SC recomenda o plantio de soja a partir de outubro."
                          }
                        ],
                        "role": "model"
                      },
                      "finishReason": "STOP"
                    }
                  ]
                }
                """;

        when(httpResponse1.statusCode()).thenReturn(200);
        when(httpResponse1.body()).thenReturn(jsonRespostaGemini);
        doReturn(httpResponse1).when(httpClient).send(any(HttpRequest.class), any());

        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Quando posso plantar soja?", contexto);

        RespostaAssistente resposta = adapter.enviarMensagem("System prompt", pergunta, List.of());

        assertNotNull(resposta);
        assertTrue(resposta.textoResposta().contains("Olá produtor!"));
        assertTrue(resposta.ferramentasExecutadas().isEmpty());
    }

    @Test
    void deveExecutarCicloDeFunctionCallingComSucesso() throws Exception {
        String respostaTurno1 = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "functionCall": {
                              "name": "consultar_zoneamento_municipio",
                              "args": {
                                "codigoIbge": "4204809"
                              }
                            }
                          }
                        ],
                        "role": "model"
                      }
                    }
                  ]
                }
                """;

        String respostaTurno2 = """
                {
                  "candidates": [
                    {
                      "content": {
                        "parts": [
                          {
                            "text": "Conforme o ZARC (MAPA), as culturas aptas para Curitibanos são Soja e Milho."
                          }
                        ],
                        "role": "model"
                      },
                      "finishReason": "STOP"
                    }
                  ]
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
        PerguntaProdutor pergunta = new PerguntaProdutor("Quais culturas são aptas?", contexto);

        List<DefinicaoFerramentaIa> ferramentas = List.of(
                new DefinicaoFerramentaIa("consultar_zoneamento_municipio", "desc", Map.of())
        );

        RespostaAssistente resposta = adapter.enviarMensagem("System prompt", pergunta, ferramentas);

        assertNotNull(resposta);
        assertEquals("Conforme o ZARC (MAPA), as culturas aptas para Curitibanos são Soja e Milho.", resposta.textoResposta());
        assertEquals(1, resposta.ferramentasExecutadas().size());
        assertEquals("consultar_zoneamento_municipio", resposta.ferramentasExecutadas().get(0).nomeFerramenta());
        assertTrue(resposta.fontesConsultadas().contains("ZARC - MAPA"));
        verify(toolExecutor).executar(any(ChamadaFerramentaIa.class));
    }

    @Test
    void deveLancarExcecaoQuandoChaveDeApiNaoEstiverConfigurada() {
        properties.getGemini().setApiKey("");

        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Pergunta teste", contexto);

        AssistenteIndisponivelException ex = assertThrows(
                AssistenteIndisponivelException.class,
                () -> adapter.enviarMensagem("System prompt", pergunta, List.of())
        );
        assertTrue(ex.getMessage().contains("Chave de API do Gemini não configurada"));
    }

    @Test
    void deveLancarAssistenteQuotaExcedidaExceptionQuandoGeminiRetornar429() throws Exception {
        when(httpResponse1.statusCode()).thenReturn(429);
        when(httpResponse1.body()).thenReturn("{\"error\": {\"code\": 429, \"message\": \"RESOURCE_EXHAUSTED\"}}");
        doReturn(httpResponse1).when(httpClient).send(any(HttpRequest.class), any());

        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Pergunta teste", contexto);

        AssistenteQuotaExcedidaException ex = assertThrows(
                AssistenteQuotaExcedidaException.class,
                () -> adapter.enviarMensagem("System prompt", pergunta, List.of())
        );
        assertEquals("Estamos temporariamente indisponíveis. Por favor, tente novamente amanhã.", ex.getMessage());
    }

    @Test
    void deveLancarAssistenteIndisponivelExceptionQuandoGeminiRetornarErroHttp() throws Exception {
        when(httpResponse1.statusCode()).thenReturn(403);
        when(httpResponse1.body()).thenReturn("{\"error\": {\"message\": \"API_KEY_INVALID\"}}");
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
        doThrow(new IOException("Timeout")).when(httpClient).send(any(HttpRequest.class), any());

        ContextoProdutor contexto = new ContextoProdutor("4204809", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Pergunta teste", contexto);

        assertThrows(
                AssistenteIndisponivelException.class,
                () -> adapter.enviarMensagem("System prompt", pergunta, List.of())
        );
    }
}
