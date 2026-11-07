package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.entity.PamControleCargaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataPamControleCargaRepository extends JpaRepository<PamControleCargaEntity, Long> {

    Optional<PamControleCargaEntity> findTopByFonteAndIdentificadorRecursoAndStatusOrderByIniciadoEmDesc(
            String fonte,
            String identificadorRecurso,
            String status
    );
}
