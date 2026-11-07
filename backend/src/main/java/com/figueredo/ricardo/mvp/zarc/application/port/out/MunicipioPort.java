package com.figueredo.ricardo.mvp.zarc.application.port.out;

import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MunicipioPort {

    Map<String, Long> mapearIbgeParaIdPorUf(String uf);

    List<Municipio> listarTodosOrdenadosPorNome();

    Optional<Municipio> buscarPorCodigoIbge(String codigoIbge);

    Optional<Long> buscarIdPorCodigoIbge(String codigoIbge);
}
