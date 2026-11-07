package com.figueredo.ricardo.mvp.epagri.application.port.out;

import java.util.Map;

public interface ProdutoCotacaoPort {

    Map<String, Long> mapearNomeBrutoParaIdPorFonte(String fonte);
}
