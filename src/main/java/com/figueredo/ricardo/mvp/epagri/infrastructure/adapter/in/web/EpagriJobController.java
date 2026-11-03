package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.epagri.application.port.in.ImportarPrecosEpagriUseCase;
import com.figueredo.ricardo.mvp.epagri.domain.model.FrequenciaCotacao;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs/epagri")
public class EpagriJobController {

    private final ImportarPrecosEpagriUseCase importarPrecosEpagriUseCase;

    public EpagriJobController(ImportarPrecosEpagriUseCase importarPrecosEpagriUseCase) {
        this.importarPrecosEpagriUseCase = importarPrecosEpagriUseCase;
    }

    @PostMapping("/executar")
    public ResponseEntity<ImportarPrecosEpagriUseCase.ResultadoImportacao> executarTodas() {
        var resultado = importarPrecosEpagriUseCase.importarTodasFrequenciasConfiguradas();
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/executar/{frequencia}")
    public ResponseEntity<ImportarPrecosEpagriUseCase.ResultadoImportacao> executarFrequencia(@PathVariable String frequencia) {
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
        return ResponseEntity.ok(resultado);
    }
}
