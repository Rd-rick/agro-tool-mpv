package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto.ListarMunicipiosResponse;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.in.web.dto.ListarMunicipiosResponse.MunicipioItemResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MunicipioWebMapper {

    public ListarMunicipiosResponse toResponse(List<Municipio> municipios) {
        List<MunicipioItemResponse> itens = municipios.stream()
                .map(this::toItemResponse)
                .toList();
        return new ListarMunicipiosResponse(itens);
    }

    public MunicipioItemResponse toItemResponse(Municipio municipio) {
        return new MunicipioItemResponse(
                municipio.nome(),
                municipio.uf(),
                municipio.codigoIbge()
        );
    }
}
