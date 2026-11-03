package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcDatasetClientPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.RecursoZarc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
public class ZarcCkanHttpClient implements ZarcDatasetClientPort {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AgroTool/1.0";
    private static final long BYTES_PER_MB = 1024L * 1024L;

    private final String ckanPackageUrl;
    private final long maxDownloadBytes;
    private final RestClient restClient;
    private final HttpClient httpClient;

    public ZarcCkanHttpClient(
            @Value("${app.jobs.zarc.ckan-package-url:https://dados.agricultura.gov.br/api/3/action/package_show?id=tabua-de-risco-zoneamento-agricola-de-risco-climatico}")
            String ckanPackageUrl,
            @Value("${app.jobs.zarc.max-download-mb:500}")
            long maxDownloadMb
    ) {
        this.ckanPackageUrl = ckanPackageUrl;
        this.maxDownloadBytes = (maxDownloadMb > 0 ? maxDownloadMb : 500L) * BYTES_PER_MB;
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
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
        URI uri = validarUrlRecurso(urlRecurso);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofMinutes(5))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                fecharSilenciosamente(response.body());
                throw new IllegalStateException("Falha ao baixar CSV do ZARC. Status HTTP: " + response.statusCode());
            }
            return limitarStream(response.body(), maxDownloadBytes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Download do recurso CSV do ZARC foi interrompido: " + urlRecurso, e);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao conectar no recurso CSV do ZARC: " + urlRecurso, e);
        }
    }

    private URI validarUrlRecurso(String urlRecurso) {
        if (urlRecurso == null || urlRecurso.isBlank()) {
            throw new IllegalArgumentException("URL do recurso ZARC é obrigatória");
        }
        URI uri = URI.create(urlRecurso.trim());
        String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase(Locale.ROOT) : "";
        String host = uri.getHost() != null ? uri.getHost().toLowerCase(Locale.ROOT) : "";
        if ((!"https".equals(scheme) && !"http".equals(scheme)) || !host.endsWith(".gov.br")) {
            throw new IllegalArgumentException("URL do recurso ZARC não pertence a um domínio oficial permitido (.gov.br): " + urlRecurso);
        }
        return uri;
    }

    private void fecharSilenciosamente(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private InputStream limitarStream(InputStream delegate, long limiteBytes) {
        return new FilterInputStream(delegate) {
            private long totalLido = 0;

            @Override
            public int read() throws IOException {
                int b = super.read();
                if (b != -1) {
                    verificarLimite(1);
                }
                return b;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                int lidos = super.read(b, off, len);
                if (lidos > 0) {
                    verificarLimite(lidos);
                }
                return lidos;
            }

            private void verificarLimite(int incremento) {
                totalLido += incremento;
                if (totalLido > limiteBytes) {
                    throw new IllegalStateException("Stream CSV do ZARC excedeu o limite configurado de " + limiteBytes + " bytes");
                }
            }
        };
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
