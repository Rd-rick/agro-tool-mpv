package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaCodigoFonteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataCulturaCodigoFonteRepository extends JpaRepository<CulturaCodigoFonteEntity, Long> {

    List<CulturaCodigoFonteEntity> findAllByFonteAndAtivoTrue(String fonte);

    List<CulturaCodigoFonteEntity> findAllByIdInAndAtivoTrue(Collection<Long> ids);
}
