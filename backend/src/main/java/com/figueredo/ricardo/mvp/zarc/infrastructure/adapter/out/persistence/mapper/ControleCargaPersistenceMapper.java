package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.zarc.domain.model.ControleCarga;
import com.figueredo.ricardo.mvp.zarc.domain.model.StatusCarga;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ControleCargaEntity;
import org.springframework.stereotype.Component;

@Component
public class ControleCargaPersistenceMapper {

    public ControleCarga toDomain(ControleCargaEntity entity) {
        return new ControleCarga(
                entity.getId(),
                entity.getFonte(),
                entity.getIdentificadorRecurso(),
                entity.getUltimoModificadoFonte(),
                StatusCarga.valueOf(entity.getStatus()),
                entity.getTotalRegistrosProcessados(),
                entity.getMensagemErro(),
                entity.getIniciadoEm(),
                entity.getFinalizadoEm()
        );
    }

    public ControleCargaEntity toEntity(ControleCarga domain) {
        ControleCargaEntity entity = new ControleCargaEntity();
        entity.setId(domain.getId());
        entity.setFonte(domain.getFonte());
        entity.setIdentificadorRecurso(domain.getIdentificadorRecurso());
        entity.setUltimoModificadoFonte(domain.getUltimoModificadoFonte());
        entity.setStatus(domain.getStatus().name());
        entity.setTotalRegistrosProcessados(domain.getTotalRegistrosProcessados());
        entity.setMensagemErro(domain.getMensagemErro());
        entity.setIniciadoEm(domain.getIniciadoEm());
        entity.setFinalizadoEm(domain.getFinalizadoEm());
        return entity;
    }
}
