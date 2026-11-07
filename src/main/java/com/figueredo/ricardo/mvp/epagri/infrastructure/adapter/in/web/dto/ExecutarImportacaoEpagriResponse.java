package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.in.web.dto;

import java.util.List;

public record ExecutarImportacaoEpagriResponse(
        int totalFrequenciasProcessadas,
        int totalRegistrosImportados,
        List<String> detalhes
) {
}
