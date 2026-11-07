package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.entity.PamCulturaCodigoFonteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPamCulturaCodigoFonteRepository extends JpaRepository<PamCulturaCodigoFonteEntity, Long> {

    List<PamCulturaCodigoFonteEntity> findAllByFonteAndAtivoTrue(String fonte);
}
