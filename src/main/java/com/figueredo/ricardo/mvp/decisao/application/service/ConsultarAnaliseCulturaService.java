package com.figueredo.ricardo.mvp.decisao.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.CulturaNaoEncontradaException;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.out.DecisaoHistoricoPamPort;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.AlertaVariacaoPreco;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.AnaliseSecao;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.CelulaDecendioMatriz;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.ComparacaoCulturaMunicipioItem;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.MelhorPeriodoComercializacaoItem;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.MercadoSecao;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.ProdutividadeMunicipalAnalise;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado.ZoneamentoSecao;
import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.mercado.application.port.in.ConsultarMercadoCulturasUseCase;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.PontoSerieHistorica;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.mercado.domain.model.ResumoPeriodoMercado;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class ConsultarAnaliseCulturaService implements ConsultarAnaliseCulturaUseCase {

    private static final String FONTE_EPAGRI = "EPAGRI_CEPA";
    private static final String FONTE_CONAB = "CONAB";
    private static final BigDecimal LIMIAR_ALERTA_PERCENTUAL = new BigDecimal("10.00");
    private static final BigDecimal LIMIAR_TENDENCIA = new BigDecimal("1.00");

    private static final String AVISO_CUSTOS_PRODUCAO =
            "Os dados históricos de produtividade média municipal (IBGE/PAM) e as cotações de mercado não incluem custos de produção, frete, insumos ou tributos, não devendo ser interpretados isoladamente como garantia de rentabilidade líquida.";

    private static final String AVISO_FONTES_MERCADO =
            "As cotações da Epagri/Cepa e da CONAB preservam suas respectivas fontes, unidades de comercialização, abrangência geográfica e metodologias, não devendo ser tratadas como equivalentes diretas sem verificação.";

    private static final List<String> NOMES_MESES = List.of(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    private static final List<Integer> ULTIMO_DIA_MES = List.of(
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    );

    private final ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;
    private final ConsultarMercadoCulturasUseCase consultarMercadoUseCase;
    private final CalcularRankingOportunidadesUseCase calcularRankingUseCase;
    private final DecisaoHistoricoPamPort historicoPamPort;

    public ConsultarAnaliseCulturaService(
            ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase,
            ConsultarMercadoCulturasUseCase consultarMercadoUseCase,
            CalcularRankingOportunidadesUseCase calcularRankingUseCase,
            DecisaoHistoricoPamPort historicoPamPort
    ) {
        this.consultarZoneamentoUseCase = Objects.requireNonNull(consultarZoneamentoUseCase, "consultarZoneamentoUseCase é obrigatório");
        this.consultarMercadoUseCase = Objects.requireNonNull(consultarMercadoUseCase, "consultarMercadoUseCase é obrigatório");
        this.calcularRankingUseCase = Objects.requireNonNull(calcularRankingUseCase, "calcularRankingUseCase é obrigatório");
        this.historicoPamPort = Objects.requireNonNull(historicoPamPort, "historicoPamPort é obrigatório");
    }

    @Override
    public AnaliseCulturaResultado consultarAnaliseCultura(FiltroAnaliseCultura filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");

        ZoneamentoMunicipioResultado zoneamentoMunicipio = consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(
                filtro.codigoIbge(),
                filtro.periodoPlantio()
        );

        Municipio municipio = zoneamentoMunicipio.municipio();
        Integer safraIni = zoneamentoMunicipio.safraIni();
        Integer safraFim = zoneamentoMunicipio.safraFim();

        Optional<CulturaZoneamentoConsolidado> culturaZarcOpt = zoneamentoMunicipio.culturasAptas().stream()
                .filter(c -> Objects.equals(c.culturaId(), filtro.culturaId()))
                .findFirst();

        List<CulturaMercadoResultado> mercadosCultura = consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(
                municipio.codigoIbge(),
                List.of(filtro.culturaId())
        );

        CulturaMercadoResultado mercadoDaCultura = mercadosCultura.stream()
                .filter(m -> Objects.equals(m.culturaId(), filtro.culturaId()))
                .findFirst()
                .orElse(null);

        if (culturaZarcOpt.isEmpty() && mercadoDaCultura == null) {
            throw new CulturaNaoEncontradaException(filtro.culturaId());
        }

        String nomeCultura = culturaZarcOpt.map(CulturaZoneamentoConsolidado::nomeCultura)
                .orElseGet(() -> mercadoDaCultura.nomeCultura());
        String nomeCientifico = culturaZarcOpt.map(CulturaZoneamentoConsolidado::nomeCientifico).orElse(null);
        String grupoAgricola = culturaZarcOpt.map(CulturaZoneamentoConsolidado::grupoAgricola).orElse(null);

        List<ZarcZoneamentoItem> itensDetalhadosZarc = consultarZoneamentoUseCase
                .consultarItensAptosPorMunicipioSafraECultura(
                        municipio.codigoIbge(),
                        safraIni,
                        safraFim,
                        filtro.culturaId()
                )
                .stream()
                .filter(item -> filtro.periodoPlantio().possuiDecendioFavoravelNoPeriodo(
                        item.decendios(),
                        ConsultarZoneamentoMunicipioUseCase.RISCOS_PERMITIDOS
                ))
                .toList();

        ZoneamentoSecao zoneamentoSecao = montarZoneamentoSecao(
                municipio,
                safraIni,
                safraFim,
                filtro.tipoSolo(),
                culturaZarcOpt.orElse(null),
                itensDetalhadosZarc
        );

        RankingOportunidadesResultado rankingMunicipio = calcularRankingUseCase.calcularRankingPorMunicipioEPeriodo(
                municipio.codigoIbge(),
                filtro.periodoPlantio()
        );

        ScoreOportunidade scoreDaCultura = rankingMunicipio.oportunidades().stream()
                .filter(s -> Objects.equals(s.culturaId(), filtro.culturaId()))
                .findFirst()
                .orElse(null);

        MercadoSecao mercadoSecao = montarMercadoSecao(
                filtro.culturaId(),
                mercadoDaCultura,
                scoreDaCultura,
                rankingMunicipio.oportunidades()
        );

        Map<Long, List<HistoricoPamCultura>> seriesPam = historicoPamPort.buscarSerieHistoricaPamPorMunicipioECulturas(
                municipio.codigoIbge(),
                List.of(filtro.culturaId())
        );
        List<HistoricoPamCultura> seriePamCultura = seriesPam.getOrDefault(filtro.culturaId(), List.of());

        AnaliseSecao analiseSecao = montarAnaliseSecao(
                nomeCultura,
                scoreDaCultura,
                mercadoDaCultura,
                seriePamCultura
        );

        return new AnaliseCulturaResultado(
                filtro.culturaId(),
                nomeCultura,
                nomeCientifico,
                grupoAgricola,
                zoneamentoSecao,
                mercadoSecao,
                analiseSecao
        );
    }

    private ZoneamentoSecao montarZoneamentoSecao(
            Municipio municipio,
            Integer safraIni,
            Integer safraFim,
            TipoSoloZarc soloSelecionado,
            CulturaZoneamentoConsolidado culturaZarc,
            List<ZarcZoneamentoItem> itensDetalhados
    ) {
        String safraFormatada = safraIni != null && safraFim != null
                ? safraIni + "/" + safraFim
                : null;

        boolean apta = culturaZarc != null;
        String situacao = apta
                ? "Apta conforme condições do ZARC"
                : "Sem janela favorável no ZARC para o município, período e solo consultados";

        List<PeriodoDecendio> janelas = apta ? culturaZarc.janelasPlantio() : List.of();
        List<CelulaDecendioMatriz> matriz36 = construirMatriz36Decendios(janelas);

        return new ZoneamentoSecao(
                municipio,
                safraIni,
                safraFim,
                safraFormatada,
                soloSelecionado,
                apta,
                situacao,
                apta ? culturaZarc.melhorPeriodoPlantio() : null,
                apta ? culturaZarc.recomendacaoMelhorPeriodo() : null,
                apta ? culturaZarc.soloProjecao() : null,
                apta ? culturaZarc.riscoMinimo() : null,
                apta ? culturaZarc.niveisRiscoDisponiveis() : Set.of(),
                apta ? culturaZarc.nomesOrigemZarc() : List.of(),
                apta ? culturaZarc.ciclos() : List.of(),
                apta ? culturaZarc.tiposSolo() : List.of(),
                apta ? culturaZarc.portarias() : List.of(),
                janelas,
                matriz36,
                itensDetalhados != null ? List.copyOf(itensDetalhados) : List.of()
        );
    }

    private List<CelulaDecendioMatriz> construirMatriz36Decendios(List<PeriodoDecendio> janelasFavoraveis) {
        Map<Integer, PeriodoDecendio> porDecendio = janelasFavoraveis.stream()
                .collect(Collectors.toMap(
                        PeriodoDecendio::decendio,
                        p -> p,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        return IntStream.rangeClosed(1, 36)
                .mapToObj(numeroDecendio -> {
                    PeriodoDecendio favoravel = porDecendio.get(numeroDecendio);
                    if (favoravel != null) {
                        return new CelulaDecendioMatriz(
                                favoravel.decendio(),
                                favoravel.mes(),
                                favoravel.nomeMes(),
                                favoravel.decendioNoMes(),
                                favoravel.diaInicio(),
                                favoravel.diaFim(),
                                favoravel.descricaoPeriodo(),
                                true,
                                favoravel.menorRisco(),
                                favoravel.riscosDisponiveis()
                        );
                    }
                    int indice = numeroDecendio - 1;
                    int mes = (indice / 3) + 1;
                    int decNoMes = (indice % 3) + 1;
                    String nomeMes = NOMES_MESES.get(mes - 1);
                    int diaIni = switch (decNoMes) {
                        case 1 -> 1;
                        case 2 -> 11;
                        default -> 21;
                    };
                    int diaFim = switch (decNoMes) {
                        case 1 -> 10;
                        case 2 -> 20;
                        default -> ULTIMO_DIA_MES.get(mes - 1);
                    };
                    String descricao = String.format("%02d a %02d de %s", diaIni, diaFim, nomeMes);
                    return new CelulaDecendioMatriz(
                            numeroDecendio,
                            mes,
                            nomeMes,
                            decNoMes,
                            diaIni,
                            diaFim,
                            descricao,
                            false,
                            null,
                            Set.of()
                    );
                })
                .toList();
    }

    private MercadoSecao montarMercadoSecao(
            Long culturaId,
            CulturaMercadoResultado mercadoDaCultura,
            ScoreOportunidade scoreDaCultura,
            List<ScoreOportunidade> oportunidadesMunicipio
    ) {
        List<ProdutoMercadoAnalise> todosProdutos = mercadoDaCultura != null
                ? mercadoDaCultura.produtos()
                : List.of();

        List<ProdutoMercadoAnalise> cotacoesEpagri = todosProdutos.stream()
                .filter(p -> FONTE_EPAGRI.equalsIgnoreCase(p.origem().fonte()))
                .toList();

        List<ProdutoMercadoAnalise> cotacoesConab = todosProdutos.stream()
                .filter(p -> FONTE_CONAB.equalsIgnoreCase(p.origem().fonte()))
                .toList();

        ProdutoMercadoAnalise produtoPrincipal = selecionarProdutoPrincipal(todosProdutos);

        String produtoRef = scoreDaCultura != null && scoreDaCultura.produtoReferencia() != null
                ? scoreDaCultura.produtoReferencia()
                : (produtoPrincipal != null ? produtoPrincipal.nomeComercial() : null);

        BigDecimal precoRef = scoreDaCultura != null && scoreDaCultura.precoAtualReferencia() != null
                ? scoreDaCultura.precoAtualReferencia()
                : (produtoPrincipal != null ? produtoPrincipal.precoAtualReferencia() : null);

        String unidadeRef = scoreDaCultura != null && scoreDaCultura.unidadeComercializacao() != null
                ? scoreDaCultura.unidadeComercializacao()
                : (produtoPrincipal != null ? produtoPrincipal.unidadeComercializacao() : null);

        var dataUltima = scoreDaCultura != null && scoreDaCultura.dataUltimaCotacao() != null
                ? scoreDaCultura.dataUltimaCotacao()
                : (produtoPrincipal != null ? produtoPrincipal.dataUltimaCotacao() : null);

        String pracaRef = scoreDaCultura != null && scoreDaCultura.pracaReferencia() != null
                ? scoreDaCultura.pracaReferencia()
                : (produtoPrincipal != null ? produtoPrincipal.origem().pracaNome() : null);

        String fonteRef = scoreDaCultura != null && scoreDaCultura.fonteReferencia() != null
                ? scoreDaCultura.fonteReferencia()
                : (produtoPrincipal != null ? produtoPrincipal.origem().fonte() : null);

        boolean fallback = scoreDaCultura != null
                ? scoreDaCultura.fallbackEstadual()
                : (produtoPrincipal != null && produtoPrincipal.origem().fallbackEstadual());

        BigDecimal var30 = scoreDaCultura != null && scoreDaCultura.variacao30DiasPercentual() != null
                ? scoreDaCultura.variacao30DiasPercentual()
                : (produtoPrincipal != null ? produtoPrincipal.variacao30DiasPercentual() : null);

        BigDecimal var90 = scoreDaCultura != null && scoreDaCultura.variacao90DiasPercentual() != null
                ? scoreDaCultura.variacao90DiasPercentual()
                : (produtoPrincipal != null ? produtoPrincipal.variacao90DiasPercentual() : null);

        BigDecimal var180 = scoreDaCultura != null && scoreDaCultura.variacao180DiasPercentual() != null
                ? scoreDaCultura.variacao180DiasPercentual()
                : (produtoPrincipal != null ? produtoPrincipal.variacao180DiasPercentual() : null);

        String tendencia = classificarTendencia(var30, var90, var180);

        List<ComparacaoCulturaMunicipioItem> comparacaoDemais = oportunidadesMunicipio.stream()
                .filter(op -> !Objects.equals(op.culturaId(), culturaId))
                .map(op -> new ComparacaoCulturaMunicipioItem(
                        op.culturaId(),
                        op.nomeCultura(),
                        op.posicaoRanking(),
                        op.notaFinal(),
                        op.riscoMinimoZarc(),
                        op.recomendacaoMelhorPeriodo(),
                        op.produtoReferencia(),
                        op.precoAtualReferencia(),
                        op.unidadeComercializacao(),
                        op.pracaReferencia(),
                        op.fonteReferencia(),
                        op.variacao30DiasPercentual(),
                        op.variacao90DiasPercentual(),
                        op.variacao180DiasPercentual()
                ))
                .toList();

        return new MercadoSecao(
                produtoRef,
                precoRef,
                unidadeRef,
                dataUltima,
                pracaRef,
                fonteRef,
                fallback,
                tendencia,
                var30,
                var90,
                var180,
                cotacoesEpagri,
                cotacoesConab,
                comparacaoDemais
        );
    }

    private AnaliseSecao montarAnaliseSecao(
            String nomeCultura,
            ScoreOportunidade scoreDaCultura,
            CulturaMercadoResultado mercadoDaCultura,
            List<HistoricoPamCultura> seriePamCultura
    ) {
        List<ProdutoMercadoAnalise> produtos = mercadoDaCultura != null ? mercadoDaCultura.produtos() : List.of();
        ProdutoMercadoAnalise principal = selecionarProdutoPrincipal(produtos);

        String resumoComportamento = gerarResumoComportamentoPrecos(nomeCultura, principal);
        List<MelhorPeriodoComercializacaoItem> melhoresPeriodos = identificarMelhoresMesesComercializacao(principal);
        List<AlertaVariacaoPreco> alertas = identificarAlertasVariacao(principal);
        ProdutividadeMunicipalAnalise produtividade = montarProdutividadeMunicipal(seriePamCultura);

        List<String> avisos = new ArrayList<>();
        if (scoreDaCultura != null && !scoreDaCultura.avisosMetodologicos().isEmpty()) {
            avisos.addAll(scoreDaCultura.avisosMetodologicos());
        } else {
            avisos.add(AVISO_CUSTOS_PRODUCAO);
        }
        avisos.add(AVISO_FONTES_MERCADO);

        return new AnaliseSecao(
                scoreDaCultura,
                resumoComportamento,
                melhoresPeriodos,
                alertas,
                produtividade,
                avisos
        );
    }

    private ProdutoMercadoAnalise selecionarProdutoPrincipal(List<ProdutoMercadoAnalise> produtos) {
        if (produtos == null || produtos.isEmpty()) {
            return null;
        }
        return produtos.stream()
                .max(Comparator
                        .comparing((ProdutoMercadoAnalise p) -> FONTE_EPAGRI.equalsIgnoreCase(p.origem().fonte()))
                        .thenComparing(p -> !p.origem().fallbackEstadual())
                        .thenComparing(
                                ProdutoMercadoAnalise::dataUltimaCotacao,
                                Comparator.nullsFirst(Comparator.naturalOrder())
                        ))
                .orElse(produtos.get(0));
    }

    private String classificarTendencia(BigDecimal var30, BigDecimal var90, BigDecimal var180) {
        if (var30 == null && var90 == null && var180 == null) {
            return "SEM_DADOS";
        }
        BigDecimal referencia = var90 != null ? var90 : (var30 != null ? var30 : var180);
        if (referencia.compareTo(LIMIAR_TENDENCIA) > 0) {
            return "VALORIZACAO";
        }
        if (referencia.compareTo(LIMIAR_TENDENCIA.negate()) < 0) {
            return "DESVALORIZACAO";
        }
        return "ESTAVEL";
    }

    private String gerarResumoComportamentoPrecos(String nomeCultura, ProdutoMercadoAnalise produto) {
        if (produto == null) {
            return String.format(
                    "Não há série histórica de preços ativa para %s nas bases consultadas (Epagri/Cepa e CONAB).",
                    nomeCultura
            );
        }
        BigDecimal v30 = produto.variacao30DiasPercentual() != null ? produto.variacao30DiasPercentual() : BigDecimal.ZERO;
        BigDecimal v90 = produto.variacao90DiasPercentual() != null ? produto.variacao90DiasPercentual() : BigDecimal.ZERO;
        BigDecimal v180 = produto.variacao180DiasPercentual() != null ? produto.variacao180DiasPercentual() : BigDecimal.ZERO;

        return String.format(
                "Para '%s' (%s - %s, praça %s), a cotação mais recente é R$ %s (%s), registrando variação de %s%% em 30 dias, %s%% em 90 dias e %s%% em 180 dias.",
                produto.nomeComercial(),
                produto.unidadeComercializacao(),
                produto.origem().fonte(),
                produto.origem().pracaNome(),
                produto.precoAtualReferencia() != null ? produto.precoAtualReferencia().toPlainString() : "N/D",
                produto.dataUltimaCotacao(),
                v30.toPlainString(),
                v90.toPlainString(),
                v180.toPlainString()
        );
    }

    private List<MelhorPeriodoComercializacaoItem> identificarMelhoresMesesComercializacao(ProdutoMercadoAnalise produto) {
        if (produto == null || produto.historico180Dias() == null || produto.historico180Dias().serieHistorica().isEmpty()) {
            return List.of();
        }

        Map<YearMonth, List<PontoSerieHistorica>> porAnoMes = new LinkedHashMap<>();
        for (PontoSerieHistorica ponto : produto.historico180Dias().serieHistorica()) {
            YearMonth ym = YearMonth.from(ponto.data());
            porAnoMes.computeIfAbsent(ym, k -> new ArrayList<>()).add(ponto);
        }

        List<MelhorPeriodoComercializacaoItem> itens = new ArrayList<>();
        for (Map.Entry<YearMonth, List<PontoSerieHistorica>> entry : porAnoMes.entrySet()) {
            YearMonth ym = entry.getKey();
            List<PontoSerieHistorica> pontos = entry.getValue();

            BigDecimal soma = BigDecimal.ZERO;
            BigDecimal maximo = null;
            for (PontoSerieHistorica p : pontos) {
                soma = soma.add(p.precoReferencia());
                BigDecimal candidatoMax = p.precoMaximo() != null ? p.precoMaximo() : p.precoReferencia();
                if (maximo == null || candidatoMax.compareTo(maximo) > 0) {
                    maximo = candidatoMax;
                }
            }

            BigDecimal media = soma.divide(BigDecimal.valueOf(pontos.size()), 2, RoundingMode.HALF_UP);
            String nomeMes = NOMES_MESES.get(ym.getMonthValue() - 1);

            itens.add(new MelhorPeriodoComercializacaoItem(
                    ym.getYear(),
                    ym.getMonthValue(),
                    nomeMes,
                    media,
                    maximo,
                    pontos.size(),
                    produto.nomeComercial(),
                    produto.unidadeComercializacao(),
                    produto.origem().fonte()
            ));
        }

        itens.sort(Comparator.comparing(MelhorPeriodoComercializacaoItem::precoMedioReferencia).reversed());
        return itens;
    }

    private List<AlertaVariacaoPreco> identificarAlertasVariacao(ProdutoMercadoAnalise produto) {
        if (produto == null) {
            return List.of();
        }

        List<AlertaVariacaoPreco> alertas = new ArrayList<>();
        avaliarAlertaPeriodo(produto.historico30Dias(), "30 dias", alertas);
        avaliarAlertaPeriodo(produto.historico90Dias(), "90 dias", alertas);
        avaliarAlertaPeriodo(produto.historico180Dias(), "180 dias", alertas);
        return alertas;
    }

    private void avaliarAlertaPeriodo(
            ResumoPeriodoMercado resumo,
            String rotuloJanela,
            List<AlertaVariacaoPreco> alertas
    ) {
        if (resumo == null) {
            return;
        }

        BigDecimal variacao = resumo.variacaoPercentual();
        if (variacao != null && variacao.abs().compareTo(LIMIAR_ALERTA_PERCENTUAL) >= 0) {
            boolean alta = variacao.compareTo(BigDecimal.ZERO) > 0;
            String tipo = alta ? "VALORIZACAO_ACENTUADA" : "DESVALORIZACAO_ACENTUADA";
            String severidade = variacao.abs().compareTo(new BigDecimal("20.00")) >= 0 ? "ALTA" : "MEDIA";
            String descricao = String.format(
                    "Variação acumulada de %s%% na janela de %s (acima do limiar de alerta de 10%%).",
                    variacao.toPlainString(),
                    rotuloJanela
            );
            alertas.add(new AlertaVariacaoPreco(tipo, severidade, rotuloJanela, variacao, descricao));
        }

        BigDecimal min = resumo.precoMinimoPeriodo();
        BigDecimal max = resumo.precoMaximoPeriodo();
        if (min != null && max != null && min.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal amplitude = max.subtract(min)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(min, 2, RoundingMode.HALF_UP);
            if (amplitude.compareTo(LIMIAR_ALERTA_PERCENTUAL) >= 0) {
                String descricao = String.format(
                        "Oscilação de %s%% entre a mínima (R$ %s) e a máxima (R$ %s) observadas em %s.",
                        amplitude.toPlainString(),
                        min.toPlainString(),
                        max.toPlainString(),
                        rotuloJanela
                );
                alertas.add(new AlertaVariacaoPreco(
                        "OSCILACAO_INTRAPERIODO",
                        amplitude.compareTo(new BigDecimal("20.00")) >= 0 ? "ALTA" : "MEDIA",
                        rotuloJanela,
                        amplitude,
                        descricao
                ));
            }
        }
    }

    private ProdutividadeMunicipalAnalise montarProdutividadeMunicipal(List<HistoricoPamCultura> seriePam) {
        List<HistoricoPamCultura> comHistorico = seriePam.stream()
                .filter(HistoricoPamCultura::possuiHistorico)
                .sorted(Comparator.comparing(
                        HistoricoPamCultura::ultimoAno,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        if (comHistorico.isEmpty()) {
            return new ProdutividadeMunicipalAnalise(
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    List.of(),
                    AVISO_CUSTOS_PRODUCAO
            );
        }

        HistoricoPamCultura maisRecente = comHistorico.get(0);

        List<Integer> rendimentosValidos = comHistorico.stream()
                .map(HistoricoPamCultura::rendimentoMedioKgHa)
                .filter(r -> r != null && r > 0)
                .toList();

        Integer mediaHistorica = null;
        if (!rendimentosValidos.isEmpty()) {
            long soma = 0L;
            for (Integer r : rendimentosValidos) {
                soma += r;
            }
            mediaHistorica = Math.toIntExact(Math.round((double) soma / rendimentosValidos.size()));
        }

        return new ProdutividadeMunicipalAnalise(
                true,
                maisRecente.ultimoAno(),
                maisRecente.rendimentoMedioKgHa(),
                mediaHistorica,
                maisRecente.areaPlantadaHa(),
                maisRecente.quantidadeProduzidaTon(),
                comHistorico,
                AVISO_CUSTOS_PRODUCAO
        );
    }
}
