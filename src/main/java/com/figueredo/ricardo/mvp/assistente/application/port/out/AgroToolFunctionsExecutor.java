package com.figueredo.ricardo.mvp.assistente.application.port.out;

import com.figueredo.ricardo.mvp.assistente.domain.model.ChamadaFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.ResultadoFerramentaIa;

import java.util.List;

public interface AgroToolFunctionsExecutor {

    List<DefinicaoFerramentaIa> obterDefinicoes();

    ResultadoFerramentaIa executar(ChamadaFerramentaIa chamada);
}
