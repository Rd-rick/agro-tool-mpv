package com.figueredo.ricardo.mvp.zarc.application.port.in;

import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface ConsultarZoneamentoMunicipioUseCase {

    Set<Integer> RISCOS_PERMITIDOS = Set.of(20, 30, 40);

    ZoneamentoMunicipioResultado consultarAptasPorMunicipioESafra(FiltroZoneamentoMunicipio filtro);

    default ZoneamentoMunicipioResultado consultarAptasPorMunicipioESafra(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim
    ) {
        return consultarAptasPorMunicipioESafra(new FiltroZoneamentoMunicipio(codigoIbge, safraIni, safraFim));
    }

    default ZoneamentoMunicipioResultado consultarAptasPorMunicipioEPeriodo(
            String codigoIbge,
            PeriodoPlantioMeses periodo
    ) {
        return consultarAptasPorMunicipioESafra(new FiltroZoneamentoMunicipio(codigoIbge, null, null));
    }

    default ZoneamentoMunicipioResultado consultarAptasPorMunicipioPeriodoESolo(FiltroPeriodoSoloZoneamento filtro) {
        Objects.requireNonNull(filtro, "filtro é obrigatório");
        return consultarAptasPorMunicipioEPeriodo(filtro.codigoIbge(), filtro.periodo());
    }

    default ZoneamentoMunicipioResultado consultarAptasPorMunicipioPeriodoESolo(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            TipoSoloZarc tipoSolo
    ) {
        return consultarAptasPorMunicipioPeriodoESolo(new FiltroPeriodoSoloZoneamento(codigoIbge, periodo, tipoSolo));
    }

    default ZoneamentoMunicipioResultado consultarAptasPorMunicipioPeriodoESolo(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            String soloCodigo
    ) {
        return consultarAptasPorMunicipioPeriodoESolo(
                new FiltroPeriodoSoloZoneamento(codigoIbge, periodo, TipoSoloZarc.deCodigoOuNome(soloCodigo))
        );
    }

    default ZoneamentoMunicipioResultado consultarAptasPorMunicipioComFiltros(FiltroDetalhadoZoneamentoMunicipio filtro) {
        return consultarAptasPorMunicipioESafra(
                new FiltroZoneamentoMunicipio(filtro.codigoIbge(), filtro.safraIni(), filtro.safraFim())
        );
    }

    default List<ZarcZoneamentoItem> consultarItensAptosPorMunicipioSafraECultura(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim,
            Long culturaId
    ) {
        return List.of();
    }

    default List<ZarcZoneamentoItem> consultarItensAptosPorMunicipioSafraCulturaESolo(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim,
            Long culturaId,
            TipoSoloZarc tipoSolo
    ) {
        return consultarItensAptosPorMunicipioSafraECultura(codigoIbge, safraIni, safraFim, culturaId)
                .stream()
                .filter(item -> tipoSolo == null
                        || (item.soloCodigo() != null && item.soloCodigo().trim().equalsIgnoreCase(tipoSolo.getCodigo())))
                .toList();
    }

    record FiltroPeriodoSoloZoneamento(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            TipoSoloZarc tipoSolo
    ) {
        public FiltroPeriodoSoloZoneamento {
            if (codigoIbge == null || codigoIbge.isBlank()) {
                throw new IllegalArgumentException("codigoIbge é obrigatório");
            }
            codigoIbge = codigoIbge.trim();
            if (!codigoIbge.matches("\\d{7}")) {
                throw new IllegalArgumentException("codigoIbge deve conter 7 dígitos numéricos");
            }
            Objects.requireNonNull(periodo, "periodo de plantio é obrigatório");
            Objects.requireNonNull(tipoSolo, "tipoSolo é obrigatório");
        }
    }

    record FiltroZoneamentoMunicipio(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim
    ) {
        public FiltroZoneamentoMunicipio {
            if (codigoIbge == null || codigoIbge.isBlank()) {
                throw new IllegalArgumentException("codigoIbge é obrigatório");
            }
            codigoIbge = codigoIbge.trim();
            if (!codigoIbge.matches("\\d{7}")) {
                throw new IllegalArgumentException("codigoIbge deve conter 7 dígitos numéricos");
            }
            if ((safraIni == null) != (safraFim == null)) {
                throw new IllegalArgumentException("safraIni e safraFim devem ser informados juntos");
            }
            if (safraIni != null && safraFim < safraIni) {
                throw new IllegalArgumentException("safraFim não pode ser menor que safraIni");
            }
        }
    }

    record FiltroDetalhadoZoneamentoMunicipio(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim,
            Long culturaId,
            Integer riscoMaximo,
            String soloCodigo
    ) {
        public FiltroDetalhadoZoneamentoMunicipio {
            if (codigoIbge == null || codigoIbge.isBlank()) {
                throw new IllegalArgumentException("codigoIbge é obrigatório");
            }
            codigoIbge = codigoIbge.trim();
            if (!codigoIbge.matches("\\d{7}")) {
                throw new IllegalArgumentException("codigoIbge deve conter 7 dígitos numéricos");
            }
            if ((safraIni == null) != (safraFim == null)) {
                throw new IllegalArgumentException("safraIni e safraFim devem ser informados juntos");
            }
            if (safraIni != null && safraFim < safraIni) {
                throw new IllegalArgumentException("safraFim não pode ser menor que safraIni");
            }
            if (culturaId != null && culturaId <= 0) {
                throw new IllegalArgumentException("culturaId deve ser maior que zero");
            }
            if (riscoMaximo != null && !RISCOS_PERMITIDOS.contains(riscoMaximo)) {
                throw new IllegalArgumentException("riscoMaximo deve ser 20, 30 ou 40");
            }
            if (soloCodigo != null) {
                soloCodigo = soloCodigo.trim();
                if (soloCodigo.isEmpty()) {
                    soloCodigo = null;
                }
            }
        }
    }
}
