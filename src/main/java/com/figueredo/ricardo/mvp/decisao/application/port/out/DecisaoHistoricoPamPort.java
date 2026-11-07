package com.figueredo.ricardo.mvp.decisao.application.port.out;

import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;

import java.util.Collection;
import java.util.Map;

public interface DecisaoHistoricoPamPort {

    Map<Long, HistoricoPamCultura> buscarUltimoHistoricoPamPorMunicipioECulturas(
            String codigoIbge,
            Collection<Long> culturaIds
    );
}
