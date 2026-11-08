package com.figueredo.ricardo.mvp.decisao.application.port.in;

import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.math.BigDecimal;
import java.util.Objects;

public interface CalcularRankingOportunidadesUseCase {

    RankingOportunidadesResultado calcularRankingPorMunicipioESafra(FiltroRankingOportunidades filtro);

    default RankingOportunidadesResultado calcularRankingPorMunicipioESafra(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim
    ) {
        return calcularRankingPorMunicipioESafra(new FiltroRankingOportunidades(codigoIbge, safraIni, safraFim, BigDecimal.ONE));
    }

    default RankingOportunidadesResultado calcularRankingPorMunicipioESafra(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim,
            BigDecimal areaHa
    ) {
        return calcularRankingPorMunicipioESafra(new FiltroRankingOportunidades(codigoIbge, safraIni, safraFim, areaHa));
    }

    default RankingOportunidadesResultado calcularRankingPorMunicipioEPeriodo(
            String codigoIbge,
            PeriodoPlantioMeses periodo
    ) {
        return calcularRankingPorMunicipioESafra(new FiltroRankingOportunidades(codigoIbge, null, null, BigDecimal.ONE));
    }

    default RankingOportunidadesResultado calcularRankingPorMunicipioEPeriodo(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            BigDecimal areaHa
    ) {
        return calcularRankingPorMunicipioESafra(new FiltroRankingOportunidades(codigoIbge, null, null, areaHa));
    }

    default RankingOportunidadesResultado calcularRankingPorMunicipioPeriodoESolo(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            TipoSoloZarc tipoSolo
    ) {
        return calcularRankingPorMunicipioPeriodoESolo(codigoIbge, periodo, tipoSolo, BigDecimal.ONE);
    }

    default RankingOportunidadesResultado calcularRankingPorMunicipioPeriodoESolo(
            String codigoIbge,
            PeriodoPlantioMeses periodo,
            TipoSoloZarc tipoSolo,
            BigDecimal areaHa
    ) {
        Objects.requireNonNull(periodo, "periodo é obrigatório");
        Objects.requireNonNull(tipoSolo, "tipoSolo é obrigatório");
        return calcularRankingPorMunicipioEPeriodo(codigoIbge, periodo, areaHa);
    }

    record FiltroRankingOportunidades(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim,
            BigDecimal areaHa
    ) {
        public FiltroRankingOportunidades {
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
            if (areaHa == null) {
                areaHa = BigDecimal.ONE;
            } else if (areaHa.compareTo(BigDecimal.ZERO) <= 0 || areaHa.compareTo(new BigDecimal("1000000")) > 0) {
                throw new IllegalArgumentException("areaHa deve ser positiva e no máximo 1000000");
            }
        }

        public FiltroRankingOportunidades(String codigoIbge, Integer safraIni, Integer safraFim) {
            this(codigoIbge, safraIni, safraFim, BigDecimal.ONE);
        }
    }
}
