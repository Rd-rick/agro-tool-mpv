package com.figueredo.ricardo.mvp.zarc.domain.model;

public record SoloZoneamento(
        String codigo,
        String descricao
) implements Comparable<SoloZoneamento> {

    public SoloZoneamento {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("codigo do solo é obrigatório");
        }
        codigo = codigo.trim();
        descricao = descricao != null && !descricao.isBlank() ? descricao.trim() : codigo;
    }

    @Override
    public int compareTo(SoloZoneamento outro) {
        return this.codigo.compareToIgnoreCase(outro.codigo);
    }
}
