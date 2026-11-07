package com.figueredo.ricardo.mvp.zarc.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.MunicipioNaoEncontradoException;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcZoneamentoQueryPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.SafraVigencia;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;
import org.springframework.stereotype.Service;

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
import java.util.TreeSet;

@Service
public class ConsultarZoneamentoMunicipioService implements ConsultarZoneamentoMunicipioUseCase {

    private static final Set<Integer> RISCOS_FAVORAVEIS = Set.of(20, 30, 40);

    private final MunicipioPort municipioPort;
    private final ZarcZoneamentoQueryPort zarcZoneamentoQueryPort;

    public ConsultarZoneamentoMunicipioService(
            MunicipioPort municipioPort,
            ZarcZoneamentoQueryPort zarcZoneamentoQueryPort
    ) {
        this.municipioPort = municipioPort;
        this.zarcZoneamentoQueryPort = zarcZoneamentoQueryPort;
    }

    @Override
    public ZoneamentoMunicipioResultado consultarAptasPorMunicipioESafra(FiltroZoneamentoMunicipio filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");
        return consultarAptasPorMunicipioComFiltros(new FiltroDetalhadoZoneamentoMunicipio(
                filtro.codigoIbge(),
                filtro.safraIni(),
                filtro.safraFim(),
                null,
                null,
                null
        ));
    }

    @Override
    public ZoneamentoMunicipioResultado consultarAptasPorMunicipioEPeriodo(
            String codigoIbge,
            PeriodoPlantioMeses periodo
    ) {
        FiltroZoneamentoMunicipio validacaoIbge = new FiltroZoneamentoMunicipio(codigoIbge, null, null);
        if (periodo == null) {
            return consultarAptasPorMunicipioESafra(validacaoIbge);
        }

        Municipio municipio = municipioPort.buscarPorCodigoIbge(validacaoIbge.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(validacaoIbge.codigoIbge()));

        Long municipioId = municipioPort.buscarIdPorCodigoIbge(validacaoIbge.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(validacaoIbge.codigoIbge()));

        Optional<SafraVigencia> safraRecente = zarcZoneamentoQueryPort.buscarSafraMaisRecentePorMunicipio(municipioId);
        if (safraRecente.isEmpty()) {
            return new ZoneamentoMunicipioResultado(municipio, null, null, List.of());
        }

        Integer safraIni = safraRecente.get().safraIni();
        Integer safraFim = safraRecente.get().safraFim();

        List<ZarcZoneamentoItem> itensAptos = zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(
                municipioId,
                safraIni,
                safraFim
        );

        List<ZarcZoneamentoItem> itensNoPeriodo = itensAptos.stream()
                .filter(item -> periodo.possuiDecendioFavoravelNoPeriodo(item.decendios(), RISCOS_FAVORAVEIS))
                .toList();

        if (itensNoPeriodo.isEmpty()) {
            return new ZoneamentoMunicipioResultado(municipio, safraIni, safraFim, List.of());
        }

        List<CulturaZoneamentoConsolidado> culturasConsolidadas = consolidarPorCultura(itensNoPeriodo, null, periodo, null);
        return new ZoneamentoMunicipioResultado(municipio, safraIni, safraFim, culturasConsolidadas);
    }

