package com.figueredo.ricardo.mvp.zarc.application.port.out;

import com.figueredo.ricardo.mvp.zarc.domain.model.ControleCarga;

import java.util.Optional;

public interface ControleCargaPort {

    Optional<ControleCarga> buscarUltimaCargaComSucesso(String fonte, String identificadorRecurso);

    ControleCarga salvar(ControleCarga controleCarga);
}
