package com.figueredo.ricardo.mvp.mercado.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.MunicipioNaoEncontradoException;
import com.figueredo.ricardo.mvp.mercado.application.port.in.ConsultarMercadoCulturasUseCase;
import com.figueredo.ricardo.mvp.mercado.application.port.out.MercadoConsultaPort;
import com.figueredo.ricardo.mvp.mercado.domain.model.CotacaoPrecoItem;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.MunicipioMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.OrigemCotacao;
import com.figueredo.ricardo.mvp.mercado.domain.model.PontoSerieHistorica;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoCotacaoMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.mercado.domain.model.RegiaoPracaMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ResumoPeriodoMercado;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConsultarMercadoCulturasService implements ConsultarMercadoCulturasUseCase {

    private static final String FONTE_EPAGRI = "EPAGRI_CEPA";
    private static final String UF_SC = "SC";
    private static final List<String> FREQUENCIAS_ORDEM_PREFERENCIA = List.of("DIARIA", "SEMANAL");
    private static final int JANELA_CURTA_DIAS = 30;
    private static final int JANELA_MEDIA_DIAS = 90;
    private static final int JANELA_LONGA_DIAS = 180;

    private final MercadoConsultaPort mercadoConsultaPort;

    public ConsultarMercadoCulturasService(MercadoConsultaPort mercadoConsultaPort) {
        this.mercadoConsultaPort = mercadoConsultaPort;
    }

    @Override
    public List<CulturaMercadoResultado> consultarMercadoPorMunicipioECulturas(FiltroMercadoCulturas filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");

        MunicipioMercado municipio = mercadoConsultaPort.buscarMunicipioPorCodigoIbge(filtro.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(filtro.codigoIbge()));

        if (filtro.culturaIds().isEmpty()) {
            return List.of();
        }

        List<CulturaMercado> culturas = mercadoConsultaPort.buscarCulturasAtivasPorIds(filtro.culturaIds());
        if (culturas.isEmpty()) {
            return List.of();
        }

        List<ProdutoCotacaoMercado> produtos = mercadoConsultaPort.buscarProdutosAtivosPorCulturasEFonte(
                filtro.culturaIds(),
                FONTE_EPAGRI
        );

        Map<Long, List<ProdutoCotacaoMercado>> produtosPorCultura = new LinkedHashMap<>();
        for (ProdutoCotacaoMercado produto : produtos) {
            produtosPorCultura.computeIfAbsent(produto.culturaId(), k -> new ArrayList<>()).add(produto);
        }

        List<RegiaoPracaMercado> pracasSc = mercadoConsultaPort.buscarPracasAtivasPorUf(UF_SC);
        Map<Long, RegiaoPracaMercado> mapaPracasSc = pracasSc.stream()
                .collect(Collectors.toMap(
                        RegiaoPracaMercado::id,
                        Function.identity(),
                        (existente, substituto) -> existente,
                        LinkedHashMap::new
                ));

        List<CulturaMercadoResultado> resultados = new ArrayList<>();
        for (CulturaMercado cultura : culturas) {
            List<ProdutoCotacaoMercado> produtosDaCultura = produtosPorCultura.getOrDefault(cultura.id(), List.of());
            List<ProdutoMercadoAnalise> analisesProdutos = new ArrayList<>();
            for (ProdutoCotacaoMercado produto : produtosDaCultura) {
                analisarProduto(produto, municipio, mapaPracasSc).ifPresent(analisesProdutos::add);
            }
            resultados.add(new CulturaMercadoResultado(
                    cultura.id(),
                    cultura.nome(),
                    analisesProdutos
            ));
        }

        resultados.sort(Comparator.comparing(CulturaMercadoResultado::nomeCultura, String.CASE_INSENSITIVE_ORDER));
        return resultados;
    }

    private Optional<ProdutoMercadoAnalise> analisarProduto(
            ProdutoCotacaoMercado produto,
            MunicipioMercado municipio,
            Map<Long, RegiaoPracaMercado> mapaPracasSc
    ) {
        Optional<SerieResolvida> serieLocal = tentarResolverPracaLocal(produto, municipio, mapaPracasSc);
        SerieResolvida serie = serieLocal.orElseGet(() -> tentarResolverFallbackEstadual(produto, mapaPracasSc).orElse(null));

        if (serie == null || serie.pontos().isEmpty()) {
            return Optional.empty();
        }

        List<PontoSerieHistorica> pontosOrdenados = serie.pontos();
        PontoSerieHistorica ultimoPonto = pontosOrdenados.get(pontosOrdenados.size() - 1);
        LocalDate dataUltima = ultimoPonto.data();
        BigDecimal precoAtual = ultimoPonto.precoReferencia();

        ResumoPeriodoMercado resumo30 = calcularResumoPeriodo(pontosOrdenados, dataUltima, JANELA_CURTA_DIAS);
        ResumoPeriodoMercado resumo90 = calcularResumoPeriodo(pontosOrdenados, dataUltima, JANELA_MEDIA_DIAS);
        ResumoPeriodoMercado resumo180 = calcularResumoPeriodo(pontosOrdenados, dataUltima, JANELA_LONGA_DIAS);

        return Optional.of(new ProdutoMercadoAnalise(
                produto.id(),
                produto.nomeComercial(),
                produto.especificacao(),
                produto.unidadeComercializacao(),
                produto.pesoEmbalagemKg(),
                serie.frequencia(),
                dataUltima,
                precoAtual,
                resumo30.variacaoPercentual(),
                resumo90.variacaoPercentual(),
                resumo180.variacaoPercentual(),
                serie.origem(),
                resumo30,
                resumo90,
                resumo180
        ));
    }

    private Optional<SerieResolvida> tentarResolverPracaLocal(
            ProdutoCotacaoMercado produto,
            MunicipioMercado municipio,
            Map<Long, RegiaoPracaMercado> mapaPracasSc
    ) {
        Long pracaLocalId = municipio.pracaReferenciaId();
        if (pracaLocalId == null) {
            return Optional.empty();
        }

        RegiaoPracaMercado pracaLocal = mapaPracasSc.get(pracaLocalId);
        String nomePraca = pracaLocal != null ? pracaLocal.nome() : "Praça Regional #" + pracaLocalId;

        for (String frequencia : FREQUENCIAS_ORDEM_PREFERENCIA) {
            Optional<LocalDate> ultimaDataOpt = mercadoConsultaPort.buscarUltimaDataCotacao(
                    produto.id(),
                    pracaLocalId,
                    frequencia
            );
            if (ultimaDataOpt.isEmpty()) {
                continue;
            }

            LocalDate dataFim = ultimaDataOpt.get();
            LocalDate dataInicio = dataFim.minusDays(JANELA_LONGA_DIAS);
            List<CotacaoPrecoItem> itens = mercadoConsultaPort.buscarSeriePorPracaEPeriodo(
                    produto.id(),
                    pracaLocalId,
                    frequencia,
                    dataInicio,
                    dataFim
            );
            if (itens.isEmpty()) {
                continue;
            }

            List<PontoSerieHistorica> pontos = itens.stream()
                    .map(item -> new PontoSerieHistorica(
                            item.dataCotacao(),
                            item.precoMinimo(),
                            item.precoReferencia(),
                            item.precoMaximo()
                    ))
                    .toList();

            String observacao = municipio.observacaoPraca() != null && !municipio.observacaoPraca().isBlank()
                    ? municipio.observacaoPraca()
                    : "Cotação obtida na praça regional de referência do município (" + nomePraca + ")";

            OrigemCotacao origem = new OrigemCotacao(
                    produto.fonte(),
                    pracaLocalId,
                    nomePraca,
                    false,
                    observacao
            );

            return Optional.of(new SerieResolvida(frequencia, origem, pontos));
        }

        return Optional.empty();
    }

    private Optional<SerieResolvida> tentarResolverFallbackEstadual(
            ProdutoCotacaoMercado produto,
            Map<Long, RegiaoPracaMercado> mapaPracasSc
    ) {
        if (mapaPracasSc.isEmpty()) {
            return Optional.empty();
        }

        Set<Long> pracaIdsSc = mapaPracasSc.keySet();

        for (String frequencia : FREQUENCIAS_ORDEM_PREFERENCIA) {
            Optional<LocalDate> ultimaDataOpt = mercadoConsultaPort.buscarUltimaDataCotacaoEmPracas(
                    produto.id(),
                    pracaIdsSc,
                    frequencia
            );
            if (ultimaDataOpt.isEmpty()) {
                continue;
            }

            LocalDate dataFim = ultimaDataOpt.get();
            LocalDate dataInicio = dataFim.minusDays(JANELA_LONGA_DIAS);
            List<CotacaoPrecoItem> itens = mercadoConsultaPort.buscarSerieEmPracasEPeriodo(
                    produto.id(),
                    pracaIdsSc,
                    frequencia,
                    dataInicio,
                    dataFim
            );
            if (itens.isEmpty()) {
                continue;
            }

            Set<Long> pracasComDados = new LinkedHashSet<>();
            for (CotacaoPrecoItem item : itens) {
                pracasComDados.add(item.pracaId());
            }

            List<String> nomesPracasComDados = pracasComDados.stream()
                    .map(id -> {
                        RegiaoPracaMercado p = mapaPracasSc.get(id);
                        return p != null ? p.nome() : "Praça #" + id;
                    })
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();

            List<PontoSerieHistorica> pontosConsolidados = consolidarPontosPorData(itens);

            OrigemCotacao origem;
            if (pracasComDados.size() == 1) {
                Long pracaUnicaId = pracasComDados.iterator().next();
                String nomeUnica = nomesPracasComDados.get(0);
                origem = new OrigemCotacao(
                        produto.fonte(),
                        pracaUnicaId,
                        nomeUnica,
                        true,
                        "Sem cotação na praça local do município; utilizada praça estadual de referência disponível em SC: " + nomeUnica
                );
            } else {
                String listaPracas = String.join(", ", nomesPracasComDados);
                origem = new OrigemCotacao(
                        produto.fonte(),
                        null,
                        "Média Estadual SC",
                        true,
                        "Sem cotação na praça local do município; utilizada média estadual consolidada de SC (praças: " + listaPracas + ")"
                );
            }

            return Optional.of(new SerieResolvida(frequencia, origem, pontosConsolidados));
        }

        return Optional.empty();
    }

    private List<PontoSerieHistorica> consolidarPontosPorData(List<CotacaoPrecoItem> itens) {
        Map<LocalDate, List<CotacaoPrecoItem>> porData = new TreeMap<>();
        for (CotacaoPrecoItem item : itens) {
            porData.computeIfAbsent(item.dataCotacao(), d -> new ArrayList<>()).add(item);
        }

        List<PontoSerieHistorica> consolidados = new ArrayList<>(porData.size());
        for (Map.Entry<LocalDate, List<CotacaoPrecoItem>> entry : porData.entrySet()) {
            List<CotacaoPrecoItem> doDia = entry.getValue();
            BigDecimal somaRef = BigDecimal.ZERO;
            BigDecimal menorMin = null;
            BigDecimal maiorMax = null;

            for (CotacaoPrecoItem item : doDia) {
                somaRef = somaRef.add(item.precoReferencia());
                BigDecimal minCandidato = item.precoMinimo() != null ? item.precoMinimo() : item.precoReferencia();
                BigDecimal maxCandidato = item.precoMaximo() != null ? item.precoMaximo() : item.precoReferencia();

                if (menorMin == null || minCandidato.compareTo(menorMin) < 0) {
                    menorMin = minCandidato;
                }
                if (maiorMax == null || maxCandidato.compareTo(maiorMax) > 0) {
                    maiorMax = maxCandidato;
                }
            }

            BigDecimal mediaRef = somaRef.divide(
                    BigDecimal.valueOf(doDia.size()),
                    2,
                    RoundingMode.HALF_UP
            );

            consolidados.add(new PontoSerieHistorica(
                    entry.getKey(),
                    menorMin,
                    mediaRef,
                    maiorMax
            ));
        }

        return consolidados;
    }

    private ResumoPeriodoMercado calcularResumoPeriodo(
            List<PontoSerieHistorica> todosPontos,
            LocalDate dataReferenciaFim,
            int diasJanela
    ) {
        LocalDate dataCorteInicio = dataReferenciaFim.minusDays(diasJanela);
        List<PontoSerieHistorica> pontosJanela = todosPontos.stream()
                .filter(p -> !p.data().isBefore(dataCorteInicio) && !p.data().isAfter(dataReferenciaFim))
                .toList();

        if (pontosJanela.isEmpty()) {
            return new ResumoPeriodoMercado(
                    diasJanela,
                    dataCorteInicio,
                    dataReferenciaFim,
                    null,
                    null,
                    null,
                    null,
                    null,
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    List.of()
            );
        }

        PontoSerieHistorica primeiro = pontosJanela.get(0);
        PontoSerieHistorica ultimo = pontosJanela.get(pontosJanela.size() - 1);

        BigDecimal precoInicial = primeiro.precoReferencia();
        BigDecimal precoFinal = ultimo.precoReferencia();
        BigDecimal variacaoPercentual = calcularVariacaoPercentual(precoInicial, precoFinal);

        BigDecimal soma = BigDecimal.ZERO;
        BigDecimal minimoPeriodo = null;
        BigDecimal maximoPeriodo = null;

        for (PontoSerieHistorica ponto : pontosJanela) {
            soma = soma.add(ponto.precoReferencia());
            BigDecimal minCandidato = ponto.precoMinimo() != null ? ponto.precoMinimo() : ponto.precoReferencia();
            BigDecimal maxCandidato = ponto.precoMaximo() != null ? ponto.precoMaximo() : ponto.precoReferencia();

            if (minimoPeriodo == null || minCandidato.compareTo(minimoPeriodo) < 0) {
                minimoPeriodo = minCandidato;
            }
            if (maximoPeriodo == null || maxCandidato.compareTo(maximoPeriodo) > 0) {
                maximoPeriodo = maxCandidato;
            }
        }

        BigDecimal mediaPeriodo = soma.divide(
                BigDecimal.valueOf(pontosJanela.size()),
                2,
                RoundingMode.HALF_UP
        );

        return new ResumoPeriodoMercado(
                diasJanela,
                primeiro.data(),
                ultimo.data(),
                precoInicial,
                precoFinal,
                minimoPeriodo,
                maximoPeriodo,
                mediaPeriodo,
                variacaoPercentual,
                pontosJanela
        );
    }

    private BigDecimal calcularVariacaoPercentual(BigDecimal precoInicial, BigDecimal precoFinal) {
        if (precoInicial == null || precoFinal == null || precoInicial.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return precoFinal.subtract(precoInicial)
                .multiply(BigDecimal.valueOf(100))
                .divide(precoInicial, 2, RoundingMode.HALF_UP);
    }

    private record SerieResolvida(
            String frequencia,
            OrigemCotacao origem,
            List<PontoSerieHistorica> pontos
    ) {
    }
}
