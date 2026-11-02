package com.figueredo.ricardo.mvp.zarc.application.port.out;

import com.figueredo.ricardo.mvp.zarc.domain.model.RecursoZarc;

import java.io.InputStream;
import java.util.List;

public interface ZarcDatasetClientPort {

    List<RecursoZarc> buscarRecursosDisponiveis();

    InputStream abrirStreamCsv(String urlRecurso);
}
