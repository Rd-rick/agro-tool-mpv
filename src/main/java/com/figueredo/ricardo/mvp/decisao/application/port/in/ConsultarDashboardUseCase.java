package com.figueredo.ricardo.mvp.decisao.application.port.in;

import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.math.BigDecimal;
import java.util.Objects;

public interface ConsultarDashboardUseCase {

    DashboardResultado consultarDashboard(FiltroDashboard filtro);

    record FiltroDashboard(
            String codigoIbge,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo,
            BigDecimal areaHa
    ) {
        public FiltroDashboard {
            if (codigoIbge == null || codigoIbge.isBlank()) {
                throw new IllegalArgumentException("codigoIbge é obrigatório");
            }
            codigoIbge = codigoIbge.trim();
            if (!codigoIbge.matches("^42\\d{5}$")) {
                throw new IllegalArgumentException("codigoIbge deve ser um código IBGE de SC com 7 dígitos");
            }
            Objects.requireNonNull(periodoPlantio, "periodoPlantio é obrigatório");
            if (areaHa != null && (areaHa.compareTo(BigDecimal.ZERO) <= 0 || areaHa.compareTo(new BigDecimal("1000000")) > 0)) {
                throw new IllegalArgumentException("areaHa deve ser positiva e no máximo 1000000");
            }
        }

        public FiltroDashboard(String codigoIbge, PeriodoPlantioMeses periodoPlantio, BigDecimal areaHa) {
            this(codigoIbge, periodoPlantio, null, areaHa);
        }
    }
}
