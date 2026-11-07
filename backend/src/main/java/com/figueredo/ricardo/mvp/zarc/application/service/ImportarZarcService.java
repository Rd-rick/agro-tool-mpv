package com.figueredo.ricardo.mvp.zarc.application.service;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ImportarZarcUseCase;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ControleCargaPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.CulturaFontePort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcDatasetClientPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcPersistencePort;
import com.figueredo.ricardo.mvp.zarc.domain.model.ControleCarga;
import com.figueredo.ricardo.mvp.zarc.domain.model.RecursoZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcRegistro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class ImportarZarcService implements ImportarZarcUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImportarZarcService.class);
    private static final String FONTE_ZARC = "MAPA_ZARC";
    private static final String UF_ALVO = "SC";

    private final ZarcDatasetClientPort datasetClientPort;
    private final ZarcPersistencePort persistencePort;
    private final ControleCargaPort controleCargaPort;
    private final MunicipioPort municipioPort;
    private final CulturaFontePort culturaFontePort;

    private final List<String> safrasAlvo;
    private final int batchSize;

    public ImportarZarcService(
            ZarcDatasetClientPort datasetClientPort,
            ZarcPersistencePort persistencePort,
            ControleCargaPort controleCargaPort,
            MunicipioPort municipioPort,
            CulturaFontePort culturaFontePort,
            @Value("${app.jobs.zarc.safras-alvo:2024/2025,2025/2026}") List<String> safrasAlvo,
            @Value("${app.jobs.zarc.batch-size:1000}") int batchSize
    ) {
        this.datasetClientPort = datasetClientPort;
        this.persistencePort = persistencePort;
        this.controleCargaPort = controleCargaPort;
        this.municipioPort = municipioPort;
        this.culturaFontePort = culturaFontePort;
        this.safrasAlvo = safrasAlvo;
        this.batchSize = batchSize > 0 ? batchSize : 1000;
    }

    @Override
    public ResultadoImportacao importarTodasSafrasConfiguradas() {
        int totalSafras = 0;
        int totalRegistros = 0;
        List<String> detalhes = new ArrayList<>();

        List<RecursoZarc> recursos = datasetClientPort.buscarRecursosDisponiveis();

        for (String safra : safrasAlvo) {
            Optional<RecursoZarc> recursoOpt = encontrarRecursoPorSafra(recursos, safra);
            if (recursoOpt.isEmpty()) {
                detalhes.add("Safra " + safra + ": recurso não encontrado no CKAN");
                continue;
            }

            RecursoZarc recurso = recursoOpt.get();
            try {
                int importados = processarRecurso(recurso, safra);
                totalSafras++;
                totalRegistros += importados;
                detalhes.add("Safra " + safra + ": processados " + importados + " registros");
            } catch (Exception e) {
                log.error("Falha ao processar safra {}", safra, e);
                detalhes.add("Safra " + safra + ": erro - " + e.getMessage());
            }
        }

        return new ResultadoImportacao(totalSafras, totalRegistros, detalhes);
    }

    @Override
    public ResultadoImportacao importarSafra(String safraNome) {
        if (safraNome == null || safraNome.isBlank()) {
            return new ResultadoImportacao(0, 0, List.of("Safra inválida"));
        }

        List<RecursoZarc> recursos = datasetClientPort.buscarRecursosDisponiveis();
        Optional<RecursoZarc> recursoOpt = encontrarRecursoPorSafra(recursos, safraNome);

        if (recursoOpt.isEmpty()) {
            return new ResultadoImportacao(0, 0, List.of("Safra " + safraNome + ": recurso não encontrado no CKAN"));
        }

        RecursoZarc recurso = recursoOpt.get();
        try {
            int importados = processarRecurso(recurso, safraNome);
            return new ResultadoImportacao(1, importados, List.of("Safra " + safraNome + ": processados " + importados + " registros"));
        } catch (Exception e) {
            log.error("Falha ao processar safra {}", safraNome, e);
            return new ResultadoImportacao(0, 0, List.of("Safra " + safraNome + ": erro - " + e.getMessage()));
        }
    }

    private Optional<RecursoZarc> encontrarRecursoPorSafra(List<RecursoZarc> recursos, String safra) {
        String normalizada = safra.replace("/", "-").trim();
        String original = safra.trim();

        return recursos.stream()
                .filter(r -> "CSV".equalsIgnoreCase(r.formato()) || (r.url() != null && r.url().toLowerCase().endsWith(".csv")))
                .filter(r -> r.nome() != null && (r.nome().contains(original) || r.nome().contains(normalizada) || (r.url() != null && r.url().contains(normalizada))))
                .findFirst();
    }

    private int processarRecurso(RecursoZarc recurso, String safra) {
        Optional<ControleCarga> ultimaCarga = controleCargaPort.buscarUltimaCargaComSucesso(FONTE_ZARC, recurso.id());
        if (ultimaCarga.isPresent()) {
            String ultimoModificadoRegistrado = ultimaCarga.get().getUltimoModificadoFonte();
            if (ultimoModificadoRegistrado != null && ultimoModificadoRegistrado.equalsIgnoreCase(recurso.lastModified())) {
                log.info("Recurso ZARC para safra {} não foi alterado na fonte (last_modified={}). Ignorando importação.", safra, recurso.lastModified());
                return 0;
            }
        }

        ControleCarga controle = new ControleCarga(FONTE_ZARC, recurso.id(), recurso.lastModified());
        controle = controleCargaPort.salvar(controle);

        Map<String, Long> municipios = municipioPort.mapearIbgeParaIdPorUf(UF_ALVO);
        Map<String, Long> culturas = culturaFontePort.mapearCodigoOrigemParaIdPorFonte(FONTE_ZARC);

        int totalProcessados = 0;
        List<ZarcRegistro> lote = new ArrayList<>(batchSize);

        try (InputStream is = datasetClientPort.abrirStreamCsv(recurso.url());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.ISO_8859_1))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                controle.marcarSucesso(0);
                controleCargaPort.salvar(controle);
                return 0;
            }

            Map<String, Integer> colIndices = mapearColunas(headerLine);

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || !contemTokenUf(line)) {
                    continue;
                }

                String[] colunas = line.split(";", -1);
                ZarcRegistro registro = extrairRegistro(colunas, colIndices, municipios, culturas);
                if (registro != null) {
                    lote.add(registro);
                    if (lote.size() >= batchSize) {
                        int salvos = persistencePort.salvarLote(lote);
                        totalProcessados += salvos > 0 ? salvos : lote.size();
                        lote.clear();
                    }
                }
            }

            if (!lote.isEmpty()) {
                int salvos = persistencePort.salvarLote(lote);
                totalProcessados += salvos > 0 ? salvos : lote.size();
                lote.clear();
            }

            controle.marcarSucesso(totalProcessados);
            controleCargaPort.salvar(controle);
            log.info("Importação do ZARC safra {} finalizada com sucesso. Registros salvos: {}", safra, totalProcessados);
            return totalProcessados;

        } catch (Exception e) {
            controle.marcarErro(e.getMessage());
            controleCargaPort.salvar(controle);
            throw new RuntimeException("Erro ao processar importação do ZARC: " + e.getMessage(), e);
        }
    }

    private boolean contemTokenUf(String line) {
        String upper = line.toUpperCase();
        return upper.contains(";" + UF_ALVO + ";")
                || upper.contains(";\"" + UF_ALVO + "\";")
                || upper.startsWith(UF_ALVO + ";")
                || upper.startsWith("\"" + UF_ALVO + "\";")
                || upper.endsWith(";" + UF_ALVO)
                || upper.endsWith(";\"" + UF_ALVO + "\"");
    }

    private Map<String, Integer> mapearColunas(String headerLine) {
        Map<String, Integer> map = new HashMap<>();
        String limpa = headerLine.replace("\uFEFF", "");
        String[] cols = limpa.split(";");
        int idx = 0;
        for (String col : cols) {
            map.put(limparValorCsv(col).toLowerCase(), idx++);
        }
        return map;
    }

    private ZarcRegistro extrairRegistro(
            String[] colunas,
            Map<String, Integer> indices,
            Map<String, Long> municipios,
            Map<String, Long> culturas
    ) {
        String uf = obterValorColuna(colunas, indices, "uf");
        if (!UF_ALVO.equalsIgnoreCase(uf)) {
            return null;
        }

        String geocodigo = obterValorColuna(colunas, indices, "geocodigo");
        Long municipioId = municipios.get(geocodigo);
        if (municipioId == null) {
            return null;
        }

        String codCultura = obterValorColuna(colunas, indices, "cod_cultura");
        Long culturaId = culturas.get(codCultura);
        if (culturaId == null) {
            return null;
        }

        Integer safraIni = parseInteger(obterValorColuna(colunas, indices, "safraini"));
        Integer safraFim = parseInteger(obterValorColuna(colunas, indices, "safrafin"));
        String cicloCodigo = obterValorColuna(colunas, indices, "cod_ciclo");
        String soloCodigo = obterValorColuna(colunas, indices, "cod_solo");
        String manejoCodigo = obterValorColuna(colunas, indices, "cod_outros_manejos");
        String manejoDescricao = obterValorColuna(colunas, indices, "nome_outros_manejos");
        String portaria = obterValorColuna(colunas, indices, "portaria");

        if (safraIni == null || safraFim == null || cicloCodigo.isBlank() || soloCodigo.isBlank() || manejoCodigo.isBlank() || portaria.isBlank()) {
            return null;
        }

        List<Integer> decendios = IntStream.rangeClosed(1, 36)
                .mapToObj(d -> {
                    String valorDec = obterValorColuna(colunas, indices, "dec" + d);
                    Integer risco = parseInteger(valorDec);
                    if (risco == null || risco < 0 || risco > 100) {
                        return 0;
                    }
                    return risco;
                })
                .toList();

        String soloDescricao = TipoSoloZarc.buscarPorCodigoOuNome(soloCodigo)
                .map(TipoSoloZarc::getDescricaoCompleta)
                .orElse(null);

        return new ZarcRegistro(
                municipioId,
                culturaId,
                safraIni,
                safraFim,
                cicloCodigo,
                null,
                soloCodigo,
                soloDescricao,
                manejoCodigo,
                manejoDescricao,
                portaria,
                decendios
        );
    }

    private String obterValorColuna(String[] colunas, Map<String, Integer> indices, String nomeColuna) {
        Integer idx = indices.get(nomeColuna.toLowerCase());
        if (idx == null || idx >= colunas.length) {
            return "";
        }
        return limparValorCsv(colunas[idx]);
    }

    private String limparValorCsv(String valor) {
        if (valor == null) {
            return "";
        }
        String trimmed = valor.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1).trim();
        }
        return trimmed;
    }

    private Integer parseInteger(String val) {
        if (val == null || val.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
