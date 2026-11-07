package com.figueredo.ricardo.mvp.pam.application.port.out;

import java.util.Map;

public interface PamCulturaFontePort {

    Map<String, Long> mapearCodigoOrigemParaIdPorFonte(String fonte);
}
