package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.zarc.application.port.out.ControleCargaPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.ControleCarga;
import com.figueredo.ricardo.mvp.zarc.domain.model.StatusCarga;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ControleCargaEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository.SpringDataControleCargaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class ControleCargaJpaAdapter implements ControleCargaPort {

    private final SpringDataControleCargaRepository repository;

    public ControleCargaJpaAdapter(SpringDataControleCargaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ControleCarga> buscarUltimaCargaComSucesso(String fonte, String identificadorRecurso) {
        return repository.findTopByFonteAndIdentificadorRecursoAndStatusOrderByIniciadoEmDesc(
                        fonte,
                        identificadorRecurso,
                        StatusCarga.SUCESSO.name()
                )
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public ControleCarga salvar(ControleCarga controleCarga) {
        ControleCargaEntity entity = toEntity(controleCarga);
        ControleCargaEntity salva = repository.save(entity);
        return toDomain(salva);
    }

    private ControleCarga toDomain(ControleCargaEntity entity) {
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

    private ControleCargaEntity toEntity(ControleCarga domain) {
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
