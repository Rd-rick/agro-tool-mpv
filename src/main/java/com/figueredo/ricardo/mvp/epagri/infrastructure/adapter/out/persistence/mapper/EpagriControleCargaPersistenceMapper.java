package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.epagri.domain.model.ControleCargaEpagri;
import com.figueredo.ricardo.mvp.epagri.domain.model.StatusCarga;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.ControleCargaEpagriEntity;
import org.springframework.stereotype.Component;

@Component
public class EpagriControleCargaPersistenceMapper {

    public ControleCargaEpagri toDomain(ControleCargaEpagriEntity entity) {
        return new ControleCargaEpagri(
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

    public ControleCargaEpagriEntity toEntity(ControleCargaEpagri domain) {
        ControleCargaEpagriEntity entity = new ControleCargaEpagriEntity();
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
