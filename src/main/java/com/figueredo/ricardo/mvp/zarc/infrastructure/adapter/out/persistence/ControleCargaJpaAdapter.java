package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.zarc.application.port.out.ControleCargaPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.ControleCarga;
import com.figueredo.ricardo.mvp.zarc.domain.model.StatusCarga;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ControleCargaEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.mapper.ControleCargaPersistenceMapper;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository.SpringDataControleCargaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class ControleCargaJpaAdapter implements ControleCargaPort {

    private final SpringDataControleCargaRepository repository;
    private final ControleCargaPersistenceMapper mapper;

    public ControleCargaJpaAdapter(
            SpringDataControleCargaRepository repository,
            ControleCargaPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ControleCarga> buscarUltimaCargaComSucesso(String fonte, String identificadorRecurso) {
        return repository.findTopByFonteAndIdentificadorRecursoAndStatusOrderByIniciadoEmDesc(
                        fonte,
                        identificadorRecurso,
                        StatusCarga.SUCESSO.name()
                )
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public ControleCarga salvar(ControleCarga controleCarga) {
        ControleCargaEntity entity = mapper.toEntity(controleCarga);
        ControleCargaEntity salva = repository.save(entity);
        return mapper.toDomain(salva);
    }
}
