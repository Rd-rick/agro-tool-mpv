package com.figueredo.ricardo.mvp.assistente.application.port.out;

import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;

import java.util.List;

public interface AssistenteLlmPort {

    RespostaAssistente enviarMensagem(
            String systemPrompt,
            PerguntaProdutor pergunta,
            List<DefinicaoFerramentaIa> ferramentasDisponiveis
    );
}
