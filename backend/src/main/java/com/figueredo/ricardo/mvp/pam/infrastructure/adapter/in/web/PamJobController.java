package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.pam.application.port.in.ImportarPamUseCase;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web.dto.ExecutarImportacaoPamResponse;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web.mapper.PamJobWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/jobs/pam")
public class PamJobController {

    private static final int ANO_MINIMO = 2010;
    private static final int ANO_MAXIMO = 2100;

    private final ImportarPamUseCase importarPamUseCase;
    private final PamJobWebMapper mapper;

    public PamJobController(ImportarPamUseCase importarPamUseCase, PamJobWebMapper mapper) {
        this.importarPamUseCase = importarPamUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/executar")
    public ResponseEntity<ExecutarImportacaoPamResponse> executarTodos() {
        var resultado = importarPamUseCase.importarTodosAnosConfigurados();
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }

    @PostMapping("/executar/{ano}")
    public ResponseEntity<ExecutarImportacaoPamResponse> executarAno(@PathVariable String ano) {
        if (ano == null || ano.isBlank() || ano.trim().length() != 4) {
            return ResponseEntity.badRequest().build();
        }

        int anoNumerico;
        try {
            anoNumerico = Integer.parseInt(ano.trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }

        if (anoNumerico < ANO_MINIMO || anoNumerico > ANO_MAXIMO) {
            return ResponseEntity.badRequest().build();
        }

        var resultado = importarPamUseCase.importarAno(anoNumerico);
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }
}
