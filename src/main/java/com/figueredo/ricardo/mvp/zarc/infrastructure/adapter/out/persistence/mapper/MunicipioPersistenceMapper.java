package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.MunicipioEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MunicipioPersistenceMapper {

    public List<Municipio> toDomainList(List<MunicipioEntity> entities) {
        return entities.stream()
                .map(this::toDomain)
                .toList();
    }

    public Municipio toDomain(MunicipioEntity entity) {
        return new Municipio(
                entity.getNome(),
                entity.getUf(),
                entity.getCodigoIbge()
        );
    }
}
