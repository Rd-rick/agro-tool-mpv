package com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.clima.application.port.in.ConsultarClimaAtualUseCase;
import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;
import com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web.dto.ClimaAtualResponse;
import com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web.mapper.ClimaWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clima")
public class ClimaController {

    private final ConsultarClimaAtualUseCase consultarClimaAtualUseCase;
    private final ClimaWebMapper mapper;

    public ClimaController(
            ConsultarClimaAtualUseCase consultarClimaAtualUseCase,
            ClimaWebMapper mapper
    ) {
        this.consultarClimaAtualUseCase = consultarClimaAtualUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/atual/{codigoIbge}")
    public ResponseEntity<ClimaAtualResponse> obterClimaAtual(@PathVariable String codigoIbge) {
        ClimaAtual clima = consultarClimaAtualUseCase.consultarClimaAtual(codigoIbge);
        return ResponseEntity.ok(mapper.toResponse(clima));
    }
}
