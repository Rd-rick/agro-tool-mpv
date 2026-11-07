package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase.FiltroAnaliseCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.AnaliseCulturaResultado;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.AnaliseCulturaResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.ConsultarAnaliseCulturaRequest;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper.AnaliseCulturaWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/municipios")
public class AnaliseCulturaController {

    private final ConsultarAnaliseCulturaUseCase consultarAnaliseCulturaUseCase;
    private final AnaliseCulturaWebMapper mapper;

    public AnaliseCulturaController(
            ConsultarAnaliseCulturaUseCase consultarAnaliseCulturaUseCase,
            AnaliseCulturaWebMapper mapper
    ) {
        this.consultarAnaliseCulturaUseCase = consultarAnaliseCulturaUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{codigoIbge}/culturas/{culturaId}/analise")
    public ResponseEntity<AnaliseCulturaResponse> consultarAnaliseCultura(
            @Valid ConsultarAnaliseCulturaRequest request
    ) {
        FiltroAnaliseCultura filtro = mapper.toFiltro(request);
        AnaliseCulturaResultado resultado = consultarAnaliseCulturaUseCase.consultarAnaliseCultura(filtro);
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }
}
