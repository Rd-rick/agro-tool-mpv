package com.figueredo.ricardo.mvp.decisao.application.service;

import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarDashboardUseCase;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.CulturaPossivelDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.IndicadorRiscoCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado.MunicipioDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ConsultarDashboardService implements ConsultarDashboardUseCase {

    private static final String SITUACAO_APTA_ZARC = "Apta conforme condições do ZARC";

    private final ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;
    private final CalcularRankingOportunidadesUseCase calcularRankingUseCase;

    public ConsultarDashboardService(
            ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase,
            CalcularRankingOportunidadesUseCase calcularRankingUseCase
    ) {
        this.consultarZoneamentoUseCase = Objects.requireNonNull(consultarZoneamentoUseCase, "consultarZoneamentoUseCase é obrigatório");
        this.calcularRankingUseCase = Objects.requireNonNull(calcularRankingUseCase, "calcularRankingUseCase é obrigatório");
    }

    @Override
    public DashboardResultado consultarDashboard(FiltroDashboard filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");

        ZoneamentoMunicipioResultado zoneamento = consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(
                filtro.codigoIbge(),
                filtro.periodoPlantio()
        );

        RankingOportunidadesResultado ranking = calcularRankingUseCase.calcularRankingPorMunicipioEPeriodo(
                filtro.codigoIbge(),
                filtro.periodoPlantio(),
                filtro.areaHa()
        );

        Municipio municipio = zoneamento.municipio();
        MunicipioDashboard municipioDashboard = new MunicipioDashboard(
                municipio.codigoIbge(),
                municipio.nome(),
                municipio.uf(),
                municipio.pracaReferenciaId(),
                ranking.pracaRegionalMunicipio(),
                municipio.observacaoPraca()
        );

        List<CulturaPossivelDashboard> culturasPossiveis = zoneamento.culturasAptas().stream()
                .map(this::montarCulturaPossivel)
                .toList();

        List<IndicadorRiscoCultura> indicadoresRisco = zoneamento.culturasAptas().stream()
                .map(this::montarIndicadorRisco)
                .toList();

        String safraFormatada = zoneamento.safraIni() != null && zoneamento.safraFim() != null
                ? zoneamento.safraIni() + "/" + zoneamento.safraFim()
                : null;

        return new DashboardResultado(
                municipioDashboard,
                filtro.periodoPlantio().mesInicio().toString(),
                filtro.periodoPlantio().mesFim().toString(),
                filtro.tipoSolo(),
                zoneamento.safraIni(),
                zoneamento.safraFim(),
                safraFormatada,
                filtro.areaHa(),
                ranking.melhorOportunidade(),
                culturasPossiveis,
                ranking.oportunidades(),
                indicadoresRisco,
                ranking.avisosGerais()
        );
    }

    private CulturaPossivelDashboard montarCulturaPossivel(CulturaZoneamentoConsolidado cultura) {
        List<PeriodoDecendio> janelas = cultura.janelasPlantio();
        String epocaResumo = resumirEpocaPlantio(janelas);

        return new CulturaPossivelDashboard(
                cultura.culturaId(),
                cultura.nomeCultura(),
                cultura.nomeCientifico(),
                cultura.grupoAgricola(),
                SITUACAO_APTA_ZARC,
                epocaResumo,
                cultura.melhorPeriodoPlantio(),
                cultura.recomendacaoMelhorPeriodo(),
                cultura.soloProjecao(),
                cultura.riscoMinimo(),
                cultura.niveisRiscoDisponiveis(),
                janelas,
                cultura.tiposSolo(),
                cultura.ciclos()
        );
    }

    private IndicadorRiscoCultura montarIndicadorRisco(CulturaZoneamentoConsolidado cultura) {
        int dec20 = 0;
        int dec30 = 0;
        int dec40 = 0;

        for (PeriodoDecendio janela : cultura.janelasPlantio()) {
            if (janela.riscosDisponiveis().contains(20)) {
                dec20++;
            }
            if (janela.riscosDisponiveis().contains(30)) {
                dec30++;
            }
            if (janela.riscosDisponiveis().contains(40)) {
                dec40++;
            }
        }

        String classificacao = classificarNivelRisco(cultura.riscoMinimo());

        return new IndicadorRiscoCultura(
                cultura.culturaId(),
                cultura.nomeCultura(),
                cultura.riscoMinimo(),
                classificacao,
                cultura.niveisRiscoDisponiveis(),
                cultura.janelasPlantio().size(),
                dec20,
                dec30,
                dec40
        );
    }

    private String resumirEpocaPlantio(List<PeriodoDecendio> janelas) {
        if (janelas == null || janelas.isEmpty()) {
            return "Sem janela favorável";
        }
        PeriodoDecendio primeira = janelas.get(0);
        PeriodoDecendio ultima = janelas.get(janelas.size() - 1);
        if (janelas.size() == 1) {
            return String.format("%s (1 decêndio)", primeira.descricaoPeriodo());
        }
        return String.format(
                "%s a %s (%d decêndios)",
                primeira.descricaoPeriodo(),
                ultima.descricaoPeriodo(),
                janelas.size()
        );
    }

    private String classificarNivelRisco(Integer riscoMinimo) {
        if (riscoMinimo == null) {
            return "INAPTO";
        }
        return switch (riscoMinimo) {
            case 20 -> "BAIXO (20%)";
            case 30 -> "MODERADO (30%)";
            case 40 -> "ELEVADO (40%)";
            default -> riscoMinimo + "%";
        };
    }
}
