package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.client;

import com.figueredo.ricardo.mvp.epagri.domain.model.CotacaoPrecoRegistro;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EpagriSaxSheetHandler implements XSSFSheetXMLHandler.SheetContentsHandler {

    private static final Logger log = LoggerFactory.getLogger(EpagriSaxSheetHandler.class);

    private static final Map<String, Integer> MESES = Map.ofEntries(
            Map.entry("janeiro", 1),
            Map.entry("fevereiro", 2),
            Map.entry("marco", 3),
            Map.entry("março", 3),
            Map.entry("abril", 4),
            Map.entry("maio", 5),
            Map.entry("junho", 6),
            Map.entry("julho", 7),
            Map.entry("agosto", 8),
            Map.entry("setembro", 9),
            Map.entry("outubro", 10),
            Map.entry("novembro", 11),
            Map.entry("dezembro", 12)
    );

    private final FrequenciaCotacao frequencia;
    private final Consumer<List<CotacaoPrecoRegistro>> consumidorLote;
    private final int batchSize;
    private final Map<String, Long> mapaProdutos;
    private final Map<String, Long> mapaPracas;

    private final Map<Integer, String> linhaAtual = new HashMap<>();
    private final List<CotacaoPrecoRegistro> loteAtual = new ArrayList<>();

    private boolean cabecalhoEncontrado = false;
    private int colAno = -1;
    private int colMes = -1;
    private int colDia = -1;
    private int colProduto = -1;
    private int colPraca = -1;
    private int colMin = -1;
    private int colRef = -1;
    private int colMax = -1;

    private int totalProcessados = 0;

    public EpagriSaxSheetHandler(
            FrequenciaCotacao frequencia,
            Consumer<List<CotacaoPrecoRegistro>> consumidorLote,
            int batchSize,
            Map<String, Long> mapaProdutos,
            Map<String, Long> mapaPracas
    ) {
        this.frequencia = frequencia;
        this.consumidorLote = consumidorLote;
        this.batchSize = batchSize;
        this.mapaProdutos = normalizarMapa(mapaProdutos);
        this.mapaPracas = normalizarMapa(mapaPracas);
    }

    @Override
    public void startRow(int rowNum) {
        linhaAtual.clear();
    }

    @Override
    public void endRow(int rowNum) {
        if (linhaAtual.isEmpty()) {
            return;
        }

        if (!cabecalhoEncontrado) {
            tentarIdentificarCabecalho();
            return;
        }

        processarLinhaDados();
    }

    @Override
    public void cell(String cellReference, String formattedValue, XSSFComment comment) {
        if (cellReference == null) {
            return;
        }
        int colIndex = extrairIndiceColuna(cellReference);
        linhaAtual.put(colIndex, formattedValue != null ? formattedValue.trim() : "");
    }

    @Override
    public void headerFooter(String text, boolean isHeader, String tagName) {
    }

    public void finalizar() {
        if (!loteAtual.isEmpty()) {
            consumidorLote.accept(new ArrayList<>(loteAtual));
            totalProcessados += loteAtual.size();
            loteAtual.clear();
        }
    }

    public int getTotalProcessados() {
        return totalProcessados;
    }

    private void tentarIdentificarCabecalho() {
        boolean temProduto = false;
        boolean temPraca = false;

        for (Map.Entry<Integer, String> entry : linhaAtual.entrySet()) {
            String val = normalizarTexto(entry.getValue());
            if (val.contains("produto")) {
                colProduto = entry.getKey();
                temProduto = true;
            } else if (val.contains("praca") || val.contains("nom_praca")) {
                colPraca = entry.getKey();
                temPraca = true;
            } else if (val.contains("ano")) {
                colAno = entry.getKey();
            } else if (val.contains("mes")) {
                colMes = entry.getKey();
            } else if (val.equals("dia")) {
                colDia = entry.getKey();
            } else if (val.contains("minimo") || val.contains("val. minimo")) {
                colMin = entry.getKey();
            } else if (val.contains("comum") || val.contains("referencia") || val.contains("val. mais comum")) {
                colRef = entry.getKey();
            } else if (val.contains("maximo") || val.contains("val. maximo")) {
                colMax = entry.getKey();
            }
        }

        if (temProduto && temPraca && colRef >= 0 && colAno >= 0 && colMes >= 0 && colDia >= 0) {
            cabecalhoEncontrado = true;
        }
    }

    private void processarLinhaDados() {
        String anoStr = linhaAtual.get(colAno);
        String mesStr = linhaAtual.get(colMes);
        String diaStr = linhaAtual.get(colDia);
        String produtoStr = linhaAtual.get(colProduto);
        String pracaStr = linhaAtual.get(colPraca);
        String refStr = linhaAtual.get(colRef);

        if (anoStr == null || mesStr == null || diaStr == null || produtoStr == null || pracaStr == null || refStr == null) {
            return;
        }

        LocalDate data = parseData(anoStr, mesStr, diaStr);
        if (data == null) {
            return;
        }

        Long produtoId = mapaProdutos.get(normalizarTexto(produtoStr));
        if (produtoId == null) {
            return;
        }

        Long pracaId = mapaPracas.get(normalizarTexto(pracaStr));
        if (pracaId == null) {
            return;
        }

        BigDecimal precoReferencia = parseBigDecimal(refStr);
        if (precoReferencia == null || precoReferencia.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal precoMinimo = parseBigDecimal(linhaAtual.get(colMin));
        BigDecimal precoMaximo = parseBigDecimal(linhaAtual.get(colMax));

        CotacaoPrecoRegistro registro = new CotacaoPrecoRegistro(
                produtoId,
                pracaId,
                data,
                precoMinimo,
                precoReferencia,
                precoMaximo,
                frequencia
        );

        loteAtual.add(registro);
        if (loteAtual.size() >= batchSize) {
            consumidorLote.accept(new ArrayList<>(loteAtual));
            totalProcessados += loteAtual.size();
            loteAtual.clear();
        }
    }

    private LocalDate parseData(String anoStr, String mesStr, String diaStr) {
        try {
            int ano = Integer.parseInt(anoStr.trim());
            String mesLimpo = normalizarTexto(mesStr);
            Integer mes = MESES.get(mesLimpo);
            if (mes == null) {
                return null;
            }
            int dia = Integer.parseInt(diaStr.trim());
            return LocalDate.of(ano, mes, dia);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String valorStr) {
        if (valorStr == null || valorStr.isBlank()) {
            return null;
        }
        try {
            String normalizado = valorStr.trim().replace(".", "").replace(',', '.');
            if (valorStr.contains(".") && !valorStr.contains(",")) {
                normalizado = valorStr.trim();
            }
            return new BigDecimal(normalizado);
        } catch (Exception e) {
            return null;
        }
    }

    private int extrairIndiceColuna(String cellReference) {
        int col = 0;
        for (char c : cellReference.toCharArray()) {
            if (Character.isLetter(c)) {
                col = col * 26 + (Character.toUpperCase(c) - 'A' + 1);
            } else {
                break;
            }
        }
        return col - 1;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.trim().toLowerCase();
    }

    private Map<String, Long> normalizarMapa(Map<String, Long> mapaOrigem) {
        Map<String, Long> normalizado = new HashMap<>();
        for (Map.Entry<String, Long> entry : mapaOrigem.entrySet()) {
            if (entry.getKey() != null) {
                normalizado.put(normalizarTexto(entry.getKey()), entry.getValue());
            }
        }
        return normalizado;
    }
}
