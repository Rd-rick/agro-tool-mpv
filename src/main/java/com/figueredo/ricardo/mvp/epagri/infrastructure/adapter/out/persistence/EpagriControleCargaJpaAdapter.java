package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.epagri.application.port.out.EpagriControleCargaPort;
import com.figueredo.ricardo.mvp.epagri.domain.model.ControleCargaEpagri;
import com.figueredo.ricardo.mvp.epagri.domain.model.StatusCarga;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.ControleCargaEpagriEntity;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.mapper.EpagriControleCargaPersistenceMapper;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.repository.SpringDataControleCargaEpagriRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class EpagriControleCargaJpaAdapter implements EpagriControleCargaPort {

    private final SpringDataControleCargaEpagriRepository repository;
    private final EpagriControleCargaPersistenceMapper mapper;

    public EpagriControleCargaJpaAdapter(
            SpringDataControleCargaEpagriRepository repository,
            EpagriControleCargaPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ControleCargaEpagri> buscarUltimaCargaComSucesso(String fonte, String identificadorRecurso) {
        return repository.findTopByFonteAndIdentificadorRecursoAndStatusOrderByIniciadoEmDesc(
                        fonte,
                        identificadorRecurso,
                        StatusCarga.SUCESSO.name()
                )
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public ControleCargaEpagri salvar(ControleCargaEpagri controleCarga) {
        ControleCargaEpagriEntity entity = mapper.toEntity(controleCarga);
        ControleCargaEpagriEntity salva = repository.save(entity);
        return mapper.toDomain(salva);
    }
}
