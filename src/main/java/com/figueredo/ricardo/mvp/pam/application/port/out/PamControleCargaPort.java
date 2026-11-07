package com.figueredo.ricardo.mvp.pam.application.port.out;

import com.figueredo.ricardo.mvp.pam.domain.model.ControleCargaPam;

import java.util.Optional;

public interface PamControleCargaPort {

    Optional<ControleCargaPam> buscarUltimaCargaComSucesso(String fonte, String identificadorRecurso);

    ControleCargaPam salvar(ControleCargaPam controleCarga);
}
