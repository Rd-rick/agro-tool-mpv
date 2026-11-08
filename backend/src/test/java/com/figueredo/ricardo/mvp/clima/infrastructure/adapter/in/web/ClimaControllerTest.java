package com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.clima.application.port.in.ConsultarClimaAtualUseCase;
import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;
import com.figueredo.ricardo.mvp.clima.domain.model.CondicaoTempo;
import com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web.dto.ClimaAtualResponse;
import com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web.mapper.ClimaWebMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClimaControllerTest {

    @Mock
    private ConsultarClimaAtualUseCase useCase;

    private ClimaController controller;

    @BeforeEach
    void setUp() {
        controller = new ClimaController(useCase, new ClimaWebMapper());
    }

    @Test
    void deveRetornarClimaDisponivelComSucesso() {
        ClimaAtual mockClima = ClimaAtual.disponivel(18, CondicaoTempo.ENSOLARADO, "Poucas nuvens");
        when(useCase.consultarClimaAtual("4204806")).thenReturn(mockClima);

        ResponseEntity<ClimaAtualResponse> response = controller.obterClimaAtual("4204806");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().disponivel()).isTrue();
        assertThat(response.getBody().temperatura()).isEqualTo(18);
        assertThat(response.getBody().condicao()).isEqualTo("ENSOLARADO");
        assertThat(response.getBody().descricaoCondicao()).isEqualTo("Poucas nuvens");

        verify(useCase).consultarClimaAtual("4204806");
    }

    @Test
    void deveRetornarIndisponivelQuandoServicoNaoPossuiDados() {
        when(useCase.consultarClimaAtual("4204806")).thenReturn(ClimaAtual.indisponivel());

        ResponseEntity<ClimaAtualResponse> response = controller.obterClimaAtual("4204806");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().disponivel()).isFalse();
        assertThat(response.getBody().mensagem()).isEqualTo("Temporariamente indisponível");
        assertThat(response.getBody().temperatura()).isNull();
        assertThat(response.getBody().condicao()).isNull();

        verify(useCase).consultarClimaAtual("4204806");
    }
}
