package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.out.llm;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AgroToolFunctionsExecutor;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AssistenteLlmPort;
import com.figueredo.ricardo.mvp.assistente.application.service.ExecutorFerramentasAgroService;
import com.figueredo.ricardo.mvp.assistente.application.service.PromptConsultivoBuilder;
import com.figueredo.ricardo.mvp.assistente.domain.model.ChamadaFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import com.figueredo.ricardo.mvp.assistente.domain.model.ResultadoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.infrastructure.config.AssistenteAiProperties;
import com.figueredo.ricardo.mvp.common.domain.exception.AssistenteIndisponivelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaHttpClientAdapter implements AssistenteLlmPort {

    private static final int MAX_FUNCTION_CALL_TURNS = 5;

    private final AssistenteAiProperties properties;
    private final AgroToolFunctionsExecutor toolExecutor;
    private final PromptConsultivoBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public OllamaHttpClientAdapter(
            AssistenteAiProperties properties,
            AgroToolFunctionsExecutor toolExecutor,
            PromptConsultivoBuilder promptBuilder,
            ObjectMapper objectMapper
    ) {
        this(
                properties,
                toolExecutor,
                promptBuilder,
                objectMapper,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(Math.max(properties.getOllama().getTimeoutSeconds(), 5)))
                        .build()
        );
    }

    OllamaHttpClientAdapter(
            AssistenteAiProperties properties,
            AgroToolFunctionsExecutor toolExecutor,
            PromptConsultivoBuilder promptBuilder,
            ObjectMapper objectMapper,
            HttpClient httpClient
    ) {
        this.properties = Objects.requireNonNull(properties);
        this.toolExecutor = Objects.requireNonNull(toolExecutor);
        this.promptBuilder = Objects.requireNonNull(promptBuilder);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    @Override
    public RespostaAssistente enviarMensagem(
            String systemPrompt,
            PerguntaProdutor pergunta,
            List<DefinicaoFerramentaIa> ferramentasDisponiveis
    ) {
        List<Map<String, Object>> historicoMensagens = new ArrayList<>();
        historicoMensagens.add(Map.of("role", "system", "content", systemPrompt));
        historicoMensagens.add(Map.of("role", "user", "content", promptBuilder.construirPromptUsuario(pergunta)));

        List<Map<String, Object>> ferramentasPayload = formatarFerramentas(ferramentasDisponiveis);
        List<ChamadaFerramentaIa> ferramentasExecutadas = new ArrayList<>();

        for (int turno = 0; turno < MAX_FUNCTION_CALL_TURNS; turno++) {
            JsonNode respostaJson = invocarOllamaChat(historicoMensagens, ferramentasPayload);
            JsonNode messageNode = respostaJson.path("message");
            String content = messageNode.path("content").asText("");
            JsonNode toolCallsNode = messageNode.path("tool_calls");

            if (!toolCallsNode.isArray() || toolCallsNode.isEmpty()) {
                Set<String> fontes = extrairFontes(ferramentasExecutadas);
                return new RespostaAssistente(content, ferramentasExecutadas, List.copyOf(fontes));
            }

            Map<String, Object> mensagemAssistente = objectMapper.convertValue(
                    messageNode,
                    new TypeReference<Map<String, Object>>() {}
            );
            historicoMensagens.add(mensagemAssistente);

            for (JsonNode callNode : toolCallsNode) {
                String nome = callNode.path("function").path("name").asText();
                JsonNode argsNode = callNode.path("function").path("arguments");
                Map<String, Object> args = argsNode.isObject()
                        ? objectMapper.convertValue(argsNode, new TypeReference<Map<String, Object>>() {})
                        : Map.of();

                ChamadaFerramentaIa chamada = new ChamadaFerramentaIa(nome, args);
                ResultadoFerramentaIa resultado = toolExecutor.executar(chamada);
                ferramentasExecutadas.add(chamada);

                Map<String, Object> toolMessage = new LinkedHashMap<>();
                toolMessage.put("role", "tool");
                toolMessage.put("content", resultado.sucesso() ? resultado.dadosJson() : resultado.mensagemErro());
                historicoMensagens.add(toolMessage);
            }
        }

        Set<String> fontes = extrairFontes(ferramentasExecutadas);
        return new RespostaAssistente(
                "Limite de interações com ferramentas atingido. Por favor, reformule sua pergunta.",
                ferramentasExecutadas,
                List.copyOf(fontes)
        );
    }

    private JsonNode invocarOllamaChat(
            List<Map<String, Object>> mensagens,
            List<Map<String, Object>> ferramentas
    ) {
        String url = normalizarUrl(properties.getOllama().getBaseUrl()) + "/api/chat";
        Map<String, Object> corpoRequisicao = new LinkedHashMap<>();
        corpoRequisicao.put("model", properties.getOllama().getModel());
        corpoRequisicao.put("stream", false);
        corpoRequisicao.put("messages", mensagens);

        if (!ferramentas.isEmpty()) {
            corpoRequisicao.put("tools", ferramentas);
        }

        try {
            String jsonPayload = objectMapper.writeValueAsString(corpoRequisicao);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(properties.getOllama().getTimeoutSeconds()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AssistenteIndisponivelException(
                        "Ollama retornou status HTTP " + response.statusCode() + ": " + response.body()
                );
            }

            return objectMapper.readTree(response.body());
        } catch (AssistenteIndisponivelException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AssistenteIndisponivelException(
                    "Não foi possível se comunicar com o assistente LLM local (Ollama). Verifique se o serviço está ativo.",
                    ex
            );
        }
    }

    private List<Map<String, Object>> formatarFerramentas(List<DefinicaoFerramentaIa> definicoes) {
        return definicoes.stream()
                .map(def -> Map.<String, Object>of(
                        "type", "function",
                        "function", Map.of(
                                "name", def.nome(),
                                "description", def.descricao(),
                                "parameters", def.schemaParametrosJson()
                        )
                ))
                .toList();
    }

    private Set<String> extrairFontes(List<ChamadaFerramentaIa> chamadas) {
        Set<String> fontes = new LinkedHashSet<>();
        for (ChamadaFerramentaIa chamada : chamadas) {
            switch (chamada.nomeFerramenta()) {
                case ExecutorFerramentasAgroService.FERRAMENTA_ZONEAMENTO -> fontes.add("ZARC - MAPA");
                case ExecutorFerramentasAgroService.FERRAMENTA_RANKING -> {
                    fontes.add("ZARC - MAPA");
                    fontes.add("Epagri/Cepa");
                    fontes.add("CONAB");
                    fontes.add("Motor de Decisão Agro Tool");
                }
                case ExecutorFerramentasAgroService.FERRAMENTA_ANALISE -> {
                    fontes.add("ZARC - MAPA");
                    fontes.add("Epagri/Cepa");
                    fontes.add("CONAB");
                    fontes.add("IBGE/PAM");
                }
                case ExecutorFerramentasAgroService.FERRAMENTA_COMPARACAO -> {
                    fontes.add("ZARC - MAPA");
                    fontes.add("Epagri/Cepa");
                    fontes.add("CONAB");
                }
                default -> {
                }
            }
        }
        return fontes;
    }

    private String normalizarUrl(String url) {
        if (url == null || url.isBlank()) {
            return "http://localhost:11434";
        }
        String trim = url.trim();
        return trim.endsWith("/") ? trim.substring(0, trim.length() - 1) : trim;
    }
}
