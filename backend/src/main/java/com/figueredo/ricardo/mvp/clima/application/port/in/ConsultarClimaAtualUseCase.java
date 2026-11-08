package com.figueredo.ricardo.mvp.clima.application.port.in;

import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;

public interface ConsultarClimaAtualUseCase {
    ClimaAtual consultarClimaAtual(String codigoIbge);
}
