package com.figueredo.ricardo.mvp.clima.infrastructure.adapter.out.http;

import tools.jackson.databind.ObjectMapper;
import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;
import com.figueredo.ricardo.mvp.clima.domain.model.CondicaoTempo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class InmetPrevisaoHttpAdapterTest {

    private MockRestServiceServer mockServer;
    private InmetPrevisaoHttpAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://apiprevmet3.inmet.gov.br");
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        adapter = new InmetPrevisaoHttpAdapter(restClient, new ObjectMapper());
    }

    @Test
    void deveObterClimaComSucessoQuandoRespostaInmetValida() {
        String json = """
                {
                    "4204806": {
                        "08/11/2026": {
                            "manha": {
                                "resumo": "Poucas nuvens",
                                "temp_max": 19,
                                "temp_min": 8
                            },
                            "tarde": {
                                "resumo": "Poucas nuvens",
                                "temp_max": 20,
                                "temp_min": 8
                            },
                            "noite": {
                                "resumo": "Poucas nuvens",
                                "temp_max": 17,
                                "temp_min": 8
                            }
                        }
                    }
                }
                """;

        mockServer.expect(requestTo("https://apiprevmet3.inmet.gov.br/previsao/4204806"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        ClimaAtual resultado = adapter.obterClimaDiaAtual("4204806");

        assertThat(resultado.isDisponivel()).isTrue();
        assertThat(resultado.getTemperatura()).isNotNull();
        assertThat(resultado.getCondicao()).isEqualTo(CondicaoTempo.ENSOLARADO);
        assertThat(resultado.getDescricaoCondicao()).isEqualTo("Poucas nuvens");
    }

    @Test
    void deveRetornarIndisponivelQuandoInmetRetornaErro500() {
        mockServer.expect(requestTo("https://apiprevmet3.inmet.gov.br/previsao/4204806"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());

        ClimaAtual resultado = adapter.obterClimaDiaAtual("4204806");

        assertThat(resultado.isDisponivel()).isFalse();
        assertThat(resultado.getMensagem()).isEqualTo("Temporariamente indisponível");
    }

    @Test
    void deveRetornarIndisponivelQuandoRespostaVazia() {
        String json = "{\"4204806\": {}}";

        mockServer.expect(requestTo("https://apiprevmet3.inmet.gov.br/previsao/4204806"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        ClimaAtual resultado = adapter.obterClimaDiaAtual("4204806");

        assertThat(resultado.isDisponivel()).isFalse();
    }
}
