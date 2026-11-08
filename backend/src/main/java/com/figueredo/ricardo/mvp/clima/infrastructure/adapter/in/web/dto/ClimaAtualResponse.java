package com.figueredo.ricardo.mvp.clima.infrastructure.adapter.in.web.dto;

public record ClimaAtualResponse(
        boolean disponivel,
        String mensagem,
        Integer temperatura,
        String condicao,
        String descricaoCondicao
) {}
