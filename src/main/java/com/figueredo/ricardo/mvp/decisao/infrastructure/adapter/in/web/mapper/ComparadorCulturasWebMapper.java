package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase.FiltroCompararCulturas;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.IndicadoresProdutivosComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ItemComparacaoCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.JanelaPlantioComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.MercadoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.RiscoClimaticoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ScoreMotorDecisaoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado.ViabilidadeZoneamentoComparacao;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasRequest;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.CicloComparadoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.CotacaoFonteResumoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.CulturaComparadaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.DecendioFavoravelComparadoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.IndicadoresProdutivosResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.JanelaPlantioComparadaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.MunicipioComparadorResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.PrecosEVariacaoComparadaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.RiscoClimaticoComparadoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.ScoreComparadoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.SoloComparadoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse.ViabilidadeZoneamentoResponse;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
public class ComparadorCulturasWebMapper {

    public FiltroCompararCulturas toFiltro(CompararCulturasRequest request) {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.deTexto(
                request.mesInicio(),
                request.mesFim(),
                YearMonth.now()
        );
        TipoSoloZarc tipoSolo = request.soloCodigo() != null && !request.soloCodigo().isBlank()
                ? TipoSoloZarc.deCodigoOuNome(request.soloCodigo())
                : null;

        return new FiltroCompararCulturas(
                request.codigoIbge(),
                periodo,
                tipoSolo,
                request.culturaIds()
        );
    }

    public CompararCulturasResponse toResponse(ComparacaoCulturasResultado resultado) {
        Municipio m = resultado.municipio();
        MunicipioComparadorResponse municipioResponse = new MunicipioComparadorResponse(
                m.codigoIbge(),
                m.nome(),
                m.uf(),
                m.pracaReferenciaId(),
                resultado.pracaRegionalMunicipio(),
                m.observacaoPraca()
        );

        SoloComparadoResponse soloSelecionado = resultado.soloSelecionado() != null
                ? toSoloResponse(resultado.soloSelecionado().toSoloZoneamento())
                : null;

        List<CulturaComparadaResponse> culturas = resultado.culturasComparadas().stream()
                .map(this::toCulturaComparadaResponse)
                .toList();

        return new CompararCulturasResponse(
                municipioResponse,
                soloSelecionado,
                resultado.safraIni(),
                resultado.safraFim(),
                resultado.safra(),
                culturas,
                resultado.avisosComparacao()
        );
    }

    private CulturaComparadaResponse toCulturaComparadaResponse(ItemComparacaoCultura item) {
        return new CulturaComparadaResponse(
                item.culturaId(),
                item.nomeCultura(),
                item.nomeCientifico(),
                item.grupoAgricola(),
                toViabilidadeResponse(item.viabilidadeZoneamento()),
                toJanelaResponse(item.janelasPlantio()),
                toRiscoResponse(item.riscoClimatico()),
                toPrecosResponse(item.precosEVariacao()),
                toScoreResponse(item.scoreMotorDecisao()),
                toIndicadoresProdutivosResponse(item.indicadoresProdutivos())
        );
    }

    private ViabilidadeZoneamentoResponse toViabilidadeResponse(ViabilidadeZoneamentoComparacao v) {
        List<SoloComparadoResponse> solos = v.tiposSolo().stream()
                .map(this::toSoloResponse)
                .toList();
        List<CicloComparadoResponse> ciclos = v.ciclos().stream()
                .map(this::toCicloResponse)
                .toList();
        return new ViabilidadeZoneamentoResponse(
                v.aptaZarc(),
                v.situacao(),
                v.quantidadeSolosAptos(),
                v.quantidadeCiclosAptos(),
                solos,
                ciclos
        );
    }

    private SoloComparadoResponse toSoloResponse(SoloZoneamento solo) {
        if (solo == null) {
            return null;
        }
        return new SoloComparadoResponse(
                solo.codigo(),
                solo.tipoSolo(),
                solo.classificacao(),
                solo.teorArgila(),
                solo.descricao()
        );
    }

    private CicloComparadoResponse toCicloResponse(CicloZoneamento ciclo) {
        return new CicloComparadoResponse(soloOrCiclo(ciclo.codigo()), ciclo.descricao());
    }

