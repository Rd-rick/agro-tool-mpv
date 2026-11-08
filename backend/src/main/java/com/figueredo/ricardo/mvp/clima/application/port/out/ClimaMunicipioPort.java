package com.figueredo.ricardo.mvp.clima.application.port.out;

import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;

public interface ClimaMunicipioPort {
    ClimaAtual obterClimaDiaAtual(String codigoIbge);
}
