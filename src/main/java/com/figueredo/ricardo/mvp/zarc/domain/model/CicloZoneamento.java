package com.figueredo.ricardo.mvp.zarc.domain.model;

public record CicloZoneamento(
        String codigo,
        String descricao
) implements Comparable<CicloZoneamento> {

    public CicloZoneamento {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("codigo do ciclo é obrigatório");
        }
        codigo = codigo.trim();
        descricao = descricao != null && !descricao.isBlank() ? descricao.trim() : codigo;
    }

    @Override
    public int compareTo(CicloZoneamento outro) {
        return this.codigo.compareToIgnoreCase(outro.codigo);
    }
}
