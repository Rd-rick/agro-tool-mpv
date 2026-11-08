package com.figueredo.ricardo.mvp.decisao.application.port.in;

import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.math.BigDecimal;
import java.util.Objects;

public interface ConsultarAnaliseCulturaUseCase {

    AnaliseCulturaResultado consultarAnaliseCultura(FiltroAnaliseCultura filtro);

    default AnaliseCulturaResultado consultarAnaliseCultura(
            String codigoIbge,
            Long culturaId,
            PeriodoPlantioMeses periodoPlantio
    ) {
        return consultarAnaliseCultura(new FiltroAnaliseCultura(codigoIbge, culturaId, periodoPlantio, null, BigDecimal.ONE));
    }

    default AnaliseCulturaResultado consultarAnaliseCultura(
            String codigoIbge,
            Long culturaId,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo
    ) {
        return consultarAnaliseCultura(new FiltroAnaliseCultura(codigoIbge, culturaId, periodoPlantio, tipoSolo, BigDecimal.ONE));
    }

    default AnaliseCulturaResultado consultarAnaliseCultura(
            String codigoIbge,
            Long culturaId,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo,
            BigDecimal areaHa
    ) {
        return consultarAnaliseCultura(new FiltroAnaliseCultura(codigoIbge, culturaId, periodoPlantio, tipoSolo, areaHa));
    }

    record FiltroAnaliseCultura(
            String codigoIbge,
            Long culturaId,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo,
            BigDecimal areaHa
    ) {
        public FiltroAnaliseCultura {
            if (codigoIbge == null || codigoIbge.isBlank()) {
                throw new IllegalArgumentException("codigoIbge é obrigatório");
            }
            codigoIbge = codigoIbge.trim();
            if (!codigoIbge.matches("^42\\d{5}$")) {
                throw new IllegalArgumentException("codigoIbge deve ser um código IBGE de SC com 7 dígitos");
            }
            if (culturaId == null || culturaId <= 0) {
                throw new IllegalArgumentException("culturaId deve ser positivo");
            }
            Objects.requireNonNull(periodoPlantio, "periodoPlantio é obrigatório");
            if (areaHa == null) {
                areaHa = BigDecimal.ONE;
            } else if (areaHa.compareTo(BigDecimal.ZERO) <= 0 || areaHa.compareTo(new BigDecimal("1000000")) > 0) {
                throw new IllegalArgumentException("areaHa deve ser positiva e no máximo 1000000");
            }
        }

        public FiltroAnaliseCultura(
                String codigoIbge,
                Long culturaId,
                PeriodoPlantioMeses periodoPlantio,
                TipoSoloZarc tipoSolo
        ) {
            this(codigoIbge, culturaId, periodoPlantio, tipoSolo, BigDecimal.ONE);
        }

        public FiltroAnaliseCultura(
                String codigoIbge,
                Long culturaId,
                PeriodoPlantioMeses periodoPlantio
        ) {
            this(codigoIbge, culturaId, periodoPlantio, null, BigDecimal.ONE);
        }
    }
}
