package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.entity.PamMunicipioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataPamMunicipioRepository extends JpaRepository<PamMunicipioEntity, Long> {

    List<PamMunicipioEntity> findAllByUf(String uf);
}
