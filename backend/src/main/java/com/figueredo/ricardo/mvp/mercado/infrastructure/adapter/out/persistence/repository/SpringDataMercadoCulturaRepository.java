package com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoCulturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataMercadoCulturaRepository extends JpaRepository<MercadoCulturaEntity, Long> {

    List<MercadoCulturaEntity> findAllByIdInAndAtivoTrue(Collection<Long> ids);
}
