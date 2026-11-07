package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.in.web.dto;

import java.util.List;

public record ExecutarImportacaoPamResponse(
        int totalAnosProcessados,
        int totalRegistrosImportados,
        List<String> detalhes
) {
}
