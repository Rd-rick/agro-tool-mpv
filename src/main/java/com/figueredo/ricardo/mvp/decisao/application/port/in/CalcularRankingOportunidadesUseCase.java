package com.figueredo.ricardo.mvp.decisao.application.port.in;

import com.figueredo.ricardo.mvp.decisao.domain.model.RankingOportunidadesResultado;

public interface CalcularRankingOportunidadesUseCase {

    RankingOportunidadesResultado calcularRankingPorMunicipioESafra(FiltroRankingOportunidades filtro);

    default RankingOportunidadesResultado calcularRankingPorMunicipioESafra(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim
    ) {
        return calcularRankingPorMunicipioESafra(new FiltroRankingOportunidades(codigoIbge, safraIni, safraFim));
    }

    record FiltroRankingOportunidades(
            String codigoIbge,
            Integer safraIni,
            Integer safraFim
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
        }
    }
}