    @Override
    public ZoneamentoMunicipioResultado consultarAptasPorMunicipioPeriodoESolo(FiltroPeriodoSoloZoneamento filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");

        Municipio municipio = municipioPort.buscarPorCodigoIbge(filtro.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(filtro.codigoIbge()));

        Long municipioId = municipioPort.buscarIdPorCodigoIbge(filtro.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(filtro.codigoIbge()));

        Optional<SafraVigencia> safraRecente = zarcZoneamentoQueryPort.buscarSafraMaisRecentePorMunicipio(municipioId);
        if (safraRecente.isEmpty()) {
            return new ZoneamentoMunicipioResultado(municipio, null, null, List.of());
        }

        Integer safraIni = safraRecente.get().safraIni();
        Integer safraFim = safraRecente.get().safraFim();

        List<ZarcZoneamentoItem> itensAptos = zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(
                municipioId,
                safraIni,
                safraFim
        );

        List<ZarcZoneamentoItem> itensNoPeriodoESolo = itensAptos.stream()
                .filter(item -> correspondeAoSolo(item.soloCodigo(), filtro.tipoSolo()))
                .filter(item -> filtro.periodo().possuiDecendioFavoravelNoPeriodo(item.decendios(), RISCOS_FAVORAVEIS))
                .toList();

        if (itensNoPeriodoESolo.isEmpty()) {
            return new ZoneamentoMunicipioResultado(municipio, safraIni, safraFim, List.of());
        }

        List<CulturaZoneamentoConsolidado> culturasConsolidadas = consolidarPorCultura(
                itensNoPeriodoESolo,
                null,
                filtro.periodo(),
                filtro.tipoSolo()
        );
        return new ZoneamentoMunicipioResultado(municipio, safraIni, safraFim, culturasConsolidadas);
    }

    @Override
    public ZoneamentoMunicipioResultado consultarAptasPorMunicipioComFiltros(FiltroDetalhadoZoneamentoMunicipio filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");

        Municipio municipio = municipioPort.buscarPorCodigoIbge(filtro.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(filtro.codigoIbge()));

        Long municipioId = municipioPort.buscarIdPorCodigoIbge(filtro.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(filtro.codigoIbge()));

        Integer safraIni = filtro.safraIni();
        Integer safraFim = filtro.safraFim();

        if (safraIni == null || safraFim == null) {
            Optional<SafraVigencia> safraRecente = zarcZoneamentoQueryPort.buscarSafraMaisRecentePorMunicipio(municipioId);
            if (safraRecente.isEmpty()) {
                return new ZoneamentoMunicipioResultado(municipio, null, null, List.of());
            }
            safraIni = safraRecente.get().safraIni();
            safraFim = safraRecente.get().safraFim();
        }

        List<ZarcZoneamentoItem> itensAptos = zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(
                municipioId,
                safraIni,
                safraFim
        );

        List<ZarcZoneamentoItem> itensFiltrados = filtrarItens(itensAptos, filtro);
        if (itensFiltrados.isEmpty()) {
            return new ZoneamentoMunicipioResultado(municipio, safraIni, safraFim, List.of());
        }

        List<CulturaZoneamentoConsolidado> culturasConsolidadas = consolidarPorCultura(
                itensFiltrados,
                filtro.riscoMaximo(),
                null,
                null
        );
        return new ZoneamentoMunicipioResultado(municipio, safraIni, safraFim, culturasConsolidadas);
    }

    @Override
    public List<ZarcZoneamentoItem> consultarItensAptosPorMunicipioSafraECultura(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim,
            Long culturaId
    ) {
        Objects.requireNonNull(culturaId, "culturaId é obrigatório");
        FiltroZoneamentoMunicipio filtro = new FiltroZoneamentoMunicipio(codigoIbge, safraIni, safraFim);

        municipioPort.buscarPorCodigoIbge(filtro.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(filtro.codigoIbge()));

        Long municipioId = municipioPort.buscarIdPorCodigoIbge(filtro.codigoIbge())
                .orElseThrow(() -> new MunicipioNaoEncontradoException(filtro.codigoIbge()));

        Integer iniResolvido = filtro.safraIni();
        Integer fimResolvido = filtro.safraFim();

        if (iniResolvido == null || fimResolvido == null) {
            Optional<SafraVigencia> safraRecente = zarcZoneamentoQueryPort.buscarSafraMaisRecentePorMunicipio(municipioId);
            if (safraRecente.isEmpty()) {
                return List.of();
            }
            iniResolvido = safraRecente.get().safraIni();
            fimResolvido = safraRecente.get().safraFim();
        }

        return zarcZoneamentoQueryPort.buscarAptosPorMunicipioESafra(municipioId, iniResolvido, fimResolvido)
                .stream()
                .filter(item -> Objects.equals(item.culturaId(), culturaId))
                .toList();
    }

