package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.ControleCargaEpagriEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataControleCargaEpagriRepository extends JpaRepository<ControleCargaEpagriEntity, Long> {

    Optional<ControleCargaEpagriEntity> findTopByFonteAndIdentificadorRecursoAndStatusOrderByIniciadoEmDesc(
            String fonte,
            String identificadorRecurso,
            String status
    );
}
