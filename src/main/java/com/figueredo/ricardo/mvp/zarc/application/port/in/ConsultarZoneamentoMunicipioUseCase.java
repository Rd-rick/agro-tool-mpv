package com.figueredo.ricardo.mvp.zarc.application.port.in;

import com.figueredo.ricardo.mvp.zarc.domain.model.ZoneamentoMunicipioResultado;

public interface ConsultarZoneamentoMunicipioUseCase {

    ZoneamentoMunicipioResultado consultarAptasPorMunicipioESafra(FiltroZoneamentoMunicipio filtro);

    default ZoneamentoMunicipioResultado consultarAptasPorMunicipioESafra(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim
    ) {
        return consultarAptasPorMunicipioESafra(new FiltroZoneamentoMunicipio(codigoIbge, safraIni, safraFim));
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
}
