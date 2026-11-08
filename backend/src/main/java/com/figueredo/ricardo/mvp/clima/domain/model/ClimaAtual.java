package com.figueredo.ricardo.mvp.clima.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClimaAtual {
    private final boolean disponivel;
    private final String mensagem;
    private final Integer temperatura;
    private final CondicaoTempo condicao;
    private final String descricaoCondicao;

    public static ClimaAtual disponivel(Integer temperatura, CondicaoTempo condicao, String descricaoCondicao) {
        return ClimaAtual.builder()
                .disponivel(true)
                .temperatura(temperatura)
                .condicao(condicao)
                .descricaoCondicao(descricaoCondicao)
                .build();
    }

    public static ClimaAtual indisponivel() {
        return ClimaAtual.builder()
                .disponivel(false)
                .mensagem("Temporariamente indisponível")
                .build();
    }
}
