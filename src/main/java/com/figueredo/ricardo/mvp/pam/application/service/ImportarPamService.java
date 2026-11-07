package com.figueredo.ricardo.mvp.pam.application.service;

import com.figueredo.ricardo.mvp.pam.application.port.in.ImportarPamUseCase;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamControleCargaPort;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamCulturaFontePort;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamDatasetClientPort;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamMunicipioPort;
import com.figueredo.ricardo.mvp.pam.application.port.out.PamPersistencePort;
import com.figueredo.ricardo.mvp.pam.domain.model.ControleCargaPam;
import com.figueredo.ricardo.mvp.pam.domain.model.PamProducaoRegistro;
import com.figueredo.ricardo.mvp.pam.domain.model.RecursoPam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ImportarPamService implements ImportarPamUseCase {

    private static final Logger log = LoggerFactory.getLogger(ImportarPamService.class);
    private static final String FONTE_PAM = "IBGE_PAM";
    private static final String UF_ALVO = "SC";
    private static final int ANO_MINIMO = 1974;
    private static final int ANO_MAXIMO = 2100;

    private static final String VAR_AREA_PLANTADA = "8331";
    private static final String VAR_AREA_COLHIDA = "216";
    private static final String VAR_QUANTIDADE_PRODUZIDA = "214";
    private static final String VAR_RENDIMENTO_MEDIO = "112";
    private static final String VAR_VALOR_PRODUCAO = "215";

    private final PamDatasetClientPort datasetClientPort;
    private final PamPersistencePort persistencePort;
    private final PamControleCargaPort controleCargaPort;
    private final PamMunicipioPort municipioPort;
    private final PamCulturaFontePort culturaFontePort;

    private final List<Integer> anosAlvo;
    private final int batchSize;

    public ImportarPamService(
            PamDatasetClientPort datasetClientPort,
            PamPersistencePort persistencePort,
            PamControleCargaPort controleCargaPort,
            PamMunicipioPort municipioPort,
            PamCulturaFontePort culturaFontePort,
            @Value("${app.jobs.pam.anos-alvo:2021,2022,2023,2024,2025}") List<Integer> anosAlvo,
            @Value("${app.jobs.pam.batch-size:1000}") int batchSize
    ) {
        this.datasetClientPort = datasetClientPort;
        this.persistencePort = persistencePort;
        this.controleCargaPort = controleCargaPort;
        this.municipioPort = municipioPort;
        this.culturaFontePort = culturaFontePort;
        this.anosAlvo = anosAlvo;
        this.batchSize = batchSize > 0 ? batchSize : 1000;
    }

    @Override
    public ResultadoImportacao importarTodosAnosConfigurados() {
        int totalAnos = 0;
        int totalRegistros = 0;
        List<String> detalhes = new ArrayList<>();

        List<RecursoPam> recursos = datasetClientPort.buscarRecursosDisponiveis();

        for (Integer ano : anosAlvo) {
            if (ano == null || ano < ANO_MINIMO || ano > ANO_MAXIMO) {
                detalhes.add("Ano " + ano + ": inválido");
                continue;
            }

            Optional<RecursoPam> recursoOpt = encontrarRecursoPorAno(recursos, ano);
            if (recursoOpt.isEmpty()) {
                detalhes.add("Ano " + ano + ": período não encontrado na API do IBGE");
                continue;
            }

            RecursoPam recurso = recursoOpt.get();
            try {
                int importados = processarRecurso(recurso);
                totalAnos++;
                totalRegistros += importados;
                detalhes.add("Ano " + ano + ": processados " + importados + " registros");
            } catch (Exception e) {
                log.error("Falha ao processar PAM para o ano {}", ano, e);
                detalhes.add("Ano " + ano + ": erro - " + e.getMessage());
            }
        }

        return new ResultadoImportacao(totalAnos, totalRegistros, detalhes);
    }

    @Override
    public ResultadoImportacao importarAno(int ano) {
        if (ano < ANO_MINIMO || ano > ANO_MAXIMO) {
            return new ResultadoImportacao(0, 0, List.of("Ano inválido: " + ano));
        }

        List<RecursoPam> recursos = datasetClientPort.buscarRecursosDisponiveis();
        Optional<RecursoPam> recursoOpt = encontrarRecursoPorAno(recursos, ano);

        if (recursoOpt.isEmpty()) {
            return new ResultadoImportacao(0, 0, List.of("Ano " + ano + ": período não encontrado na API do IBGE"));
        }

        RecursoPam recurso = recursoOpt.get();
        try {
            int importados = processarRecurso(recurso);
            return new ResultadoImportacao(1, importados, List.of("Ano " + ano + ": processados " + importados + " registros"));
        } catch (Exception e) {
            log.error("Falha ao processar PAM para o ano {}", ano, e);
            return new ResultadoImportacao(0, 0, List.of("Ano " + ano + ": erro - " + e.getMessage()));
        }
    }

    private Optional<RecursoPam> encontrarRecursoPorAno(List<RecursoPam> recursos, int ano) {
        return recursos.stream()
                .filter(r -> r.ano() == ano)
                .findFirst();
    }

    private int processarRecurso(RecursoPam recurso) {
        Optional<ControleCargaPam> ultimaCarga = controleCargaPort.buscarUltimaCargaComSucesso(
                FONTE_PAM,
                recurso.identificador()
        );

        if (ultimaCarga.isPresent()) {
            String ultimoModificadoRegistrado = ultimaCarga.get().getUltimoModificadoFonte();
            if (ultimoModificadoRegistrado != null && ultimoModificadoRegistrado.equalsIgnoreCase(recurso.ultimoModificado())) {
                log.info("Recurso PAM para o ano {} não foi alterado na fonte (last_modified={}). Ignorando importação.",
                        recurso.ano(), recurso.ultimoModificado());
                return 0;
            }
        }

        ControleCargaPam controle = new ControleCargaPam(
                FONTE_PAM,
                recurso.identificador(),
                recurso.ultimoModificado()
        );
        controle = controleCargaPort.salvar(controle);

        Map<String, Long> municipios = municipioPort.mapearIbgeParaIdPorUf(UF_ALVO);
        Map<String, Long> culturas = culturaFontePort.mapearCodigoOrigemParaIdPorFonte(FONTE_PAM);

        if (municipios.isEmpty() || culturas.isEmpty()) {
            controle.marcarSucesso(0);
            controleCargaPort.salvar(controle);
            return 0;
        }

        int totalProcessados = 0;
        List<PamProducaoRegistro> lote = new ArrayList<>(batchSize);
        Map<Long, AcumuladorMetricasPam> acumuladoresMunicipio = new LinkedHashMap<>();
        Long municipioAtualId = null;

        try (InputStream is = datasetClientPort.abrirStreamValoresAno(recurso.ano(), culturas.keySet());
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            Map<String, String> itemJson;
            while ((itemJson = lerProximoObjetoJson(reader)) != null) {
                String codigoMunicipio = itemJson.get("D1C");
                String codigoVariavel = itemJson.get("D2C");
                String codigoProduto = itemJson.get("D4C");
                String valorBruto = itemJson.get("V");

                if (codigoMunicipio == null || codigoVariavel == null || codigoProduto == null) {
                    continue;
                }

                Long municipioId = municipios.get(codigoMunicipio.trim());
                Long culturaFonteId = culturas.get(codigoProduto.trim());
                if (municipioId == null || culturaFonteId == null) {
                    continue;
                }

                if (municipioAtualId != null && !municipioAtualId.equals(municipioId)) {
                    descarregarMunicipioNoLote(municipioAtualId, recurso.ano(), acumuladoresMunicipio, lote);
                    acumuladoresMunicipio.clear();

                    if (lote.size() >= batchSize) {
                        totalProcessados += salvarELimparLote(lote);
                    }
                }

                municipioAtualId = municipioId;
                AcumuladorMetricasPam acumulador = acumuladoresMunicipio.computeIfAbsent(
                        culturaFonteId,
                        k -> new AcumuladorMetricasPam()
                );
                preencherVariavel(acumulador, codigoVariavel.trim(), valorBruto);
            }

            if (municipioAtualId != null && !acumuladoresMunicipio.isEmpty()) {
                descarregarMunicipioNoLote(municipioAtualId, recurso.ano(), acumuladoresMunicipio, lote);
                acumuladoresMunicipio.clear();
            }

            if (!lote.isEmpty()) {
                totalProcessados += salvarELimparLote(lote);
            }

            controle.marcarSucesso(totalProcessados);
            controleCargaPort.salvar(controle);
            log.info("Importação da PAM ano {} finalizada com sucesso. Registros salvos: {}", recurso.ano(), totalProcessados);
            return totalProcessados;

        } catch (Exception e) {
            controle.marcarErro(e.getMessage());
            controleCargaPort.salvar(controle);
            throw new RuntimeException("Erro ao processar importação da PAM para o ano " + recurso.ano() + ": " + e.getMessage(), e);
        }
    }

    private void descarregarMunicipioNoLote(
            Long municipioId,
            int ano,
            Map<Long, AcumuladorMetricasPam> acumuladores,
            List<PamProducaoRegistro> lote
    ) {
        for (Map.Entry<Long, AcumuladorMetricasPam> entry : acumuladores.entrySet()) {
            AcumuladorMetricasPam acc = entry.getValue();
            PamProducaoRegistro registro = new PamProducaoRegistro(
                    municipioId,
                    entry.getKey(),
                    ano,
                    acc.areaPlantadaHa,
                    acc.areaColhidaHa,
                    acc.quantidadeProduzidaTon,
                    acc.rendimentoMedioKgHa,
                    acc.valorProducaoMilReais
            );
            if (registro.possuiProducaoOuPlantio()) {
                lote.add(registro);
            }
        }
    }

    private int salvarELimparLote(List<PamProducaoRegistro> lote) {
        List<PamProducaoRegistro> copiaLote = List.copyOf(lote);
        int salvos = persistencePort.salvarLote(copiaLote);
        int contabilizados = salvos > 0 ? salvos : copiaLote.size();
        lote.clear();
        return contabilizados;
    }

    private void preencherVariavel(AcumuladorMetricasPam acumulador, String codigoVariavel, String valorBruto) {
        switch (codigoVariavel) {
            case VAR_AREA_PLANTADA -> acumulador.areaPlantadaHa = parseIntegerPositivoOuZero(valorBruto);
            case VAR_AREA_COLHIDA -> acumulador.areaColhidaHa = parseIntegerPositivoOuZero(valorBruto);
            case VAR_QUANTIDADE_PRODUZIDA -> acumulador.quantidadeProduzidaTon = parseIntegerPositivoOuZero(valorBruto);
            case VAR_RENDIMENTO_MEDIO -> acumulador.rendimentoMedioKgHa = parseIntegerPositivoOuZero(valorBruto);
            case VAR_VALOR_PRODUCAO -> acumulador.valorProducaoMilReais = parseBigDecimalPositivoOuZero(valorBruto);
            default -> {
            }
        }
    }

    private Integer parseIntegerPositivoOuZero(String valor) {
        if (!ehValorNumericoValido(valor)) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(valor.trim());
            return parsed >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimalPositivoOuZero(String valor) {
        if (!ehValorNumericoValido(valor)) {
            return null;
        }
        try {
            BigDecimal parsed = new BigDecimal(valor.trim().replace(",", "."));
            return parsed.compareTo(BigDecimal.ZERO) >= 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean ehValorNumericoValido(String valor) {
        if (valor == null) {
            return false;
        }
        String limpo = valor.trim();
        return !limpo.isEmpty()
                && !"-".equals(limpo)
                && !"..".equals(limpo)
                && !"...".equals(limpo)
                && !"X".equalsIgnoreCase(limpo);
    }

    private Map<String, String> lerProximoObjetoJson(Reader reader) throws IOException {
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == '{') {
                break;
            }
        }
        if (ch == -1) {
            return null;
        }

        Map<String, String> campos = new HashMap<>();
        String chaveAtual = null;
        boolean aguardandoValor = false;

        while ((ch = reader.read()) != -1) {
            if (ch == '}') {
                return campos;
            }
            if (ch == '"') {
                String texto = lerStringJson(reader);
                if (!aguardandoValor) {
                    chaveAtual = texto;
                } else {
                    if (chaveAtual != null) {
                        campos.put(chaveAtual, texto);
                    }
                    chaveAtual = null;
                    aguardandoValor = false;
                }
            } else if (ch == ':') {
                aguardandoValor = true;
            } else if (ch == ',') {
                aguardandoValor = false;
            }
        }
        return campos.isEmpty() ? null : campos;
    }

    private String lerStringJson(Reader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = reader.read()) != -1) {
            if (ch == '"') {
                break;
            }
            if (ch == '\\') {
                int escapado = reader.read();
                if (escapado == -1) {
                    break;
                }
                switch (escapado) {
                    case '"', '\\', '/' -> sb.append((char) escapado);
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        char[] hex = new char[4];
                        int lidos = reader.read(hex, 0, 4);
                        if (lidos == 4) {
                            sb.append((char) Integer.parseInt(new String(hex), 16));
                        }
                    }
                    default -> sb.append((char) escapado);
                }
            } else {
                sb.append((char) ch);
            }
        }
        return sb.toString();
    }

    private static class AcumuladorMetricasPam {
        private Integer areaPlantadaHa;
        private Integer areaColhidaHa;
        private Integer quantidadeProduzidaTon;
        private Integer rendimentoMedioKgHa;
        private BigDecimal valorProducaoMilReais;
    }
}
