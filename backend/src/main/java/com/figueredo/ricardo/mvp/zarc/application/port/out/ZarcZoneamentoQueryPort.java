package com.figueredo.ricardo.mvp.zarc.application.port.out;

import com.figueredo.ricardo.mvp.zarc.domain.model.SafraVigencia;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;

import java.util.List;
import java.util.Optional;

public interface ZarcZoneamentoQueryPort {

    List<ZarcZoneamentoItem> buscarAptosPorMunicipioESafra(Long municipioId, Integer safraIni, Integer safraFim);

    Optional<SafraVigencia> buscarSafraMaisRecentePorMunicipio(Long municipioId);
}
