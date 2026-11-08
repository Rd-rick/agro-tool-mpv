package com.figueredo.ricardo.mvp.decisao.application.service;

import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.out.DecisaoHistoricoPamPort;
import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.decisao.domain.service.CalculadoraPontuacaoOportunidade;
import com.figueredo.ricardo.mvp.mercado.application.port.in.ConsultarMercadoCulturasUseCase;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CalcularRankingOportunidadesService implements CalcularRankingOportunidadesUseCase {

    private static final String AVISO_GERAL_RANKING =
            "O ranking de oportunidades cruza segurança climática (ZARC), amplitude da janela de plantio, tendência de preços (Epagri/Cepa), potencial de receita bruta municipal (IBGE/PAM) e qualidade dos dados oficiais.";

    private static final String AVISO_SEM_CULTURAS_APTAS =
            "Nenhuma cultura com janela favorável no ZARC foi encontrada para o município e safra consultados.";

    private final ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;
    private final ConsultarMercadoCulturasUseCase consultarMercadoUseCase;
    private final DecisaoHistoricoPamPort historicoPamPort;
    private final CalculadoraPontuacaoOportunidade calculadora;

    public CalcularRankingOportunidadesService(
            ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase,
            ConsultarMercadoCulturasUseCase consultarMercadoUseCase,
            DecisaoHistoricoPamPort historicoPamPort
    ) {
        this.consultarZoneamentoUseCase = Objects.requireNonNull(consultarZoneamentoUseCase, "consultarZoneamentoUseCase é obrigatório");
        this.consultarMercadoUseCase = Objects.requireNonNull(consultarMercadoUseCase, "consultarMercadoUseCase é obrigatório");
        this.historicoPamPort = Objects.requireNonNull(historicoPamPort, "historicoPamPort é obrigatório");
        this.calculadora = new CalculadoraPontuacaoOportunidade();
    }

    @Override
    public RankingOportunidadesResultado calcularRankingPorMunicipioESafra(FiltroRankingOportunidades filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");

        ZoneamentoMunicipioResultado zoneamentoResultado = consultarZoneamentoUseCase.consultarAptasPorMunicipioESafra(
                filtro.codigoIbge(),
                filtro.safraIni(),
                filtro.safraFim()
        );

        return calcularAPartirDoZoneamento(zoneamentoResultado, filtro.areaHa());
    }

    @Override
    public RankingOportunidadesResultado calcularRankingPorMunicipioEPeriodo(
            String codigoIbge,
            PeriodoPlantioMeses periodo
    ) {
        return calcularRankingPorMunicipioEPeriodo(codigoIbge, periodo, BigDecimal.ONE);
    }

    @Override
    public RankingOportunidadesResultado calcularRankingPorMunicipioEPeriodo(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            BigDecimal areaHa
    ) {
        FiltroRankingOportunidades validacao = new FiltroRankingOportunidades(codigoIbge, null, null, areaHa);
        ZoneamentoMunicipioResultado zoneamentoResultado = consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(
                validacao.codigoIbge(),
                periodo
        );

        return calcularAPartirDoZoneamento(zoneamentoResultado, validacao.areaHa());
    }

    @Override
    public RankingOportunidadesResultado calcularRankingPorMunicipioPeriodoESolo(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            TipoSoloZarc tipoSolo
    ) {
        return calcularRankingPorMunicipioPeriodoESolo(codigoIbge, periodo, tipoSolo, BigDecimal.ONE);
    }

    @Override
    public RankingOportunidadesResultado calcularRankingPorMunicipioPeriodoESolo(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            TipoSoloZarc tipoSolo,
            BigDecimal areaHa
    ) {
        Objects.requireNonNull(periodo, "periodo é obrigatório");
        Objects.requireNonNull(tipoSolo, "tipoSolo é obrigatório");
        FiltroRankingOportunidades validacao = new FiltroRankingOportunidades(codigoIbge, null, null, areaHa);
        ZoneamentoMunicipioResultado zoneamentoResultado = consultarZoneamentoUseCase.consultarAptasPorMunicipioPeriodoESolo(
                validacao.codigoIbge(),
                periodo,
                tipoSolo
        );

        return calcularAPartirDoZoneamento(zoneamentoResultado, validacao.areaHa());
    }

    private RankingOportunidadesResultado calcularAPartirDoZoneamento(
            ZoneamentoMunicipioResultado zoneamentoResultado,
            BigDecimal areaHa
    ) {
        Municipio municipio = zoneamentoResultado.municipio();
        List<CulturaZoneamentoConsolidado> culturasAptas = zoneamentoResultado.culturasAptas();

        if (culturasAptas.isEmpty()) {
            return new RankingOportunidadesResultado(
                    municipio.codigoIbge(),
                    municipio.nome(),
                    municipio.uf(),
                    municipio.observacaoPraca(),
                    zoneamentoResultado.safraIni(),
                    zoneamentoResultado.safraFim(),
                    null,
                    List.of(),
                    List.of(AVISO_SEM_CULTURAS_APTAS)
            );
        }

        List<Long> culturaIds = culturasAptas.stream()
                .map(CulturaZoneamentoConsolidado::culturaId)
                .toList();

        List<CulturaMercadoResultado> mercados = consultarMercadoUseCase.consultarMercadoPorMunicipioECulturas(
                municipio.codigoIbge(),
                culturaIds
        );

        Map<Long, CulturaMercadoResultado> mercadoPorCulturaId = mercados.stream()
                .collect(Collectors.toMap(
                        CulturaMercadoResultado::culturaId,
                        Function.identity(),
                        (existente, substituto) -> existente,
                        LinkedHashMap::new
                ));

        Map<Long, HistoricoPamCultura> pamPorCulturaId = historicoPamPort.buscarUltimoHistoricoPamPorMunicipioECulturas(
                municipio.codigoIbge(),
                culturaIds
        );

        LocalDate dataReferenciaCotacao = identificarMaiorDataCotacao(mercados);

        List<ScoreOportunidade> scoresCalculados = new ArrayList<>(culturasAptas.size());
        for (CulturaZoneamentoConsolidado culturaZarc : culturasAptas) {
            CulturaMercadoResultado mercadoDaCultura = mercadoPorCulturaId.get(culturaZarc.culturaId());
            HistoricoPamCultura pamDaCultura = pamPorCulturaId.getOrDefault(
                    culturaZarc.culturaId(),
                    HistoricoPamCultura.semHistorico(culturaZarc.culturaId())
            );

            ScoreOportunidade score = calculadora.calcular(
                    culturaZarc,
                    mercadoDaCultura,
                    pamDaCultura,
                    areaHa,
                    dataReferenciaCotacao
            );
            scoresCalculados.add(score);
        }

        scoresCalculados.sort(comparadorRanking());

        List<ScoreOportunidade> oportunidadesPosicaoAtribuida = new ArrayList<>(scoresCalculados.size());
        int posicao = 1;
        for (ScoreOportunidade item : scoresCalculados) {
            oportunidadesPosicaoAtribuida.add(item.comPosicaoRanking(posicao));
            posicao++;
        }

        String pracaRegional = resolverPracaRegionalMunicipio(oportunidadesPosicaoAtribuida, municipio);
        ScoreOportunidade melhor = oportunidadesPosicaoAtribuida.get(0);

        return new RankingOportunidadesResultado(
                municipio.codigoIbge(),
                municipio.nome(),
                municipio.uf(),
                pracaRegional,
                zoneamentoResultado.safraIni(),
                zoneamentoResultado.safraFim(),
                melhor,
                oportunidadesPosicaoAtribuida,
                List.of(AVISO_GERAL_RANKING)
        );
    }

    private String resolverPracaRegionalMunicipio(List<ScoreOportunidade> oportunidades, Municipio municipio) {
        for (ScoreOportunidade item : oportunidades) {
            if (!item.fallbackEstadual() && item.pracaReferencia() != null && !item.pracaReferencia().isBlank()) {
                return item.pracaReferencia();
            }
        }
        return municipio.observacaoPraca();
    }

    private LocalDate identificarMaiorDataCotacao(List<CulturaMercadoResultado> mercados) {
        LocalDate maiorData = null;
        for (CulturaMercadoResultado mercado : mercados) {
            for (ProdutoMercadoAnalise produto : mercado.produtos()) {
                LocalDate data = produto.dataUltimaCotacao();
                if (data != null && (maiorData == null || data.isAfter(maiorData))) {
                    maiorData = data;
                }
            }
        }
        return maiorData;
    }

    private Comparator<ScoreOportunidade> comparadorRanking() {
        return Comparator
                .comparingInt(ScoreOportunidade::notaFinal).reversed()
                .thenComparingInt(ScoreOportunidade::riscoMinimoZarc)
                .thenComparing(
                        (ScoreOportunidade s) -> s.pontosPorCriterio().potencialReceita(),
                        Comparator.reverseOrder()
                )
                .thenComparing(
                        (ScoreOportunidade s) -> s.pontosPorCriterio().dinamicaPrecos(),
                        Comparator.reverseOrder()
                )
                .thenComparing(ScoreOportunidade::nomeCultura, String.CASE_INSENSITIVE_ORDER);
    }
}
