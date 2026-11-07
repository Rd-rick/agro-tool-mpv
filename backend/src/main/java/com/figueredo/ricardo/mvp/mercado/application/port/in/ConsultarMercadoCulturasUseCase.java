package com.figueredo.ricardo.mvp.mercado.application.port.in;

import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;

import java.util.List;

public interface ConsultarMercadoCulturasUseCase {

    List<CulturaMercadoResultado> consultarMercadoPorMunicipioECulturas(FiltroMercadoCulturas filtro);

    default List<CulturaMercadoResultado> consultarMercadoPorMunicipioECulturas(
            String codigoIbge,
            List<Long> culturaIds
    ) {
        return consultarMercadoPorMunicipioECulturas(new FiltroMercadoCulturas(codigoIbge, culturaIds));
    }

    record FiltroMercadoCulturas(
            String codigoIbge,
            List<Long> culturaIds
    ) {
        public FiltroMercadoCulturas {
            if (codigoIbge == null || codigoIbge.isBlank()) {
                throw new IllegalArgumentException("codigoIbge é obrigatório");
            }
            codigoIbge = codigoIbge.trim();
            if (!codigoIbge.matches("\\d{7}")) {
                throw new IllegalArgumentException("codigoIbge deve conter 7 dígitos numéricos");
            }
            culturaIds = culturaIds != null ? List.copyOf(culturaIds) : List.of();
        }
    }
}
