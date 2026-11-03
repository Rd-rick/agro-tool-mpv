package com.figueredo.ricardo.mvp.epagri.application.port.out;

import com.figueredo.ricardo.mvp.epagri.domain.model.ControleCargaEpagri;

import java.util.Optional;

public interface EpagriControleCargaPort {

    Optional<ControleCargaEpagri> buscarUltimaCargaComSucesso(String fonte, String identificadorRecurso);

    ControleCargaEpagri salvar(ControleCargaEpagri controleCarga);
}
