package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.epagri.application.port.out.EpagriControleCargaPort;
import com.figueredo.ricardo.mvp.epagri.domain.model.ControleCargaEpagri;
import com.figueredo.ricardo.mvp.epagri.domain.model.StatusCarga;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.ControleCargaEpagriEntity;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.repository.SpringDataControleCargaEpagriRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class EpagriControleCargaJpaAdapter implements EpagriControleCargaPort {

    private final SpringDataControleCargaEpagriRepository repository;

    public EpagriControleCargaJpaAdapter(SpringDataControleCargaEpagriRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ControleCargaEpagri> buscarUltimaCargaComSucesso(String fonte, String identificadorRecurso) {
        return repository.findTopByFonteAndIdentificadorRecursoAndStatusOrderByIniciadoEmDesc(
                        fonte,
                        identificadorRecurso,
                        StatusCarga.SUCESSO.name()
                )
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public ControleCargaEpagri salvar(ControleCargaEpagri controleCarga) {
        ControleCargaEpagriEntity entity = toEntity(controleCarga);
        ControleCargaEpagriEntity salva = repository.save(entity);
        return toDomain(salva);
    }

    private ControleCargaEpagri toDomain(ControleCargaEpagriEntity entity) {
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

    private ControleCargaEpagriEntity toEntity(ControleCargaEpagri domain) {
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
