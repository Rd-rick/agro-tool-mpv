package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataCulturaRepository extends JpaRepository<CulturaEntity, Long> {

    List<CulturaEntity> findAllByIdInAndAtivoTrue(Collection<Long> ids);
}
