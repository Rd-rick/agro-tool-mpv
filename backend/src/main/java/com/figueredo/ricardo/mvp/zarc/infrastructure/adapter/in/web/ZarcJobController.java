package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ImportarZarcUseCase;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto.ExecutarImportacaoZarcResponse;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.mapper.ZarcJobWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs/zarc")
public class ZarcJobController {

    private final ImportarZarcUseCase importarZarcUseCase;
    private final ZarcJobWebMapper mapper;

    public ZarcJobController(ImportarZarcUseCase importarZarcUseCase, ZarcJobWebMapper mapper) {
        this.importarZarcUseCase = importarZarcUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/executar")
    public ResponseEntity<ExecutarImportacaoZarcResponse> executarTodas() {
        var resultado = importarZarcUseCase.importarTodasSafrasConfiguradas();
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }

    @PostMapping("/executar/{safra}")
    public ResponseEntity<ExecutarImportacaoZarcResponse> executarSafra(@PathVariable String safra) {
        if (safra == null || safra.isBlank() || safra.length() > 20) {
            return ResponseEntity.badRequest().build();
        }
        var resultado = importarZarcUseCase.importarSafra(safra.trim());
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }
}
