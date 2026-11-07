package com.figueredo.ricardo.mvp.zarc.application.port.in;

import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;

import java.util.List;

public interface ListarMunicipiosUseCase {

    List<Municipio> listarTodos();
}
