package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarDashboardUseCase.FiltroDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.CulturaPossivelDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.IndicadorRiscoCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.MunicipioDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.ConsultarDashboardRequest;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse.CicloResumoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse.CriteriosPontuacaoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse.CulturaPossivelDashboardResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse.IndicadorRiscoDashboardResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse.JanelaPlantioResumoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse.MunicipioDashboardResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse.OportunidadeDashboardResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse.SoloResumoResponse;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
public class DashboardWebMapper {

    public FiltroDashboard toFiltro(ConsultarDashboardRequest request) {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.deTexto(
                request.mesInicio(),
                request.mesFim(),
                YearMonth.now()
        );
        TipoSoloZarc tipoSolo = request.soloCodigo() != null && !request.soloCodigo().isBlank()
                ? TipoSoloZarc.deCodigoOuNome(request.soloCodigo())
                : null;

        return new FiltroDashboard(
                request.codigoIbge(),
                periodo,
                tipoSolo,
                request.areaHa()
        );
    }

    public DashboardResponse toResponse(DashboardResultado resultado) {
        MunicipioDashboard m = resultado.municipio();
        MunicipioDashboardResponse municipioResponse = new MunicipioDashboardResponse(
                m.codigoIbge(),
                m.nome(),
                m.uf(),
                m.pracaReferenciaId(),
                m.pracaReferenciaNome(),
                m.observacaoRegional()
        );

        SoloResumoResponse soloSelecionadoResponse = resultado.soloSelecionado() != null
                ? toSoloResponse(resultado.soloSelecionado().toSoloZoneamento())
                : null;

        List<CulturaPossivelDashboardResponse> culturasPossiveis = resultado.culturasPossiveis().stream()
                .map(this::toCulturaPossivelResponse)
                .toList();

        List<OportunidadeDashboardResponse> ranking = resultado.rankingOportunidades().stream()
                .map(this::toOportunidadeResponse)
                .toList();

        OportunidadeDashboardResponse melhorOportunidade = resultado.melhorOportunidade() != null
                ? toOportunidadeResponse(resultado.melhorOportunidade())
                : null;

        List<IndicadorRiscoDashboardResponse> indicadores = resultado.indicadoresRisco().stream()
                .map(this::toIndicadorRiscoResponse)
                .toList();

        return new DashboardResponse(
                municipioResponse,
                resultado.mesInicio(),
                resultado.mesFim(),
                soloSelecionadoResponse,
                resultado.safraIni(),
                resultado.safraFim(),
                resultado.safra(),
                resultado.areaHa(),
                melhorOportunidade,
                culturasPossiveis,
                ranking,
                indicadores,
                resultado.avisosGerais()
        );
    }

    private CulturaPossivelDashboardResponse toCulturaPossivelResponse(CulturaPossivelDashboard cultura) {
        List<JanelaPlantioResumoResponse> janelas = cultura.janelasPlantio().stream()
                .map(this::toJanelaResponse)
                .toList();

        List<SoloResumoResponse> solos = cultura.solos().stream()
                .map(this::toSoloResponse)
                .toList();

        List<CicloResumoResponse> ciclos = cultura.ciclos().stream()
                .map(this::toCicloResponse)
                .toList();

        return new CulturaPossivelDashboardResponse(
                cultura.culturaId(),
                cultura.nomeCultura(),
                cultura.nomeCientifico(),
                cultura.grupoAgricola(),
                cultura.situacao(),
                cultura.epocaPlantioResumo(),
                toJanelaResponse(cultura.melhorPeriodoPlantio()),
                cultura.recomendacaoMelhorPeriodo(),
                toSoloResponse(cultura.soloProjecao()),
                cultura.riscoMinimo(),
                cultura.niveisRiscoDisponiveis(),
                janelas,
                solos,
                ciclos
        );
    }

    private JanelaPlantioResumoResponse toJanelaResponse(PeriodoDecendio periodo) {
        if (periodo == null) {
            return null;
        }
        return new JanelaPlantioResumoResponse(
                periodo.decendio(),
                periodo.mes(),
                periodo.nomeMes(),
                periodo.descricaoPeriodo(),
                periodo.menorRisco(),
                periodo.riscosDisponiveis()
        );
    }

    private SoloResumoResponse toSoloResponse(SoloZoneamento solo) {
        if (solo == null) {
            return null;
        }
        return new SoloResumoResponse(
                solo.codigo(),
                solo.tipoSolo(),
                solo.classificacao(),
                solo.teorArgila(),
                solo.descricao()
        );
    }

    private CicloResumoResponse toCicloResponse(CicloZoneamento ciclo) {
        return new CicloResumoResponse(ciclo.codigo(), ciclo.descricao());
    }

    private OportunidadeDashboardResponse toOportunidadeResponse(ScoreOportunidade score) {
        PontuacaoCriterios c = score.pontosPorCriterio();
        CriteriosPontuacaoResponse criteriosResponse = new CriteriosPontuacaoResponse(
                c.segurancaClimatica(),
                c.janelaPlantio(),
                c.dinamicaPrecos(),
                c.qualidadeInformacao(),
                c.notaFinal()
        );

        List<SoloResumoResponse> solosAptosResponse = score.solosAptos().stream()
                .map(this::toSoloResponse)
                .toList();

        return new OportunidadeDashboardResponse(
                score.culturaId(),
                score.nomeCultura(),
                score.nomeCientifico(),
                score.grupoAgricola(),
                score.posicaoRanking(),
                score.notaFinal(),
                criteriosResponse,
                score.riscoMinimoZarc(),
                score.quantidadeDecendiosFavoraveis(),
                score.quantidadeSolosAptos(),
                score.quantidadeCiclosAptos(),
                toJanelaResponse(score.melhorPeriodoPlantio()),
                score.recomendacaoMelhorPeriodo(),
                toSoloResponse(score.soloProjecao()),
                solosAptosResponse,
                score.produtoReferencia(),
                score.precoAtualReferencia(),
                score.unidadeComercializacao(),
                score.dataUltimaCotacao(),
                score.variacao30DiasPercentual(),
                score.variacao90DiasPercentual(),
                score.variacao180DiasPercentual(),
                score.pracaReferencia(),
                score.fonteReferencia(),
                score.fallbackEstadual(),
                score.possuiHistoricoPam(),
                score.ultimoAnoPam(),
                score.rendimentoMedioPamKgHa(),
                score.justificativas(),
                score.avisosMetodologicos()
        );
    }

    private IndicadorRiscoDashboardResponse toIndicadorRiscoResponse(IndicadorRiscoCultura indicador) {
        return new IndicadorRiscoDashboardResponse(
                indicador.culturaId(),
                indicador.nomeCultura(),
                indicador.riscoMinimo(),
                indicador.classificacaoRisco(),
                indicador.niveisRiscoDisponiveis(),
                indicador.totalDecendiosFavoraveis(),
                indicador.decendiosComRisco20(),
                indicador.decendiosComRisco30(),
                indicador.decendiosComRisco40()
        );
    }
}
