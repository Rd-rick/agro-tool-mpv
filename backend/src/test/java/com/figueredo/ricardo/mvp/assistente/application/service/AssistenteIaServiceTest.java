package com.figueredo.ricardo.mvp.assistente.application.service;

import com.figueredo.ricardo.mvp.assistente.application.port.out.AgroToolFunctionsExecutor;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AssistenteLlmPort;
import com.figueredo.ricardo.mvp.assistente.domain.model.ContextoProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import com.figueredo.ricardo.mvp.common.domain.exception.AssistenteIndisponivelException;
import com.figueredo.ricardo.mvp.common.domain.exception.AssistenteQuotaExcedidaException;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssistenteIaServiceTest {

    @Mock
    private AgroToolFunctionsExecutor toolExecutor;

    @Mock
    private AssistenteLlmPort llmPort;

    @Mock
    private MunicipioPort municipioPort;

    private PromptConsultivoBuilder promptBuilder;
    private AssistenteIaService service;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptConsultivoBuilder();
        service = new AssistenteIaService(promptBuilder, toolExecutor, llmPort, municipioPort);
    }

    @Test
    void deveDelegarPerguntaParaPortaLlmComSystemPromptEContextoCompleto() {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.resolverOuPadrao("2026-11", "2027-03", null);
        ContextoProdutor contexto = new ContextoProdutor("4204809", "Curitibanos", periodo, null, new BigDecimal("50.0"));
        PerguntaProdutor pergunta = new PerguntaProdutor("Qual a melhor cultura para minha região?", contexto);

        List<DefinicaoFerramentaIa> ferramentas = List.of(
                new DefinicaoFerramentaIa("teste_tool", "desc", Map.of())
        );
        when(toolExecutor.obterDefinicoes()).thenReturn(ferramentas);

        RespostaAssistente mockResposta = new RespostaAssistente(
                "A soja é recomendada pelo ZARC...",
                List.of(),
                List.of("ZARC - MAPA")
        );
        when(llmPort.enviarMensagem(any(), any(), eq(ferramentas)))
                .thenReturn(mockResposta);

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertEquals("A soja é recomendada pelo ZARC...", resposta.textoResposta());
        assertEquals(List.of("ZARC - MAPA"), resposta.fontesConsultadas());
        verify(toolExecutor).obterDefinicoes();
        verify(llmPort).enviarMensagem(any(), any(), eq(ferramentas));
    }

    @Test
    void naoDeveChamarIaQuandoContextoMinimoEstiverIncompleto() {
        ContextoProdutor contexto = new ContextoProdutor("4204809", "Curitibanos", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Qual a melhor cultura para plantar?", contexto);

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertTrue(resposta.textoResposta().contains("filtros no topo da página"));
        assertTrue(resposta.textoResposta().contains("Consultar"));
        verifyNoInteractions(llmPort);
        verifyNoInteractions(toolExecutor);
    }

    @Test
    void deveResolverMunicipioPorNomeSemAcentoNoBackend() {
        Municipio chapeco = new Municipio("Chapecó", "SC", "4204202", null, null);
        when(municipioPort.buscarPorNome("chapeco")).thenReturn(Optional.empty());
        when(municipioPort.listarTodosOrdenadosPorNome()).thenReturn(List.of(chapeco));

        ContextoProdutor contexto = new ContextoProdutor(null, "chapeco", null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Olá", contexto);

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertNotNull(resposta.contextoConsolidado());
        assertEquals("4204202", resposta.contextoConsolidado().codigoIbge());
        assertTrue(resposta.textoResposta().contains("Consultar"));
        verifyNoInteractions(llmPort);
    }

    @Test
    void deveConfirmarContextoCompletoSemPerguntaESugerirProximaAcao() {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.resolverOuPadrao("2026-11", "2027-03", null);
        ContextoProdutor contexto = new ContextoProdutor("4204809", "Curitibanos", periodo, null, new BigDecimal("50.0"));
        PerguntaProdutor pergunta = new PerguntaProdutor("Curitibanos, 50 ha, novembro a março", contexto);

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertTrue(resposta.textoResposta().contains("Contexto completo registrado"));
        assertTrue(resposta.textoResposta().contains("Curitibanos"));
        assertTrue(resposta.textoResposta().contains("Ver ranking de culturas"));
        verifyNoInteractions(llmPort);
    }

    @Test
    void deveResponderSaudacaoReconhecendoContextoCompleto() {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.resolverOuPadrao("2026-11", "2027-03", null);
        ContextoProdutor contexto = new ContextoProdutor("4204809", "Curitibanos", periodo, null, new BigDecimal("50.0"));
        PerguntaProdutor pergunta = new PerguntaProdutor("Bom dia!", contexto);

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertTrue(resposta.textoResposta().contains("Bom dia!"));
        assertTrue(resposta.textoResposta().contains("Curitibanos"));
        assertTrue(resposta.textoResposta().contains("pronto para análise"));
        verifyNoInteractions(llmPort);
    }

    @Test
    void deveTratarErroDeQuotaDaIaERetornarMensagemPadrao() {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.resolverOuPadrao("2026-11", "2027-03", null);
        ContextoProdutor contexto = new ContextoProdutor("4204809", "Curitibanos", periodo, null, new BigDecimal("50.0"));
        PerguntaProdutor pergunta = new PerguntaProdutor("Qual cultura tem melhor preço?", contexto);

        when(toolExecutor.obterDefinicoes()).thenReturn(List.of());
        when(llmPort.enviarMensagem(any(), any(), any()))
                .thenThrow(new AssistenteQuotaExcedidaException("Quota exceeded"));

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertEquals("Estamos temporariamente indisponíveis. Por favor, tente novamente amanhã.", resposta.textoResposta());
    }

    @Test
    void deveTratarErroGenericoDaIaERetornarMensagemPadrao() {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.resolverOuPadrao("2026-11", "2027-03", null);
        ContextoProdutor contexto = new ContextoProdutor("4204809", "Curitibanos", periodo, null, new BigDecimal("50.0"));
        PerguntaProdutor pergunta = new PerguntaProdutor("Qual cultura tem melhor preço?", contexto);

        when(toolExecutor.obterDefinicoes()).thenReturn(List.of());
        when(llmPort.enviarMensagem(any(), any(), any()))
                .thenThrow(new AssistenteIndisponivelException("Network timeout"));

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertEquals("O assistente inteligente está temporariamente indisponível no momento. Por favor, tente novamente mais tarde.", resposta.textoResposta());
    }

    @Test
    void deveOrientarPreenchimentoDeFiltrosAoReceberContextoIncompleto() {
        ContextoProdutor contexto = new ContextoProdutor(null, null, null, null, null);
        PerguntaProdutor pergunta = new PerguntaProdutor("Quais culturas devo plantar?", contexto);

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertTrue(resposta.textoResposta().contains("filtros no topo da página"));
        assertTrue(resposta.textoResposta().contains("Consultar"));
        verifyNoInteractions(llmPort);
    }

    @Test
    void deveChamarIaQuandoPerguntaForVerRankingDeCulturas() {
        PeriodoPlantioMeses periodo = PeriodoPlantioMeses.resolverOuPadrao("2026-11", "2027-03", null);
        ContextoProdutor contexto = new ContextoProdutor("4204809", "Curitibanos", periodo, null, new BigDecimal("50.0"));
        PerguntaProdutor pergunta = new PerguntaProdutor("Ver ranking de culturas", contexto);

        List<DefinicaoFerramentaIa> ferramentas = List.of();
        when(toolExecutor.obterDefinicoes()).thenReturn(ferramentas);
        when(llmPort.enviarMensagem(any(), any(), any()))
                .thenReturn(new RespostaAssistente("Aqui está o ranking...", List.of(), List.of()));

        RespostaAssistente resposta = service.perguntar(pergunta);

        assertNotNull(resposta);
        assertEquals("Aqui está o ranking...", resposta.textoResposta());
        verify(llmPort).enviarMensagem(any(), any(), any());
    }
}
