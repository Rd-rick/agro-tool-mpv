package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase.FiltroAnaliseCultura;
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
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.AlertaVariacaoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.AnaliseDetalhadaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.CicloItemResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.CombinacaoZarcDetalhadaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.ComparacaoCulturaMunicipioResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.DecendioMatrizResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.JanelaPlantioDetalhadaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.MelhorPeriodoComercializacaoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.MercadoAnaliseResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.MunicipioResumoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.OrigemCotacaoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.PontoGraficoPrecoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.ProdutoFonteCotacaoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.ProdutividadeMunicipalResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.RegistroAnualPamResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.ScoreResumoAnaliseResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.SeriePeriodoResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.SoloItemResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse.ZoneamentoAnaliseResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.ConsultarAnaliseCulturaRequest;
import com.figueredo.ricardo.mvp.mercado.domain.model.OrigemCotacao;
import com.figueredo.ricardo.mvp.mercado.domain.model.PontoSerieHistorica;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.mercado.domain.model.ResumoPeriodoMercado;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
public class AnaliseCulturaWebMapper {

    public FiltroAnaliseCultura toFiltro(ConsultarAnaliseCulturaRequest request) {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.deTexto(
                request.mesInicio(),
                request.mesFim(),
                YearMonth.now()
        );
        TipoSoloZarc tipoSolo = request.soloCodigo() != null && !request.soloCodigo().isBlank()
                ? TipoSoloZarc.deCodigoOuNome(request.soloCodigo())
                : null;

        return new FiltroAnaliseCultura(
                request.codigoIbge(),
                request.culturaId(),
                periodo,
                tipoSolo
        );
    }

    public AnaliseCulturaResponse toResponse(AnaliseCulturaResultado resultado) {
        return new AnaliseCulturaResponse(
                resultado.culturaId(),
                resultado.nomeCultura(),
                resultado.nomeCientifico(),
                resultado.grupoAgricola(),
                toZoneamentoResponse(resultado.zoneamento()),
                toMercadoResponse(resultado.mercado()),
                toAnaliseResponse(resultado.analise())
        );
    }

    private ZoneamentoAnaliseResponse toZoneamentoResponse(ZoneamentoSecao secao) {
        Municipio m = secao.municipio();
        MunicipioResumoResponse municipioResponse = new MunicipioResumoResponse(
                m.codigoIbge(),
                m.nome(),
                m.uf(),
                m.pracaReferenciaId(),
                m.observacaoPraca()
        );

        SoloItemResponse soloSelecionadoResponse = secao.soloSelecionado() != null
                ? toSoloResponse(secao.soloSelecionado().toSoloZoneamento())
                : null;

        List<CicloItemResponse> ciclos = secao.ciclos().stream()
                .map(this::toCicloResponse)
                .toList();

        List<SoloItemResponse> solos = secao.tiposSolo().stream()
                .map(this::toSoloResponse)
                .toList();

        List<JanelaPlantioDetalhadaResponse> janelas = secao.janelasPlantio().stream()
                .map(this::toJanelaResponse)
                .toList();

        List<DecendioMatrizResponse> matriz = secao.matriz36Decendios().stream()
                .map(this::toMatrizResponse)
                .toList();

        List<CombinacaoZarcDetalhadaResponse> combinacoes = secao.combinacoesDetalhadas().stream()
                .map(this::toCombinacaoResponse)
                .toList();

        return new ZoneamentoAnaliseResponse(
                municipioResponse,
                secao.safraIni(),
                secao.safraFim(),
                secao.safra(),
                soloSelecionadoResponse,
                secao.aptaZarc(),
                secao.situacao(),
                toJanelaResponse(secao.melhorPeriodoPlantio()),
                secao.recomendacaoMelhorPeriodo(),
                toSoloResponse(secao.soloProjecao()),
                secao.riscoMinimo(),
                secao.niveisRiscoDisponiveis(),
                secao.cultivaresENomesOrigem(),
                ciclos,
                solos,
                secao.portarias(),
                janelas,
                matriz,
                combinacoes
        );
    }

    private CicloItemResponse toCicloResponse(CicloZoneamento ciclo) {
        return new CicloItemResponse(ciclo.codigo(), ciclo.descricao());
    }

    private SoloItemResponse toSoloResponse(SoloZoneamento solo) {
        if (solo == null) {
            return null;
        }
        return new SoloItemResponse(
                solo.codigo(),
                solo.tipoSolo(),
                solo.classificacao(),
                solo.teorArgila(),
                solo.descricao()
        );
    }

