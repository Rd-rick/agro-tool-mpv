package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamDatasetClientPort;
import com.figueredo.ricardo.mvp.pam.domain.model.RecursoPam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class PamSidraHttpClientAdapter implements PamDatasetClientPort {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AgroTool-PamClient/1.0";
    private static final long BYTES_PER_MB = 1024L * 1024L;
    private static final String DOMINIO_IBGE = ".ibge.gov.br";
    private static final String PREFIXO_IDENTIFICADOR = "PAM_5457_SC_";

    private final String periodosUrl;
    private final String sidraValoresBaseUrl;
    private final long maxDownloadBytes;
    private final RestClient restClient;
    private final HttpClient httpClient;

    public PamSidraHttpClientAdapter(
            @Value("${app.jobs.pam.periodos-url:https://servicodados.ibge.gov.br/api/v3/agregados/5457/periodos}")
            String periodosUrl,
            @Value("${app.jobs.pam.sidra-valores-base-url:https://apisidra.ibge.gov.br/values/t/5457/n6/in%20n3%2042/v/8331,216,214,112,215}")
            String sidraValoresBaseUrl,
            @Value("${app.jobs.pam.max-download-mb:100}")
            long maxDownloadMb
    ) {
        this.periodosUrl = periodosUrl;
        this.sidraValoresBaseUrl = sidraValoresBaseUrl;
        this.maxDownloadBytes = (maxDownloadMb > 0 ? maxDownloadMb : 100L) * BYTES_PER_MB;
        this.restClient = RestClient.builder()
                .defaultHeader("User-Agent", USER_AGENT)
                .build();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public List<RecursoPam> buscarRecursosDisponiveis() {
        URI uri = validarUrlIbge(periodosUrl);
        List<IbgePeriodoItem> response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (response == null || response.isEmpty()) {
            return Collections.emptyList();
        }

        return response.stream()
                .filter(Objects::nonNull)
                .map(this::converterParaRecursoPam)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public InputStream abrirStreamValoresAno(int ano, Collection<String> codigosCulturaOrigem) {
        if (codigosCulturaOrigem == null || codigosCulturaOrigem.isEmpty()) {
            throw new IllegalArgumentException("Lista de códigos de cultura para consulta no SIDRA não pode ser vazia");
        }

        String codigosConcatenados = codigosCulturaOrigem.stream()
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .sorted()
                .collect(Collectors.joining(","));

        String baseLimpa = sidraValoresBaseUrl.endsWith("/")
                ? sidraValoresBaseUrl.substring(0, sidraValoresBaseUrl.length() - 1)
                : sidraValoresBaseUrl;

        String urlCompleta = baseLimpa + "/p/" + ano + "/c782/" + codigosConcatenados + "/h/n";
        URI uri = validarUrlIbge(urlCompleta);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("User-Agent", USER_AGENT)
                    .timeout(Duration.ofMinutes(3))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() >= 400) {
                fecharSilenciosamente(response.body());
                throw new IllegalStateException("Falha ao consultar API SIDRA do IBGE. Status HTTP: " + response.statusCode());
            }
            return limitarStream(response.body(), maxDownloadBytes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Consulta ao recurso SIDRA da PAM foi interrompida: " + urlCompleta, e);
        } catch (IOException e) {
            throw new RuntimeException("Erro de I/O ao consultar recurso SIDRA da PAM: " + urlCompleta, e);
        }
    }

    private RecursoPam converterParaRecursoPam(IbgePeriodoItem item) {
        if (item.id() == null || item.id().isBlank()) {
            return null;
        }
        try {
            int ano = Integer.parseInt(item.id().trim());
            return new RecursoPam(
                    PREFIXO_IDENTIFICADOR + ano,
                    ano,
                    item.modificacao()
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private URI validarUrlIbge(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL da API do IBGE é obrigatória");
        }
        URI uri = URI.create(url.trim());
        String scheme = uri.getScheme() != null ? uri.getScheme().toLowerCase(Locale.ROOT) : "";
        String host = uri.getHost() != null ? uri.getHost().toLowerCase(Locale.ROOT) : "";
        if (!"https".equals(scheme) || !host.endsWith(DOMINIO_IBGE)) {
            throw new IllegalArgumentException("URL não pertence a um domínio HTTPS oficial do IBGE (.ibge.gov.br): " + url);
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
                    throw new IllegalStateException("Stream da API SIDRA excedeu o limite configurado de " + limiteBytes + " bytes");
                }
            }
        };
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record IbgePeriodoItem(
            String id,
            String modificacao
    ) {}
}
