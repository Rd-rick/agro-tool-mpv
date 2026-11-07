package com.figueredo.ricardo.mvp.epagri.application.port.out;

import java.util.Map;

public interface RegiaoPracaPort {

    Map<String, Long> mapearNomeParaIdPorUf(String uf);
}