    private boolean correspondeAoSolo(String soloCodigoItem, TipoSoloZarc tipoSolo) {
        if (tipoSolo == null) {
            return true;
        }
        if (soloCodigoItem == null || soloCodigoItem.isBlank()) {
            return false;
        }
        return TipoSoloZarc.buscarPorCodigoOuNome(soloCodigoItem)
                .map(encontrado -> encontrado == tipoSolo)
                .orElseGet(() -> soloCodigoItem.trim().equalsIgnoreCase(tipoSolo.getCodigo()));
    }

    private List<ZarcZoneamentoItem> filtrarItens(
            List<ZarcZoneamentoItem> itens,
            FiltroDetalhadoZoneamentoMunicipio filtro
    ) {
        if (itens.isEmpty()) {
            return List.of();
        }
        return itens.stream()
                .filter(item -> filtro.culturaId() == null || Objects.equals(item.culturaId(), filtro.culturaId()))
                .filter(item -> filtro.soloCodigo() == null
                        || (item.soloCodigo() != null && item.soloCodigo().trim().equalsIgnoreCase(filtro.soloCodigo())))
                .filter(item -> possuiDecendioDentroDoRisco(item, filtro.riscoMaximo()))
                .toList();
    }

    private boolean possuiDecendioDentroDoRisco(ZarcZoneamentoItem item, Integer riscoMaximo) {
        for (Integer risco : item.decendios()) {
            if (riscoFavoravelAceito(risco, riscoMaximo)) {
                return true;
            }
        }
        return false;
    }

    private boolean riscoFavoravelAceito(Integer risco, Integer riscoMaximo) {
        if (risco == null || risco == 0 || !RISCOS_FAVORAVEIS.contains(risco)) {
            return false;
        }
        return riscoMaximo == null || risco <= riscoMaximo;
    }

