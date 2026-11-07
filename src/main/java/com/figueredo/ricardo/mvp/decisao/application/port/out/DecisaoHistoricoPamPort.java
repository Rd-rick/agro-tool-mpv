package com.figueredo.ricardo.mvp.decisao.application.port.out;

import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface DecisaoHistoricoPamPort {

    Map<Long, HistoricoPamCultura> buscarUltimoHistoricoPamPorMunicipioECulturas(
            String codigoIbge,
            Collection<Long> culturaIds
    );

    default Map<Long, List<HistoricoPamCultura>> buscarSerieHistoricaPamPorMunicipioECulturas(
            String codigoIbge,
            Collection<Long> culturaIds
    ) {
        Map<Long, HistoricoPamCultura> ultimos = buscarUltimoHistoricoPamPorMunicipioECulturas(codigoIbge, culturaIds);
        Map<Long, List<HistoricoPamCultura>> resultado = new LinkedHashMap<>();
        for (Map.Entry<Long, HistoricoPamCultura> entry : ultimos.entrySet()) {
            HistoricoPamCultura item = entry.getValue();
            resultado.put(entry.getKey(), item != null && item.possuiHistorico() ? List.of(item) : List.of());
        }
        return resultado;
    }
}
