package com.figueredo.ricardo.mvp.assistente.application.port.in;

import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;

public interface PerguntarAssistenteUseCase {
    RespostaAssistente perguntar(PerguntaProdutor pergunta);
}