    private String soloOrCiclo(String val) {
        return val;
    }

    private JanelaPlantioComparadaResponse toJanelaResponse(JanelaPlantioComparacao j) {
        List<DecendioFavoravelComparadoResponse> periodos = j.periodosFavoraveis().stream()
                .map(this::toDecendioResponse)
                .toList();
        return new JanelaPlantioComparadaResponse(
                j.quantidadeDecendiosFavoraveis(),
                j.epocaPlantioResumo(),
                toDecendioResponse(j.melhorPeriodoPlantio()),
                j.recomendacaoMelhorPeriodo(),
                toSoloResponse(j.soloProjecao()),
                periodos
        );
    }

    private DecendioFavoravelComparadoResponse toDecendioResponse(PeriodoDecendio p) {
        if (p == null) {
            return null;
        }
        return new DecendioFavoravelComparadoResponse(
                p.decendio(),
                p.mes(),
                p.nomeMes(),
                p.descricaoPeriodo(),
                p.menorRisco(),
                p.riscosDisponiveis()
        );
    }

    private RiscoClimaticoComparadoResponse toRiscoResponse(RiscoClimaticoComparacao r) {
        return new RiscoClimaticoComparadoResponse(
                r.riscoMinimo(),
                r.classificacaoRisco(),
                r.niveisRiscoDisponiveis(),
                r.decendiosRisco20(),
                r.decendiosRisco30(),
                r.decendiosRisco40()
        );
    }

    private PrecosEVariacaoComparadaResponse toPrecosResponse(MercadoComparacao m) {
        List<CotacaoFonteResumoResponse> epagri = m.cotacoesEpagriCepa().stream()
                .map(this::toCotacaoFonteResumo)
                .toList();
        List<CotacaoFonteResumoResponse> conab = m.cotacoesConab().stream()
                .map(this::toCotacaoFonteResumo)
                .toList();

        return new PrecosEVariacaoComparadaResponse(
                m.produtoReferencia(),
                m.precoAtualReferencia(),
                m.unidadeComercializacao(),
                m.dataUltimaCotacao(),
                m.pracaReferencia(),
                m.fonteReferencia(),
                m.fallbackEstadual(),
                m.variacao30d(),
                m.variacao90d(),
                m.variacao180d(),
                epagri,
                conab
        );
    }

    private CotacaoFonteResumoResponse toCotacaoFonteResumo(ProdutoMercadoAnalise p) {
        return new CotacaoFonteResumoResponse(
                p.produtoId(),
                p.nomeComercial(),
                p.especificacao(),
                p.unidadeComercializacao(),
                p.pesoEmbalagemKg(),
                p.origem().fonte(),
                p.origem().pracaNome(),
                p.origem().fallbackEstadual(),
                p.dataUltimaCotacao(),
                p.precoAtualReferencia(),
                p.variacao30DiasPercentual(),
                p.variacao90DiasPercentual(),
                p.variacao180DiasPercentual()
        );
    }

    private ScoreComparadoResponse toScoreResponse(ScoreMotorDecisaoComparacao s) {
        PontuacaoCriterios c = s.pontosPorCriterio();
        return new ScoreComparadoResponse(
                s.elegivelRanking(),
                s.posicaoRankingMunicipio(),
                s.notaFinal(),
                c.segurancaClimatica(),
                c.janelaPlantio(),
                c.dinamicaPrecos(),
                c.qualidadeInformacao(),
                s.justificativas()
        );
    }

    private IndicadoresProdutivosResponse toIndicadoresProdutivosResponse(IndicadoresProdutivosComparacao ind) {
        return new IndicadoresProdutivosResponse(
                ind.possuiHistoricoPam(),
                ind.ultimoAnoPam(),
                ind.ultimoRendimentoMedioKgHa(),
                ind.rendimentoMedioHistoricoKgHa(),
                ind.ultimaAreaPlantadaHa(),
                ind.ultimaQuantidadeProduzidaTon(),
                ind.avisoPotencialEconomico()
        );
    }
}
