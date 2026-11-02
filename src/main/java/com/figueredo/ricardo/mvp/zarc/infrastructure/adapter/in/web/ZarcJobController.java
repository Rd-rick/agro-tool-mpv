package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ImportarZarcUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs/zarc")
public class ZarcJobController {

    private final ImportarZarcUseCase importarZarcUseCase;

    public ZarcJobController(ImportarZarcUseCase importarZarcUseCase) {
        this.importarZarcUseCase = importarZarcUseCase;
    }

    @PostMapping("/executar")
    public ResponseEntity<ImportarZarcUseCase.ResultadoImportacao> executarTodas() {
        var resultado = importarZarcUseCase.importarTodasSafrasConfiguradas();
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/executar/{safra}")
    public ResponseEntity<ImportarZarcUseCase.ResultadoImportacao> executarSafra(@PathVariable String safra) {
        if (safra == null || safra.isBlank() || safra.length() > 20) {
            return ResponseEntity.badRequest().build();
        }
        var resultado = importarZarcUseCase.importarSafra(safra.trim());
        return ResponseEntity.ok(resultado);
    }
}
