package com.figueredo.ricardo.mvp.decisao.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.CulturaNaoEncontradaException;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.out.DecisaoHistoricoPamPort;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.IndicadoresProdutivosComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ItemComparacaoCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.JanelaPlantioComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.MercadoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.RiscoClimaticoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ScoreMotorDecisaoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ViabilidadeZoneamentoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.mercado.application.port.in.ConsultarMercadoCulturasUseCase;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CompararCulturasService implements CompararCulturasUseCase {

    private static final String FONTE_EPAGRI = "EPAGRI_CEPA";
    private static final String FONTE_CONAB = "CONAB";

    private static final String AVISO_COMPARACAO_FONTES =
            "As cotações da Epagri/Cepa e da CONAB preservam suas respectivas fontes, unidades de medida e metodologias, não devendo ser comparadas pelo valor nominal bruto.";

    private static final String AVISO_POTENCIAL_ECONOMICO =
            "Indicadores de produtividade municipal (IBGE/PAM) e preços de mercado são referenciais; a estimativa de potencial econômico e rentabilidade líquida depende dos custos operacionais e da produtividade efetiva de cada propriedade.";

    private final ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;
    private final ConsultarMercadoCulturasUseCase consultarMercadoUseCase;
    private final CalcularRankingOportunidadesUseCase calcularRankingUseCase;
    private final DecisaoHistoricoPamPort historicoPamPort;

    public CompararCulturasService(
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
    public ComparacaoCulturasResultado compararCulturas(FiltroCompararCulturas filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");

        ZoneamentoMunicipioResultado zoneamentoMunicipio = consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(
                filtro.codigoIbge(),
                filtro.periodoPlantio()
        );

        Municipio municipio = zoneamentoMunicipio.municipio();
        Integer safraIni = zoneamentoMunicipio.safraIni();
        Integer safraFim = zoneamentoMunicipio.safraFim();

        Map<Long, CulturaZoneamentoConsolidado> zarcPorCulturaId = zoneamentoMunicipio.culturasAptas().stream()
                .collect(Collectors.toMap(
                        CulturaZoneamentoConsolidado::culturaId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        List<CulturaMercadoResultado> mercados = consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(
                municipio.codigoIbge(),
                filtro.culturaIds()
        );

        Map<Long, CulturaMercadoResultado> mercadoPorCulturaId = mercados.stream()
                .collect(Collectors.toMap(
                        CulturaMercadoResultado::culturaId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        for (Long culturaId : filtro.culturaIds()) {
            if (!zarcPorCulturaId.containsKey(culturaId) && !mercadoPorCulturaId.containsKey(culturaId)) {
                throw new CulturaNaoEncontradaException(culturaId);
            }
        }

        RankingOportunidadesResultado rankingMunicipio = calcularRankingUseCase.calcularRankingPorMunicipioEPeriodo(
                municipio.codigoIbge(),
                filtro.periodoPlantio()
        );

        Map<Long, ScoreOportunidade> scorePorCulturaId = rankingMunicipio.oportunidades().stream()
                .collect(Collectors.toMap(
                        ScoreOportunidade::culturaId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Map<Long, List<HistoricoPamCultura>> seriesPamPorCultura = historicoPamPort
                .buscarSerieHistoricaPamPorMunicipioECulturas(municipio.codigoIbge(), filtro.culturaIds());

        List<ItemComparacaoCultura> itensComparados = new ArrayList<>(filtro.culturaIds().size());
        for (Long culturaId : filtro.culturaIds()) {
            CulturaZoneamentoConsolidado zarc = zarcPorCulturaId.get(culturaId);
            CulturaMercadoResultado mercado = mercadoPorCulturaId.get(culturaId);
            ScoreOportunidade score = scorePorCulturaId.get(culturaId);
            List<HistoricoPamCultura> seriePam = seriesPamPorCultura.getOrDefault(culturaId, List.of());

            itensComparados.add(montarItemComparacao(culturaId, zarc, mercado, score, seriePam));
        }

        String safraFormatada = safraIni != null && safraFim != null
                ? safraIni + "/" + safraFim
                : null;

        return new ComparacaoCulturasResultado(
                municipio,
                rankingMunicipio.pracaRegionalMunicipio(),
                filtro.tipoSolo(),
                safraIni,
                safraFim,
                safraFormatada,
                itensComparados,
                List.of(AVISO_COMPARACAO_FONTES, AVISO_POTENCIAL_ECONOMICO)
        );
    }

    private ItemComparacaoCultura montarItemComparacao(
            Long culturaId,
            CulturaZoneamentoConsolidado zarc,
            CulturaMercadoResultado mercado,
            ScoreOportunidade score,
            List<HistoricoPamCultura> seriePam
    ) {
        String nomeCultura = zarc != null
                ? zarc.nomeCultura()
                : (mercado != null ? mercado.nomeCultura() : "Cultura #" + culturaId);
        String nomeCientifico = zarc != null ? zarc.nomeCientifico() : null;
        String grupoAgricola = zarc != null ? zarc.grupoAgricola() : null;

        ViabilidadeZoneamentoComparacao viabilidade = montarViabilidade(zarc);
        JanelaPlantioComparacao janelas = montarJanelas(zarc);
        RiscoClimaticoComparacao risco = montarRisco(zarc);
        MercadoComparacao precos = montarMercado(mercado, score);
        ScoreMotorDecisaoComparacao scoreComparacao = montarScore(score);
        IndicadoresProdutivosComparacao produtivos = montarIndicadoresProdutivos(seriePam);

        return new ItemComparacaoCultura(
                culturaId,
                nomeCultura,
                nomeCientifico,
                grupoAgricola,
                viabilidade,
                janelas,
                risco,
                precos,
                scoreComparacao,
                produtivos
        );
    }

    private ViabilidadeZoneamentoComparacao montarViabilidade(CulturaZoneamentoConsolidado zarc) {
        if (zarc == null) {
            return new ViabilidadeZoneamentoComparacao(
                    false,
                    "Inapta ou sem janela favorável no ZARC para o município, período e solo",
                    0,
                    0,
                    List.of(),
                    List.of()
            );
        }
        return new ViabilidadeZoneamentoComparacao(
                true,
                "Apta conforme condições do ZARC",
                zarc.tiposSolo().size(),
                zarc.ciclos().size(),
                zarc.tiposSolo(),
                zarc.ciclos()
        );
    }

    private JanelaPlantioComparacao montarJanelas(CulturaZoneamentoConsolidado zarc) {
        if (zarc == null || zarc.janelasPlantio().isEmpty()) {
            return new JanelaPlantioComparacao(0, "Sem janela favorável", null, null, null, List.of());
        }
        List<PeriodoDecendio> periodos = zarc.janelasPlantio();
        PeriodoDecendio primeiro = periodos.get(0);
        PeriodoDecendio ultimo = periodos.get(periodos.size() - 1);
        String resumo = periodos.size() == 1
                ? String.format("%s (1 decêndio)", primeiro.descricaoPeriodo())
                : String.format("%s a %s (%d decêndios)", primeiro.descricaoPeriodo(), ultimo.descricaoPeriodo(), periodos.size());

        return new JanelaPlantioComparacao(
                periodos.size(),
                resumo,
                zarc.melhorPeriodoPlantio(),
                zarc.recomendacaoMelhorPeriodo(),
                zarc.soloProjecao(),
                periodos
        );
    }

    private RiscoClimaticoComparacao montarRisco(CulturaZoneamentoConsolidado zarc) {
        if (zarc == null) {
            return new RiscoClimaticoComparacao(null, "INAPTO", Set.of(), 0, 0, 0);
        }
        int dec20 = 0;
        int dec30 = 0;
        int dec40 = 0;
        for (PeriodoDecendio p : zarc.janelasPlantio()) {
            if (p.riscosDisponiveis().contains(20)) {
                dec20++;
            }
            if (p.riscosDisponiveis().contains(30)) {
                dec30++;
            }
            if (p.riscosDisponiveis().contains(40)) {
                dec40++;
            }
        }
        String classificacao = switch (zarc.riscoMinimo()) {
            case 20 -> "BAIXO (20%)";
            case 30 -> "MODERADO (30%)";
            case 40 -> "ELEVADO (40%)";
            default -> zarc.riscoMinimo() + "%";
        };
        return new RiscoClimaticoComparacao(
                zarc.riscoMinimo(),
                classificacao,
                zarc.niveisRiscoDisponiveis(),
                dec20,
                dec30,
                dec40
        );
    }

    private MercadoComparacao montarMercado(CulturaMercadoResultado mercado, ScoreOportunidade score) {
        List<ProdutoMercadoAnalise> todos = mercado != null ? mercado.produtos() : List.of();
        List<ProdutoMercadoAnalise> epagri = todos.stream()
                .filter(p -> FONTE_EPAGRI.equalsIgnoreCase(p.origem().fonte()))
                .toList();
        List<ProdutoMercadoAnalise> conab = todos.stream()
                .filter(p -> FONTE_CONAB.equalsIgnoreCase(p.origem().fonte()))
                .toList();

        ProdutoMercadoAnalise principal = todos.stream()
                .max(Comparator
                        .comparing((ProdutoMercadoAnalise p) -> FONTE_EPAGRI.equalsIgnoreCase(p.origem().fonte()))
                        .thenComparing(p -> !p.origem().fallbackEstadual())
                        .thenComparing(
                                ProdutoMercadoAnalise::dataUltimaCotacao,
                                Comparator.nullsFirst(Comparator.naturalOrder())
                        ))
                .orElse(null);

        if (score != null && score.produtoReferencia() != null) {
            return new MercadoComparacao(
                    score.produtoReferencia(),
                    score.precoAtualReferencia(),
                    score.unidadeComercializacao(),
                    score.dataUltimaCotacao(),
                    score.pracaReferencia(),
                    score.fonteReferencia(),
                    score.fallbackEstadual(),
                    score.variacao30DiasPercentual(),
                    score.variacao90DiasPercentual(),
                    score.variacao180DiasPercentual(),
                    epagri,
                    conab
            );
        }

        return new MercadoComparacao(
                principal != null ? principal.nomeComercial() : null,
                principal != null ? principal.precoAtualReferencia() : null,
                principal != null ? principal.unidadeComercializacao() : null,
                principal != null ? principal.dataUltimaCotacao() : null,
                principal != null ? principal.origem().pracaNome() : null,
                principal != null ? principal.origem().fonte() : null,
                principal != null && principal.origem().fallbackEstadual(),
                principal != null ? principal.variacao30DiasPercentual() : null,
                principal != null ? principal.variacao90DiasPercentual() : null,
                principal != null ? principal.variacao180DiasPercentual() : null,
                epagri,
                conab
        );
    }

    private ScoreMotorDecisaoComparacao montarScore(ScoreOportunidade score) {
        if (score == null) {
            return new ScoreMotorDecisaoComparacao(
                    false,
                    null,
                    0,
                    new PontuacaoCriterios(0, 0, 0, 0),
                    List.of("Cultura sem janela favorável no ZARC para o município, período e solo; pontuação de oportunidade não aplicável.")
            );
        }
        return new ScoreMotorDecisaoComparacao(
                true,
                score.posicaoRanking(),
                score.notaFinal(),
                score.pontosPorCriterio(),
                score.justificativas()
        );
    }

    private IndicadoresProdutivosComparacao montarIndicadoresProdutivos(List<HistoricoPamCultura> seriePam) {
        List<HistoricoPamCultura> validos = seriePam.stream()
                .filter(HistoricoPamCultura::possuiHistorico)
                .sorted(Comparator.comparing(
                        HistoricoPamCultura::ultimoAno,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();

        if (validos.isEmpty()) {
            return new IndicadoresProdutivosComparacao(
                    false,
                    null,
                    null,
                    null,
                    null,
                    null,
                    AVISO_POTENCIAL_ECONOMICO
            );
        }

        HistoricoPamCultura ultimo = validos.get(0);
        List<Integer> rendimentos = validos.stream()
                .map(HistoricoPamCultura::rendimentoMedioKgHa)
                .filter(r -> r != null && r > 0)
                .toList();

        Integer mediaRendimentos = null;
        if (!rendimentos.isEmpty()) {
            long soma = 0L;
            for (Integer r : rendimentos) {
                soma += r;
            }
            mediaRendimentos = Math.toIntExact(Math.round((double) soma / rendimentos.size()));
        }

        return new IndicadoresProdutivosComparacao(
                true,
                ultimo.ultimoAno(),
                ultimo.rendimentoMedioKgHa(),
                mediaRendimentos,
                ultimo.areaPlantadaHa(),
                ultimo.quantidadeProduzidaTon(),
                AVISO_POTENCIAL_ECONOMICO
        );
    }
}