    private JanelaPlantioDetalhadaResponse toJanelaResponse(PeriodoDecendio periodo) {
        if (periodo == null) {
            return null;
        }
        return new JanelaPlantioDetalhadaResponse(
                periodo.decendio(),
                periodo.mes(),
                periodo.nomeMes(),
                periodo.decendioNoMes(),
                periodo.diaInicio(),
                periodo.diaFim(),
                periodo.descricaoPeriodo(),
                periodo.menorRisco(),
                periodo.riscosDisponiveis()
        );
    }

    private DecendioMatrizResponse toMatrizResponse(CelulaDecendioMatriz celula) {
        return new DecendioMatrizResponse(
                celula.decendio(),
                celula.mes(),
                celula.nomeMes(),
                celula.decendioNoMes(),
                celula.diaInicio(),
                celula.diaFim(),
                celula.descricaoPeriodo(),
                celula.favoravel(),
                celula.menorRisco(),
                celula.riscosDisponiveis()
        );
    }

    private CombinacaoZarcDetalhadaResponse toCombinacaoResponse(ZarcZoneamentoItem item) {
        return new CombinacaoZarcDetalhadaResponse(
                item.nomeOrigemZarc(),
                item.cicloCodigo(),
                item.cicloDescricao(),
                item.soloCodigo(),
                item.soloDescricao(),
                item.manejoCodigo(),
                item.manejoDescricao(),
                item.portaria(),
                item.riscoMinimo(),
                item.decendios()
        );
    }

    private MercadoAnaliseResponse toMercadoResponse(MercadoSecao secao) {
        List<ProdutoFonteCotacaoResponse> epagri = secao.cotacoesEpagriCepa().stream()
                .map(this::toProdutoFonteResponse)
                .toList();

        List<ProdutoFonteCotacaoResponse> conab = secao.cotacoesConab().stream()
                .map(this::toProdutoFonteResponse)
                .toList();

        List<ComparacaoCulturaMunicipioResponse> comparacoes = secao.comparacaoDemaisCulturas().stream()
                .map(this::toComparacaoResponse)
                .toList();

        return new MercadoAnaliseResponse(
                secao.produtoReferencia(),
                secao.precoReferenciaAtual(),
                secao.unidadeMedidaReferencia(),
                secao.dataUltimaCotacao(),
                secao.pracaReferencia(),
                secao.fonteReferencia(),
                secao.fallbackEstadual(),
                secao.tendencia(),
                secao.variacao30d(),
                secao.variacao90d(),
                secao.variacao180d(),
                epagri,
                conab,
                comparacoes
        );
    }

    public ProdutoFonteCotacaoResponse toProdutoFonteResponse(ProdutoMercadoAnalise produto) {
        OrigemCotacao o = produto.origem();
        OrigemCotacaoResponse origemResponse = new OrigemCotacaoResponse(
                o.fonte(),
                o.pracaId(),
                o.pracaNome(),
                o.fallbackEstadual(),
                o.observacaoOrigem()
        );

        return new ProdutoFonteCotacaoResponse(
                produto.produtoId(),
                produto.nomeComercial(),
                produto.especificacao(),
                produto.unidadeComercializacao(),
                produto.pesoEmbalagemKg(),
                produto.frequenciaUtilizada(),
                produto.dataUltimaCotacao(),
                produto.precoAtualReferencia(),
                produto.variacao30DiasPercentual(),
                produto.variacao90DiasPercentual(),
                produto.variacao180DiasPercentual(),
                origemResponse,
                toSeriePeriodoResponse(produto.historico30Dias()),
                toSeriePeriodoResponse(produto.historico90Dias()),
                toSeriePeriodoResponse(produto.historico180Dias())
        );
    }

    private SeriePeriodoResponse toSeriePeriodoResponse(ResumoPeriodoMercado resumo) {
        if (resumo == null) {
            return null;
        }
        List<PontoGraficoPrecoResponse> pontos = resumo.serieHistorica().stream()
                .map(this::toPontoGraficoResponse)
                .toList();

        return new SeriePeriodoResponse(
                resumo.dias(),
                resumo.dataInicio(),
                resumo.dataFim(),
                resumo.precoInicial(),
                resumo.precoFinal(),
                resumo.precoMinimoPeriodo(),
                resumo.precoMaximoPeriodo(),
                resumo.precoMedioPeriodo(),
                resumo.variacaoPercentual(),
                pontos
        );
    }

