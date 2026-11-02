package com.figueredo.ricardo.mvp.zarc.application.port.out;

import java.util.Map;

public interface CulturaFontePort {

    Map<String, Long> mapearCodigoOrigemParaIdPorFonte(String fonte);
}
