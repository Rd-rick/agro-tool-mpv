package com.figueredo.ricardo.mvp.clima.application.service;

import com.figueredo.ricardo.mvp.clima.application.port.out.ClimaMunicipioPort;
import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;
import com.figueredo.ricardo.mvp.clima.domain.model.CondicaoTempo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarClimaAtualServiceTest {

    @Mock
    private ClimaMunicipioPort climaMunicipioPort;

    private ConsultarClimaAtualService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarClimaAtualService(climaMunicipioPort);
    }

    @Test
    void deveRetornarIndisponivelQuandoCodigoIbgeForNuloOuVazio() {
        ClimaAtual resultadoNulo = service.consultarClimaAtual(null);
        assertThat(resultadoNulo.isDisponivel()).isFalse();

        ClimaAtual resultadoVazio = service.consultarClimaAtual("   ");
        assertThat(resultadoVazio.isDisponivel()).isFalse();

        verifyNoInteractions(climaMunicipioPort);
    }

    @Test
    void deveConsultarPortEGuardarEmCacheQuandoDisponivel() {
        ClimaAtual mockClima = ClimaAtual.disponivel(20, CondicaoTempo.ENSOLARADO, "Céu claro");
        when(climaMunicipioPort.obterClimaDiaAtual("4204806")).thenReturn(mockClima);

        ClimaAtual primeiraConsulta = service.consultarClimaAtual("4204806");
        ClimaAtual segundaConsulta = service.consultarClimaAtual("4204806");

        assertThat(primeiraConsulta.isDisponivel()).isTrue();
        assertThat(primeiraConsulta.getTemperatura()).isEqualTo(20);
        assertThat(segundaConsulta).isSameAs(primeiraConsulta);

        verify(climaMunicipioPort, times(1)).obterClimaDiaAtual("4204806");
    }

    @Test
    void naoDeveGuardarEmCacheQuandoIndisponivel() {
        when(climaMunicipioPort.obterClimaDiaAtual("4204806")).thenReturn(ClimaAtual.indisponivel());

        ClimaAtual primeiraConsulta = service.consultarClimaAtual("4204806");
        ClimaAtual segundaConsulta = service.consultarClimaAtual("4204806");

        assertThat(primeiraConsulta.isDisponivel()).isFalse();
        assertThat(segundaConsulta.isDisponivel()).isFalse();

        verify(climaMunicipioPort, times(2)).obterClimaDiaAtual("4204806");
    }
}
