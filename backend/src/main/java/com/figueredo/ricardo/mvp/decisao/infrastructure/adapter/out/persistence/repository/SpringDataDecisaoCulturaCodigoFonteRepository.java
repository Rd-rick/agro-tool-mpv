package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoCulturaCodigoFonteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataDecisaoCulturaCodigoFonteRepository extends JpaRepository<DecisaoCulturaCodigoFonteEntity, Long> {

    List<DecisaoCulturaCodigoFonteEntity> findAllByCulturaIdInAndFonteAndAtivoTrue(
            Collection<Long> culturaIds,
            String fonte
    );
}
