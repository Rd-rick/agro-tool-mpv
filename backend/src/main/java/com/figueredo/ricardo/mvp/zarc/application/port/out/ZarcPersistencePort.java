package com.figueredo.ricardo.mvp.zarc.application.port.out;

import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcRegistro;

import java.util.List;

public interface ZarcPersistencePort {

    int salvarLote(List<ZarcRegistro> registros);
}
