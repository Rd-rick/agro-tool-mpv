package com.figueredo.ricardo.mvp.assistente.application.service;

import com.figueredo.ricardo.mvp.assistente.application.port.in.PerguntarAssistenteUseCase;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AgroToolFunctionsExecutor;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AssistenteLlmPort;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AssistenteIaService implements PerguntarAssistenteUseCase {

    private final PromptConsultivoBuilder promptBuilder;
    private final AgroToolFunctionsExecutor toolExecutor;
    private final AssistenteLlmPort llmPort;

    @Autowired
    public AssistenteIaService(
            PromptConsultivoBuilder promptBuilder,
            AgroToolFunctionsExecutor toolExecutor,
            AssistenteLlmPort llmPort
    ) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder);
        this.toolExecutor = Objects.requireNonNull(toolExecutor);
        this.llmPort = Objects.requireNonNull(llmPort);
    }

    @Override
    public RespostaAssistente perguntar(PerguntaProdutor pergunta) {
        Objects.requireNonNull(pergunta, "pergunta é obrigatória");
        String systemPrompt = promptBuilder.construirSystemPrompt();
        List<DefinicaoFerramentaIa> ferramentas = toolExecutor.obterDefinicoes();
        return llmPort.enviarMensagem(systemPrompt, pergunta, ferramentas);
    }
}
