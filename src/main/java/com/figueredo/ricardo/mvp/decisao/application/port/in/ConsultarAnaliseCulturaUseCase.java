package com.figueredo.ricardo.mvp.decisao.application.port.in;

import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.util.Objects;

public interface ConsultarAnaliseCulturaUseCase {

    AnaliseCulturaResultado consultarAnaliseCultura(FiltroAnaliseCultura filtro);

    default AnaliseCulturaResultado consultarAnaliseCultura(
            String codigoIbge,
            Long culturaId,
            PeriodoPlantioMeses periodoPlantio
    ) {
        return consultarAnaliseCultura(new FiltroAnaliseCultura(codigoIbge, culturaId, periodoPlantio, null));
    }

    default AnaliseCulturaResultado consultarAnaliseCultura(
            String codigoIbge,
            Long culturaId,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo
    ) {
        return consultarAnaliseCultura(new FiltroAnaliseCultura(codigoIbge, culturaId, periodoPlantio, tipoSolo));
    }

    record FiltroAnaliseCultura(
            String codigoIbge,
            Long culturaId,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo
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
        }

        public FiltroAnaliseCultura(
                String codigoIbge,
                Long culturaId,
                PeriodoPlantioMeses periodoPlantio
        ) {
            this(codigoIbge, culturaId, periodoPlantio, null);
        }
    }
}
