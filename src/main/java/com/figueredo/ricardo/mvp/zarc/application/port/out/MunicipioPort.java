package com.figueredo.ricardo.mvp.zarc.application.port.out;

import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;

import java.util.List;
import java.util.Map;

public interface MunicipioPort {

    Map<String, Long> mapearIbgeParaIdPorUf(String uf);

    List<Municipio> listarTodosOrdenadosPorNome();
}
