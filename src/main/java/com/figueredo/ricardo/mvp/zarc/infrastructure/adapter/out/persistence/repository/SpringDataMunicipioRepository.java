package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.MunicipioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataMunicipioRepository extends JpaRepository<MunicipioEntity, Long> {

    List<MunicipioEntity> findAllByUf(String uf);
}
