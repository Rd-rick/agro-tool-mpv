package com.figueredo.ricardo.mvp.zarc.application.service;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ListarMunicipiosUseCase;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListarMunicipiosService implements ListarMunicipiosUseCase {

    private final MunicipioPort municipioPort;

    public ListarMunicipiosService(MunicipioPort municipioPort) {
        this.municipioPort = municipioPort;
    }

    @Override
    public List<Municipio> listarTodos() {
        return municipioPort.listarTodosOrdenadosPorNome();
    }
}
