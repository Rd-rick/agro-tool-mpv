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
import com.figueredo.ricardo.mvp.common.domain.exception.AssistenteQuotaExcedidaException;
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
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiHttpClientAdapter implements AssistenteLlmPort {

    private static final int MAX_FUNCTION_CALL_TURNS = 5;

    private final AssistenteAiProperties properties;
    private final AgroToolFunctionsExecutor toolExecutor;
    private final PromptConsultivoBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Autowired
    public GeminiHttpClientAdapter(
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
                        .connectTimeout(Duration.ofSeconds(Math.max(properties.getGemini().getTimeoutSeconds(), 5)))
                        .build()
        );
    }

    GeminiHttpClientAdapter(
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
        validarConfiguracao();

        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", promptBuilder.construirPromptUsuario(pergunta)))
        ));

        List<Map<String, Object>> toolsPayload = formatarFerramentasGemini(ferramentasDisponiveis);
        List<ChamadaFerramentaIa> ferramentasExecutadas = new ArrayList<>();

        int turno = 0;
        while (turno < MAX_FUNCTION_CALL_TURNS) {
            turno++;
            JsonNode respostaJson = invocarGeminiApi(systemPrompt, contents, toolsPayload);
            JsonNode candidate = respostaJson.path("candidates").path(0);
            JsonNode contentNode = candidate.path("content");
            JsonNode partsNode = contentNode.path("parts");

            List<JsonNode> functionCalls = new ArrayList<>();
            String textoResposta = null;

            for (JsonNode part : partsNode) {
                if (part.has("functionCall")) {
                    functionCalls.add(part.path("functionCall"));
                } else if (part.has("text")) {
                    textoResposta = part.path("text").asText();
                }
            }

            if (functionCalls.isEmpty()) {
                Set<String> fontes = extrairFontes(ferramentasExecutadas);
                String respostaFinal = textoResposta != null ? textoResposta : "";
                return new RespostaAssistente(respostaFinal, ferramentasExecutadas, List.copyOf(fontes));
            }

            Map<String, Object> modelMessage = objectMapper.convertValue(
                    contentNode,
                    new TypeReference<Map<String, Object>>() {}
            );
            contents.add(modelMessage);

            List<Map<String, Object>> responseParts = new ArrayList<>();
            for (JsonNode callNode : functionCalls) {
                String nome = callNode.path("name").asText();
                JsonNode argsNode = callNode.path("args");
                Map<String, Object> args = argsNode.isObject()
                        ? objectMapper.convertValue(argsNode, new TypeReference<Map<String, Object>>() {})
                        : Map.of();

                ChamadaFerramentaIa chamada = new ChamadaFerramentaIa(nome, args);
                ResultadoFerramentaIa resultado = toolExecutor.executar(chamada);
                ferramentasExecutadas.add(chamada);

                Object respostaPayload = converterPayloadResposta(resultado);
                responseParts.add(Map.of(
                        "functionResponse", Map.of(
                                "name", nome,
                                "response", Map.of("result", respostaPayload)
                        )
                ));
            }

            contents.add(Map.of(
                    "role", "user",
                    "parts", responseParts
            ));
        }

        Set<String> fontes = extrairFontes(ferramentasExecutadas);
        return new RespostaAssistente(
                "Limite de interações com ferramentas atingido. Por favor, reformule sua pergunta.",
                ferramentasExecutadas,
                List.copyOf(fontes)
        );
    }

    private JsonNode invocarGeminiApi(
            String systemPrompt,
            List<Map<String, Object>> contents,
            List<Map<String, Object>> tools
    ) {
        String baseUrl = normalizarUrl(properties.getGemini().getBaseUrl());
        String model = properties.getGemini().getModel();
        String apiKey = properties.getGemini().getApiKey();
        String url = String.format("%s/v1beta/models/%s:generateContent?key=%s", baseUrl, model, apiKey);

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("system_instruction", Map.of(
                "parts", List.of(Map.of("text", systemPrompt))
        ));
        requestBody.put("contents", contents);

        if (!tools.isEmpty()) {
            requestBody.put("tools", tools);
        }

        try {
            String jsonPayload = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(properties.getGemini().getTimeoutSeconds()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String body = response.body() != null ? response.body().toUpperCase() : "";
                if (response.statusCode() == 429 || body.contains("RESOURCE_EXHAUSTED") || body.contains("QUOTA_EXCEEDED") || body.contains("QUOTA")) {
                    throw new AssistenteQuotaExcedidaException("Estamos temporariamente indisponíveis. Por favor, tente novamente amanhã.");
                }
                throw new AssistenteIndisponivelException("O assistente inteligente está temporariamente indisponível no momento.");
            }

            return objectMapper.readTree(response.body());
        } catch (AssistenteQuotaExcedidaException | AssistenteIndisponivelException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AssistenteIndisponivelException(
                    "O assistente inteligente está temporariamente indisponível no momento.",
                    ex
            );
        }
    }

    private void validarConfiguracao() {
        String apiKey = properties.getGemini().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new AssistenteIndisponivelException(
                    "Chave de API do Gemini não configurada. Defina a variável de ambiente GEMINI_API_KEY ou app.ai.gemini.api-key no application.yaml."
            );
        }
    }

    private List<Map<String, Object>> formatarFerramentasGemini(List<DefinicaoFerramentaIa> definicoes) {
        if (definicoes == null || definicoes.isEmpty()) {
            return List.of();
        }
        var declarations = definicoes.stream()
                .map(def -> Map.<String, Object>of(
                        "name", def.nome(),
                        "description", def.descricao(),
                        "parameters", def.schemaParametrosJson()
                ))
                .toList();

        return List.of(Map.of("function_declarations", declarations));
    }

    private Object converterPayloadResposta(ResultadoFerramentaIa resultado) {
        if (resultado.sucesso()) {
            try {
                return objectMapper.readTree(resultado.dadosJson());
            } catch (Exception ex) {
                return Map.of("conteudo", resultado.dadosJson());
            }
        }
        return Map.of("erro", resultado.mensagemErro() != null ? resultado.mensagemErro() : "Falha na execução da ferramenta");
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
            return "https://generativelanguage.googleapis.com";
        }
        String trim = url.trim();
        return trim.endsWith("/") ? trim.substring(0, trim.length() - 1) : trim;
    }
}
