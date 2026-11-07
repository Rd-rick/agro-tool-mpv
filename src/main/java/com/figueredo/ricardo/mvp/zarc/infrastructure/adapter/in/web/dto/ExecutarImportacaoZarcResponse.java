package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto;

import java.util.List;

public record ExecutarImportacaoZarcResponse(
        int totalSafrasProcessadas,
        int totalRegistrosImportados,
        List<String> detalhes
) {
}
