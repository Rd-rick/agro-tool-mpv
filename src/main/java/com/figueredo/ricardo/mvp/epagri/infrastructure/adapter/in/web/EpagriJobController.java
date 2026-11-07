package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.epagri.application.port.in.ImportarPrecosEpagriUseCase;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web.dto.ExecutarImportacaoEpagriResponse;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web.mapper.EpagriJobWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs/epagri")
public class EpagriJobController {

    private final ImportarPrecosEpagriUseCase importarPrecosEpagriUseCase;
    private final EpagriJobWebMapper mapper;

    public EpagriJobController(
            ImportarPrecosEpagriUseCase importarPrecosEpagriUseCase,
            EpagriJobWebMapper mapper
    ) {
        this.importarPrecosEpagriUseCase = importarPrecosEpagriUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/executar")
    public ResponseEntity<ExecutarImportacaoEpagriResponse> executarTodas() {
        var resultado = importarPrecosEpagriUseCase.importarTodasFrequenciasConfiguradas();
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }

    @PostMapping("/executar/{frequencia}")
    public ResponseEntity<ExecutarImportacaoEpagriResponse> executarFrequencia(@PathVariable String frequencia) {
        if (frequencia == null || frequencia.isBlank() || frequencia.length() > 20) {
            return ResponseEntity.badRequest().build();
        }

        FrequenciaCotacao freqEnum;
        try {
            freqEnum = FrequenciaCotacao.valueOf(frequencia.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        var resultado = importarPrecosEpagriUseCase.importarFrequencia(freqEnum);
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }
}
