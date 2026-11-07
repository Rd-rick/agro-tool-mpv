package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.pam.application.port.out.PamControleCargaPort;
import com.figueredo.ricardo.mvp.pam.domain.model.ControleCargaPam;
import com.figueredo.ricardo.mvp.pam.domain.model.StatusCargaPam;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.entity.PamControleCargaEntity;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.mapper.PamControleCargaPersistenceMapper;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.repository.SpringDataPamControleCargaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PamControleCargaJpaAdapter implements PamControleCargaPort {

    private final SpringDataPamControleCargaRepository repository;
    private final PamControleCargaPersistenceMapper mapper;

    public PamControleCargaJpaAdapter(
            SpringDataPamControleCargaRepository repository,
            PamControleCargaPersistenceMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ControleCargaPam> buscarUltimaCargaComSucesso(String fonte, String identificadorRecurso) {
        return repository.findTopByFonteAndIdentificadorRecursoAndStatusOrderByIniciadoEmDesc(
                        fonte,
                        identificadorRecurso,
                        StatusCargaPam.SUCESSO.name()
                )
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public ControleCargaPam salvar(ControleCargaPam controleCarga) {
        PamControleCargaEntity entity = mapper.toEntity(controleCarga);
        PamControleCargaEntity salva = repository.save(entity);
        return mapper.toDomain(salva);
    }
}
