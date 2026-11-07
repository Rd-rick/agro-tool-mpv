package com.figueredo.ricardo.mvp.zarc.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.MunicipioNaoEncontradoException;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcZoneamentoQueryPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.CicloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoDecendio;
import com.figueredo.ricardo.mvp.zarc.domain.model.SafraVigencia;
import com.figueredo.ricardo.mvp.zarc.domain.model.SoloZoneamento;
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

        if (itensAptos.isEmpty()) {
            return new ZoneamentoMunicipioResultado(municipio, safraIni, safraFim, List.of());
        }

        List<CulturaZoneamentoConsolidado> culturasConsolidadas = consolidarPorCultura(itensAptos);
        return new ZoneamentoMunicipioResultado(municipio, safraIni, safraFim, culturasConsolidadas);
    }

    private List<CulturaZoneamentoConsolidado> consolidarPorCultura(List<ZarcZoneamentoItem> itens) {
        Map<Long, List<ZarcZoneamentoItem>> agrupadosPorCultura = new LinkedHashMap<>();
        for (ZarcZoneamentoItem item : itens) {
            agrupadosPorCultura.computeIfAbsent(item.culturaId(), k -> new ArrayList<>()).add(item);
        }

        List<CulturaZoneamentoConsolidado> resultado = new ArrayList<>();
        for (List<ZarcZoneamentoItem> grupo : agrupadosPorCultura.values()) {
            consolidarGrupoCultura(grupo).ifPresent(resultado::add);
        }

        resultado.sort(Comparator.comparing(CulturaZoneamentoConsolidado::nomeCultura, String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    private Optional<CulturaZoneamentoConsolidado> consolidarGrupoCultura(List<ZarcZoneamentoItem> grupo) {
        if (grupo.isEmpty()) {
            return Optional.empty();
        }

        ZarcZoneamentoItem primeiro = grupo.get(0);
        Set<String> nomesOrigem = new LinkedHashSet<>();
        TreeSet<Integer> niveisRiscoCultura = new TreeSet<>();
        Map<Integer, TreeSet<Integer>> riscosPorDecendio = new TreeMap<>();
        TreeSet<SoloZoneamento> solos = new TreeSet<>();
        TreeSet<CicloZoneamento> ciclos = new TreeSet<>();
        Set<String> portarias = new LinkedHashSet<>();

        for (ZarcZoneamentoItem item : grupo) {
            if (item.nomeOrigemZarc() != null && !item.nomeOrigemZarc().isBlank()) {
                nomesOrigem.add(item.nomeOrigemZarc().trim());
            }
            if (item.soloCodigo() != null && !item.soloCodigo().isBlank()) {
                solos.add(new SoloZoneamento(item.soloCodigo(), item.soloDescricao()));
            }
            if (item.cicloCodigo() != null && !item.cicloCodigo().isBlank()) {
                ciclos.add(new CicloZoneamento(item.cicloCodigo(), item.cicloDescricao()));
            }
            if (item.portaria() != null && !item.portaria().isBlank()) {
                portarias.add(item.portaria().trim());
            }

            int numeroDecendio = 1;
            for (Integer riscoDecendio : item.decendios()) {
                if (riscoDecendio != null && RISCOS_FAVORAVEIS.contains(riscoDecendio)) {
                    niveisRiscoCultura.add(riscoDecendio);
                    riscosPorDecendio
                            .computeIfAbsent(numeroDecendio, d -> new TreeSet<>())
                            .add(riscoDecendio);
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
                new ArrayList<>(portarias)
        ));
    }
}
