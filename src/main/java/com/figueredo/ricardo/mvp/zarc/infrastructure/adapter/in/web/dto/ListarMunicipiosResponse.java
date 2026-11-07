package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto;

import java.util.List;

public record ListarMunicipiosResponse(
        List<MunicipioItemResponse> municipios
) {
    public record MunicipioItemResponse(
            String nome,
            String uf,
            String codigoIbge,
            Long pracaReferenciaId,
            String observacaoPraca
    ) {
    }
}
