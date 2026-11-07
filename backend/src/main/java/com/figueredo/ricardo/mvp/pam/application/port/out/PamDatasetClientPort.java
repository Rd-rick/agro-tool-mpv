package com.figueredo.ricardo.mvp.pam.application.port.out;

import com.figueredo.ricardo.mvp.pam.domain.model.RecursoPam;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface PamDatasetClientPort {

    List<RecursoPam> buscarRecursosDisponiveis();

    InputStream abrirStreamValoresAno(int ano, Collection<String> codigosCulturaOrigem);
}
