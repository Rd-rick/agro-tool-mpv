package com.figueredo.ricardo.mvp.decisao.application.port.in;

import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface CompararCulturasUseCase {

    ComparacaoCulturasResultado compararCulturas(FiltroCompararCulturas filtro);

    default ComparacaoCulturasResultado compararCulturas(
            String codigoIbge,
            PeriodoPlantioMeses periodoPlantio,
            List<Long> culturaIds
    ) {
        return compararCulturas(new FiltroCompararCulturas(codigoIbge, periodoPlantio, null, culturaIds));
    }

    default ComparacaoCulturasResultado compararCulturas(
            String codigoIbge,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo,
            List<Long> culturaIds
    ) {
        return compararCulturas(new FiltroCompararCulturas(codigoIbge, periodoPlantio, tipoSolo, culturaIds));
    }

    record FiltroCompararCulturas(
            String codigoIbge,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo,
            List<Long> culturaIds
    ) {
        public FiltroCompararCulturas {
            if (codigoIbge == null || codigoIbge.isBlank()) {
                throw new IllegalArgumentException("codigoIbge é obrigatório");
            }
            codigoIbge = codigoIbge.trim();
            if (!codigoIbge.matches("^42\\d{5}$")) {
                throw new IllegalArgumentException("codigoIbge deve ser um código IBGE de SC com 7 dígitos");
            }
            Objects.requireNonNull(periodoPlantio, "periodoPlantio é obrigatório");
            if (culturaIds == null || culturaIds.size() < 2 || culturaIds.size() > 3) {
                throw new IllegalArgumentException("culturaIds deve conter entre 2 e 3 culturas para comparação");
            }
            Set<Long> unicos = new LinkedHashSet<>();
            for (Long id : culturaIds) {
                if (id == null || id <= 0) {
                    throw new IllegalArgumentException("Todos os culturaIds devem ser positivos");
                }
                if (!unicos.add(id)) {
                    throw new IllegalArgumentException("culturaIds não deve conter identificadores duplicados");
                }
            }
            culturaIds = List.copyOf(unicos);
        }

        public FiltroCompararCulturas(
                String codigoIbge,
                PeriodoPlantioMeses periodoPlantio,
                List<Long> culturaIds
        ) {
            this(codigoIbge, periodoPlantio, null, culturaIds);
        }
    }
}
