package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.pam.domain.model.ControleCargaPam;
import com.figueredo.ricardo.mvp.pam.domain.model.StatusCargaPam;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.entity.PamControleCargaEntity;
import org.springframework.stereotype.Component;

@Component
public class PamControleCargaPersistenceMapper {

    public ControleCargaPam toDomain(PamControleCargaEntity entity) {
        return new ControleCargaPam(
                entity.getId(),
                entity.getFonte(),
                entity.getIdentificadorRecurso(),
                entity.getUltimoModificadoFonte(),
                StatusCargaPam.valueOf(entity.getStatus()),
                entity.getTotalRegistrosProcessados(),
                entity.getMensagemErro(),
                entity.getIniciadoEm(),
                entity.getFinalizadoEm()
        );
    }

    public PamControleCargaEntity toEntity(ControleCargaPam domain) {
        PamControleCargaEntity entity = new PamControleCargaEntity();
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
