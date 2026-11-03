package com.figueredo.ricardo.mvp.epagri.application.port.out;

import com.figueredo.ricardo.mvp.epagri.domain.model.CotacaoPrecoRegistro;

import java.util.List;

public interface CotacaoPersistencePort {

    void salvarLote(List<CotacaoPrecoRegistro> lote);
}
