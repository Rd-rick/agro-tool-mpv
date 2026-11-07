package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.RegiaoPracaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataRegiaoPracaRepository extends JpaRepository<RegiaoPracaEntity, Long> {

    List<RegiaoPracaEntity> findAllByUfAndAtivoTrue(String uf);
}