    private PontoGraficoPrecoResponse toPontoGraficoResponse(PontoSerieHistorica ponto) {
        return new PontoGraficoPrecoResponse(
                ponto.data(),
                ponto.precoMinimo(),
                ponto.precoReferencia(),
                ponto.precoMaximo()
        );
    }

    private ComparacaoCulturaMunicipioResponse toComparacaoResponse(ComparacaoCulturaMunicipioItem item) {
        return new ComparacaoCulturaMunicipioResponse(
                item.culturaId(),
                item.nomeCultura(),
                item.posicaoRanking(),
                item.notaFinal(),
                item.riscoMinimoZarc(),
                item.recomendacaoMelhorPeriodo(),
                item.produtoReferencia(),
                item.precoAtualReferencia(),
                item.unidadeMedida(),
                item.praca(),
                item.fonte(),
                item.variacao30d(),
                item.variacao90d(),
                item.variacao180d()
        );
    }

    private AnaliseDetalhadaResponse toAnaliseResponse(AnaliseSecao secao) {
        ScoreResumoAnaliseResponse scoreResponse = toScoreResumoResponse(secao.scoreOportunidade());

        List<MelhorPeriodoComercializacaoResponse> melhores = secao.melhoresPeriodosComercializacao().stream()
                .map(this::toMelhorPeriodoResponse)
                .toList();

        List<AlertaVariacaoResponse> alertas = secao.alertasVariacoesRelevantes().stream()
                .map(this::toAlertaResponse)
                .toList();

        ProdutividadeMunicipalResponse produtividade = toProdutividadeResponse(secao.produtividadeMunicipal());

        return new AnaliseDetalhadaResponse(
                scoreResponse,
                secao.comportamentoPrecosResumo(),
                melhores,
                alertas,
                produtividade,
                secao.avisosMetodologicos()
        );
    }

    private ScoreResumoAnaliseResponse toScoreResumoResponse(ScoreOportunidade score) {
        if (score == null) {
            return null;
        }
        PontuacaoCriterios c = score.pontosPorCriterio();
        return new ScoreResumoAnaliseResponse(
                score.posicaoRanking(),
                score.notaFinal(),
                c.segurancaClimatica(),
                c.janelaPlantio(),
                c.dinamicaPrecos(),
                c.qualidadeInformacao(),
                toJanelaResponse(score.melhorPeriodoPlantio()),
                score.recomendacaoMelhorPeriodo(),
                score.justificativas()
        );
    }

    private MelhorPeriodoComercializacaoResponse toMelhorPeriodoResponse(MelhorPeriodoComercializacaoItem item) {
        return new MelhorPeriodoComercializacaoResponse(
                item.ano(),
                item.mes(),
                item.nomeMes(),
                item.precoMedioReferencia(),
                item.precoMaximoObservado(),
                item.quantidadeCotacoes(),
                item.produto(),
                item.unidadeMedida(),
                item.fonte()
        );
    }

    private AlertaVariacaoResponse toAlertaResponse(AlertaVariacaoPreco alerta) {
        return new AlertaVariacaoResponse(
                alerta.tipoAlerta(),
                alerta.severidade(),
                alerta.janelaReferencia(),
                alerta.percentualVariacaoOuOscilacao(),
                alerta.descricao()
        );
    }

    private ProdutividadeMunicipalResponse toProdutividadeResponse(ProdutividadeMunicipalAnalise prod) {
        List<RegistroAnualPamResponse> serie = prod.serieHistoricaAnual().stream()
                .map(this::toRegistroPamResponse)
                .toList();

        return new ProdutividadeMunicipalResponse(
                prod.possuiDadosPam(),
                prod.ultimoAno(),
                prod.ultimoRendimentoMedioKgHa(),
                prod.rendimentoMedioHistoricoKgHa(),
                prod.ultimaAreaPlantadaHa(),
                prod.ultimaQuantidadeProduzidaTon(),
                serie,
                prod.avisoCustosProducao()
        );
    }

    private RegistroAnualPamResponse toRegistroPamResponse(HistoricoPamCultura pam) {
        return new RegistroAnualPamResponse(
                pam.ultimoAno(),
                pam.areaPlantadaHa(),
                pam.quantidadeProduzidaTon(),
                pam.rendimentoMedioKgHa()
        );
    }
}
