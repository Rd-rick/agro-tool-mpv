package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.client;

import com.figueredo.ricardo.mvp.epagri.application.port.out.EpagriClientPort;
import com.figueredo.ricardo.mvp.epagri.domain.model.CotacaoPrecoRegistro;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import com.figueredo.ricardo.mvp.epagri.domain.model.RecursoPrecoEpagri;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EpagriWebScraperClientAdapter implements EpagriClientPort {

    private static final Logger log = LoggerFactory.getLogger(EpagriWebScraperClientAdapter.class);

    private static final Pattern PATTERN_URL_DIARIO = Pattern.compile("href=\"([^\"]*Historico_precos_diario[^\"]*\\.xlsx)\"", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_URL_SEMANAL = Pattern.compile("href=\"([^\"]*Historico_precos_semanal[^\"]*\\.xlsx)\"", Pattern.CASE_INSENSITIVE);

    private final HttpClient httpClient;
    private final String paginaPrecosUrl;
    private final String urlDiariaPadrao;
    private final String urlSemanalPadrao;

    public EpagriWebScraperClientAdapter(
            @Value("${app.jobs.epagri.pagina-precos-url:https://cepa.epagri.sc.gov.br/index.php/mercado-agricola/}") String paginaPrecosUrl,
            @Value("${app.jobs.epagri.url-diaria-padrao:https://docweb.epagri.sc.gov.br/website_cepa/precos/Historico_precos_diario.xlsx}") String urlDiariaPadrao,
            @Value("${app.jobs.epagri.url-semanal-padrao:https://docweb.epagri.sc.gov.br/website_cepa/precos/Historico_precos_semanal.xlsx}") String urlSemanalPadrao
    ) {
        this.paginaPrecosUrl = paginaPrecosUrl;
        this.urlDiariaPadrao = urlDiariaPadrao;
        this.urlSemanalPadrao = urlSemanalPadrao;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    @Override
    public List<RecursoPrecoEpagri> descobrirRecursosDisponiveis() {
        String urlDiaria = urlDiariaPadrao;
        String urlSemanal = urlSemanalPadrao;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(paginaPrecosUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("User-Agent", "AgroTool-EpagriClient/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && response.body() != null) {
                Matcher matcherDiario = PATTERN_URL_DIARIO.matcher(response.body());
                if (matcherDiario.find()) {
                    urlDiaria = matcherDiario.group(1).trim();
                }

                Matcher matcherSemanal = PATTERN_URL_SEMANAL.matcher(response.body());
                if (matcherSemanal.find()) {
                    urlSemanal = matcherSemanal.group(1).trim();
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível realizar scraping na página de preços da Epagri ({}), utilizando URLs padrão configuradas: {}",
                    paginaPrecosUrl, e.getMessage());
        }

        List<RecursoPrecoEpagri> recursos = new ArrayList<>();

        String modificadoDiario = obterUltimoModificado(urlDiaria).orElse(null);
        recursos.add(new RecursoPrecoEpagri("Historico_precos_diario", FrequenciaCotacao.DIARIA, urlDiaria, modificadoDiario));

        String modificadoSemanal = obterUltimoModificado(urlSemanal).orElse(null);
        recursos.add(new RecursoPrecoEpagri("Historico_precos_semanal", FrequenciaCotacao.SEMANAL, urlSemanal, modificadoSemanal));

        return recursos;
    }

    @Override
    public Optional<String> obterUltimoModificado(String urlArquivo) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlArquivo))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "AgroTool-EpagriClient/1.0")
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() == 200) {
                Optional<String> lastModified = response.headers().firstValue("Last-Modified");
                if (lastModified.isPresent()) {
                    return lastModified;
                }
                return response.headers().firstValue("ETag");
            }
        } catch (Exception e) {
            log.warn("Falha ao obter cabeçalhos de última modificação para {}: {}", urlArquivo, e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public int processarPlanilhaStreaming(
            String urlArquivo,
            FrequenciaCotacao frequencia,
            Consumer<List<CotacaoPrecoRegistro>> consumidorLote,
            int batchSize,
            Map<String, Long> mapaProdutos,
            Map<String, Long> mapaPracas
    ) {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("epagri_cotacoes_", ".xlsx");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlArquivo))
                    .timeout(Duration.ofMinutes(5))
                    .header("User-Agent", "AgroTool-EpagriClient/1.0")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream is = response.body()) {
                if (response.statusCode() != 200) {
                    throw new IllegalStateException("Falha ao baixar planilha da Epagri. Código HTTP: " + response.statusCode());
                }

                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            int totalLinhasProcessadas = 0;

            try (OPCPackage pkg = OPCPackage.open(tempFile.toFile(), PackageAccess.READ)) {
                ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(pkg);
                XSSFReader xssfReader = new XSSFReader(pkg);
                StylesTable styles = xssfReader.getStylesTable();
                XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) xssfReader.getSheetsData();

                while (sheets.hasNext()) {
                    try (InputStream sheetStream = sheets.next()) {
                        EpagriSaxSheetHandler handler = new EpagriSaxSheetHandler(
                                frequencia,
                                consumidorLote,
                                batchSize,
                                mapaProdutos,
                                mapaPracas
                        );

                        XMLReader parser = XMLHelper.newXMLReader();
                        XSSFSheetXMLHandler xmlHandler = new XSSFSheetXMLHandler(styles, strings, handler, false);
                        parser.setContentHandler(xmlHandler);
                        parser.parse(new InputSource(sheetStream));

                        handler.finalizar();
                        totalLinhasProcessadas += handler.getTotalProcessados();
                    }
                }
            }

            return totalLinhasProcessadas;
        } catch (Exception e) {
            throw new RuntimeException("Erro durante streaming e processamento do arquivo " + urlArquivo + ": " + e.getMessage(), e);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (Exception e) {
                    log.warn("Não foi possível excluir arquivo temporário: {}", tempFile, e);
                }
            }
        }
    }
}
