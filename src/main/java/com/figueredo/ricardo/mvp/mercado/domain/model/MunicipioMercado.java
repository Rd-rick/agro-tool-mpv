package com.figueredo.ricardo.mvp.mercado.domain.model;

public record MunicipioMercado(
        Long id,
        String codigoIbge,
        String nome,
        String uf,
        Long pracaReferenciaId,
        String observacaoPraca
) {
}
