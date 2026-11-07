package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.assistente.application.port.in.PerguntarAssistenteUseCase;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.PerguntaAssistenteRequest;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.RespostaAssistenteResponse;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.mapper.AssistenteWebMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/assistente")
public class AssistenteController {

    private final PerguntarAssistenteUseCase perguntarAssistenteUseCase;
    private final AssistenteWebMapper mapper;

    @Autowired
    public AssistenteController(
            PerguntarAssistenteUseCase perguntarAssistenteUseCase,
            AssistenteWebMapper mapper
    ) {
        this.perguntarAssistenteUseCase = Objects.requireNonNull(perguntarAssistenteUseCase);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @PostMapping("/perguntar")
    public ResponseEntity<RespostaAssistenteResponse> perguntar(
            @Valid @RequestBody PerguntaAssistenteRequest request
    ) {
        PerguntaProdutor pergunta = mapper.toDomain(request);
        RespostaAssistente resposta = perguntarAssistenteUseCase.perguntar(pergunta);
        return ResponseEntity.ok(mapper.toResponse(resposta));
    }
}
