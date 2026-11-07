package com.figueredo.ricardo.mvp.assistente;

import com.figueredo.ricardo.mvp.assistente.application.port.in.PerguntarAssistenteUseCase;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AssistenteLlmPort;
import com.figueredo.ricardo.mvp.assistente.application.service.AssistenteIaService;
import com.figueredo.ricardo.mvp.assistente.application.service.ExecutorFerramentasAgroService;
import com.figueredo.ricardo.mvp.assistente.application.service.PromptConsultivoBuilder;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.AssistenteController;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.mapper.AssistenteWebMapper;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.out.llm.OllamaHttpClientAdapter;
import com.figueredo.ricardo.mvp.assistente.infrastructure.config.AssistenteAiProperties;
import com.figueredo.ricardo.mvp.assistente.infrastructure.config.AssistenteConfig;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AssistenteConfig.class,
        AssistenteAiProperties.class,
        PromptConsultivoBuilder.class,
        ExecutorFerramentasAgroService.class,
        OllamaHttpClientAdapter.class,
        AssistenteIaService.class,
        AssistenteWebMapper.class,
        AssistenteController.class
})
class AssistenteSpringContextTest {

    @MockitoBean
    private ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;

    @MockitoBean
    private CalcularRankingOportunidadesUseCase calcularRankingUseCase;

    @MockitoBean
    private ConsultarAnaliseCulturaUseCase consultarAnaliseCulturaUseCase;

    @MockitoBean
    private CompararCulturasUseCase compararCulturasUseCase;

    @Autowired
    private PerguntarAssistenteUseCase perguntarAssistenteUseCase;

    @Autowired
    private AssistenteLlmPort assistenteLlmPort;

    @Autowired
    private AssistenteController assistenteController;

    @Test
    void deveCarregarContextoSpringDoModuloAssistenteComSucesso() {
        assertNotNull(perguntarAssistenteUseCase);
        assertNotNull(assistenteLlmPort);
        assertNotNull(assistenteController);
    }
}
