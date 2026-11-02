package com.figueredo.ricardo.mvp.zarc.application.port.out;

import java.util.Map;

public interface MunicipioPort {

    Map<String, Long> mapearIbgeParaIdPorUf(String uf);
}