    private List<CulturaZoneamentoConsolidado> consolidarPorCultura(
            List<ZarcZoneamentoItem> itens,
            Integer riscoMaximo,
            PeriodoPlantioMeses periodoFiltro,
            TipoSoloZarc tipoSoloFiltro
    ) {
        Map<Long, List<ZarcZoneamentoItem>> agrupadosPorCultura = new LinkedHashMap<>();
        for (ZarcZoneamentoItem item : itens) {
            agrupadosPorCultura.computeIfAbsent(item.culturaId(), k -> new ArrayList<>()).add(item);
        }

        List<CulturaZoneamentoConsolidado> resultado = new ArrayList<>();
        for (List<ZarcZoneamentoItem> grupo : agrupadosPorCultura.values()) {
            consolidarGrupoCultura(grupo, riscoMaximo, periodoFiltro, tipoSoloFiltro).ifPresent(resultado::add);
        }

        resultado.sort(Comparator.comparing(CulturaZoneamentoConsolidado::nomeCultura, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    private Optional<CulturaZoneamentoConsolidado> consolidarGrupoCultura(
            List<ZarcZoneamentoItem> grupo,
            Integer riscoMaximo,
            PeriodoPlantioMeses periodoFiltro,
            TipoSoloZarc tipoSoloFiltro
    ) {
        if (grupo.isEmpty()) {
            return Optional.empty();
        }

        ZarcZoneamentoItem primeiro = grupo.get(0);
        Set<String> nomesOrigem = new LinkedHashSet<>();
        TreeSet<Integer> niveisRiscoCultura = new TreeSet<>();
        Map<Integer, TreeSet<Integer>> riscosPorDecendio = new TreeMap<>();
        Map<Integer, Map<Integer, SoloZoneamento>> solosPorDecendioERisco = new LinkedHashMap<>();
        TreeSet<SoloZoneamento> solos = new TreeSet<>();
        TreeSet<CicloZoneamento> ciclos = new TreeSet<>();
        Set<String> portarias = new LinkedHashSet<>();

        for (ZarcZoneamentoItem item : grupo) {
            if (item.nomeOrigemZarc() != null && !item.nomeOrigemZarc().isBlank()) {
                nomesOrigem.add(item.nomeOrigemZarc().trim());
            }
            SoloZoneamento soloDoItem = null;
            if (item.soloCodigo() != null && !item.soloCodigo().isBlank()) {
                soloDoItem = new SoloZoneamento(item.soloCodigo(), item.soloDescricao());
                solos.add(soloDoItem);
            }
            if (item.cicloCodigo() != null && !item.cicloCodigo().isBlank()) {
                ciclos.add(new CicloZoneamento(item.cicloCodigo(), item.cicloDescricao()));
            }
            if (item.portaria() != null && !item.portaria().isBlank()) {
                portarias.add(item.portaria().trim());
            }

            int numeroDecendio = 1;
            for (Integer riscoDecendio : item.decendios()) {
                boolean dentroDoPeriodo = periodoFiltro == null || periodoFiltro.contemDecendio(numeroDecendio);
                if (dentroDoPeriodo && riscoFavoravelAceito(riscoDecendio, riscoMaximo)) {
                    niveisRiscoCultura.add(riscoDecendio);
                    riscosPorDecendio
                            .computeIfAbsent(numeroDecendio, d -> new TreeSet<>())
                            .add(riscoDecendio);
                    if (soloDoItem != null) {
                        solosPorDecendioERisco
                                .computeIfAbsent(numeroDecendio, d -> new LinkedHashMap<>())
                                .putIfAbsent(riscoDecendio, soloDoItem);
                    }
                }
                numeroDecendio++;
            }
        }

        if (niveisRiscoCultura.isEmpty()) {
            return Optional.empty();
        }

        List<PeriodoDecendio> janelasPlantio = riscosPorDecendio.entrySet().stream()
                .map(entry -> PeriodoDecendio.deDecendio(entry.getKey(), entry.getValue()))
                .toList();

        Integer riscoMinimo = niveisRiscoCultura.first();

        PeriodoDecendio melhorPeriodo = periodoFiltro != null
                ? periodoFiltro.selecionarMelhorDecendio(janelasPlantio).orElseGet(() -> selecionarMenorRiscoPadrao(janelasPlantio))
                : selecionarMenorRiscoPadrao(janelasPlantio);

        SoloZoneamento soloProjecao = null;
        if (melhorPeriodo != null) {
            Map<Integer, SoloZoneamento> porRisco = solosPorDecendioERisco.get(melhorPeriodo.decendio());
            if (porRisco != null) {
                soloProjecao = porRisco.get(melhorPeriodo.menorRisco());
            }
        }
        if (soloProjecao == null && tipoSoloFiltro != null) {
            soloProjecao = tipoSoloFiltro.toSoloZoneamento();
        }
        if (soloProjecao == null && !solos.isEmpty()) {
            soloProjecao = solos.first();
        }

        String descricaoSoloRecomendacao = soloProjecao != null
                ? soloProjecao.tipoSolo()
                : (!solos.isEmpty() ? solos.first().tipoSolo() : "o solo selecionado");

        String recomendacao = melhorPeriodo != null
                ? melhorPeriodo.formatarRecomendacaoSolo(descricaoSoloRecomendacao)
                : null;

        return Optional.of(new CulturaZoneamentoConsolidado(
                primeiro.culturaId(),
                primeiro.nomeCultura(),
                primeiro.nomeCientifico(),
                primeiro.grupoAgricola(),
                new ArrayList<>(nomesOrigem),
                riscoMinimo,
                niveisRiscoCultura,
                janelasPlantio,
                new ArrayList<>(solos),
                new ArrayList<>(ciclos),
                new ArrayList<>(portarias),
                melhorPeriodo,
                recomendacao,
                soloProjecao
        ));
    }

    private PeriodoDecendio selecionarMenorRiscoPadrao(List<PeriodoDecendio> janelas) {
        if (janelas == null || janelas.isEmpty()) {
            return null;
        }
        return janelas.stream()
                .min(Comparator.comparingInt(PeriodoDecendio::menorRisco)
                        .thenComparingInt(PeriodoDecendio::decendio))
                .orElse(null);
    }
}
