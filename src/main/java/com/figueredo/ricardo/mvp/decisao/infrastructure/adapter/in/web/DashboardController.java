package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web;

import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarDashboardUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarDashboardUseCase.FiltroDashboard;
import com.figueredo.ricardo.mvp.decisao.domain.model.DashboardResultado;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.ConsultarDashboardRequest;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.dto.DashboardResponse;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.in.web.mapper.DashboardWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final ConsultarDashboardUseCase consultarDashboardUseCase;
    private final DashboardWebMapper mapper;

    public DashboardController(
            ConsultarDashboardUseCase consultarDashboardUseCase,
            DashboardWebMapper mapper
    ) {
        this.consultarDashboardUseCase = consultarDashboardUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> consultarDashboard(
            @Valid ConsultarDashboardRequest request
    ) {
        FiltroDashboard filtro = mapper.toFiltro(request);
        DashboardResultado resultado = consultarDashboardUseCase.consultarDashboard(filtro);
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }
}
