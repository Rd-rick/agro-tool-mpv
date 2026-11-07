package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto;

import java.util.Map;

public record ChamadaFerramentaResponse(
        String ferramenta,
        Map<String, Object> argumentos
) {
}
