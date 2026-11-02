package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcDatasetClientPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.RecursoZarc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Component
public class ZarcCkanHttpClient implements ZarcDatasetClientPort {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AgroTool/1.0";

    private final String ckanPackageUrl;
    private final RestClient restClient;
    private final HttpClient httpClient;

    public ZarcCkanHttpClient(
            @Value("${app.jobs.zarc.ckan-package-url:https://dados.agricultura.gov.br/api/3/action/package_show?id=tabua-de-risco-zoneamento-agricola-de-risco-climatico}")
            String ckanPackageUrl
    ) {
        this.ckanPackageUrl = ckanPackageUrl;
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    @Override
    public List<RecursoZarc> buscarRecursosDisponiveis() {
        CkanPackageResponse response = restClient.get()
                .uri(ckanPackageUrl)
                .retrieve()
                .body(CkanPackageResponse.class);

        if (response == null || !response.success() || response.result() == null || response.result().resources() == null) {
            return Collections.emptyList();
        }

        return response.result().resources().stream()
                .map(r -> new RecursoZarc(r.id(), r.name(), r.format(), r.url(), r.lastModified()))
                .toList();
    }

    @Override
    public InputStream abrirStreamCsv(String urlRecurso) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlRecurso))
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofMinutes(5))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Falha ao baixar CSV do ZARC. Status HTTP: " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao conectar no recurso CSV do ZARC: " + urlRecurso, e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CkanPackageResponse(
            boolean success,
            CkanPackageResult result
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CkanPackageResult(
            List<CkanResourceItem> resources
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CkanResourceItem(
            String id,
            String name,
            String format,
            String url,
            @JsonProperty("last_modified") String lastModified
    ) {}
}
