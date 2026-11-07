package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase.FiltroCompararCulturas;
import com.figueredo.ricardo.mvp.decisao.domain.model.ComparacaoCulturasResultado;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasRequest;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.CompararCulturasResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper.ComparadorCulturasWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comparador")
public class ComparadorCulturasController {

    private final CompararCulturasUseCase compararCulturasUseCase;
    private final ComparadorCulturasWebMapper mapper;

    public ComparadorCulturasController(
            CompararCulturasUseCase compararCulturasUseCase,
            ComparadorCulturasWebMapper mapper
    ) {
        this.compararCulturasUseCase = compararCulturasUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<CompararCulturasResponse> compararCulturas(
            @Valid CompararCulturasRequest request
    ) {
        FiltroCompararCulturas filtro = mapper.toFiltro(request);
        ComparacaoCulturasResultado resultado = compararCulturasUseCase.compararCulturas(filtro);
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }
}
