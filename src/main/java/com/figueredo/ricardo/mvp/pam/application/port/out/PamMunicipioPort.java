package com.figueredo.ricardo.mvp.pam.application.port.out;

import java.util.Map;

public interface PamMunicipioPort {

    Map<String, Long> mapearIbgeParaIdPorUf(String uf);
}
