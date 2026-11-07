package com.figueredo.ricardo.mvp.pam.application.port.out;

import com.figueredo.ricardo.mvp.pam.domain.model.PamProducaoRegistro;

import java.util.List;

public interface PamPersistencePort {

    int salvarLote(List<PamProducaoRegistro> registros);
}
