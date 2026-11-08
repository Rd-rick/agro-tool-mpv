package com.figueredo.ricardo.mvp.clima.infrastructure.adapter.out.http;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.figueredo.ricardo.mvp.clima.application.port.out.ClimaMunicipioPort;
import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;
import com.figueredo.ricardo.mvp.clima.domain.model.CondicaoTempo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Component
public class InmetPrevisaoHttpAdapter implements ClimaMunicipioPort {

    private static final String BASE_URL = "https://apiprevmet3.inmet.gov.br";
    private static final ZoneId ZONE_SP = ZoneId.of("America/Sao_Paulo");

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public InmetPrevisaoHttpAdapter() {
        this(new ObjectMapper());
    }

    @Autowired
    public InmetPrevisaoHttpAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "AgroTool/1.0 (Santa Catarina Agricola)")
                .build();
    }

    InmetPrevisaoHttpAdapter(RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public ClimaAtual obterClimaDiaAtual(String codigoIbge) {
        if (codigoIbge == null || codigoIbge.isBlank()) {
            return ClimaAtual.indisponivel();
        }

        try {
            String json = restClient.get()
                    .uri("/previsao/{codigoIbge}", codigoIbge)
                    .retrieve()
                    .body(String.class);

            if (json == null || json.isBlank()) {
                return ClimaAtual.indisponivel();
            }

            JsonNode root = objectMapper.readTree(json);
            JsonNode cityNode = root.path(codigoIbge);
            if (cityNode.isMissingNode() || cityNode.isEmpty()) {
                return ClimaAtual.indisponivel();
            }

            Map<String, Object> datesMap = objectMapper.convertValue(cityNode, new TypeReference<Map<String, Object>>() {});
            if (datesMap == null || datesMap.isEmpty()) {
                return ClimaAtual.indisponivel();
            }

            String firstDateKey = datesMap.keySet().iterator().next();
            JsonNode todayNode = cityNode.path(firstDateKey);
            if (todayNode.isMissingNode() || todayNode.isEmpty()) {
                return ClimaAtual.indisponivel();
            }

            JsonNode periodNode = selectPeriodNode(todayNode);
            if (periodNode == null || periodNode.isEmpty()) {
                return ClimaAtual.indisponivel();
            }

            Integer temperatura = extractTemperatura(periodNode);
            String resumo = periodNode.path("resumo").asText(null);
            CondicaoTempo condicao = CondicaoTempo.fromResumo(resumo);

            if (temperatura == null && (resumo == null || resumo.isBlank())) {
                return ClimaAtual.indisponivel();
            }

            return ClimaAtual.disponivel(temperatura, condicao, resumo);
        } catch (Exception e) {
            return ClimaAtual.indisponivel();
        }
    }

    private JsonNode selectPeriodNode(JsonNode todayNode) {
        int hour = LocalTime.now(ZONE_SP).getHour();
        List<String> periodPreferences = resolvePeriodPreferences(hour);

        for (String period : periodPreferences) {
            JsonNode node = todayNode.path(period);
            if (!node.isMissingNode() && !node.isEmpty()) {
                return node;
            }
        }

        if (todayNode.has("resumo") || todayNode.has("temp_max")) {
            return todayNode;
        }

        return null;
    }

    private List<String> resolvePeriodPreferences(int hour) {
        if (hour < 12) {
            return List.of("manha", "tarde", "noite");
        }
        if (hour < 18) {
            return List.of("tarde", "manha", "noite");
        }
        return List.of("noite", "tarde", "manha");
    }

    private Integer extractTemperatura(JsonNode node) {
        if (node.hasNonNull("temp_max")) {
            return node.path("temp_max").asInt();
        }
        if (node.hasNonNull("temp_min")) {
            return node.path("temp_min").asInt();
        }
        return null;
    }
}

