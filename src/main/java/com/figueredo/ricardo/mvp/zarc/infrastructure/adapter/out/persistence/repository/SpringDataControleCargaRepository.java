package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ControleCargaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataControleCargaRepository extends JpaRepository<ControleCargaEntity, Long> {

    Optional<ControleCargaEntity> findTopByFonteAndIdentificadorRecursoAndStatusOrderByIniciadoEmDesc(
            String fonte,
            String identificadorRecurso,
            String status
    );
}
