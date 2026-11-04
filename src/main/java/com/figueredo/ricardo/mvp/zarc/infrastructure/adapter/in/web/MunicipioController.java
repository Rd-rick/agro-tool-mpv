package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.zarc.application.port.in.ListarMunicipiosUseCase;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto.ListarMunicipiosResponse;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.mapper.MunicipioWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/municipios")
public class MunicipioController {

    private final ListarMunicipiosUseCase listarMunicipiosUseCase;
    private final MunicipioWebMapper mapper;

    public MunicipioController(ListarMunicipiosUseCase listarMunicipiosUseCase, MunicipioWebMapper mapper) {
        this.listarMunicipiosUseCase = listarMunicipiosUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<ListarMunicipiosResponse> listar() {
        List<Municipio> municipios = listarMunicipiosUseCase.listarTodos();
        return ResponseEntity.ok(mapper.toResponse(municipios));
    }
}
