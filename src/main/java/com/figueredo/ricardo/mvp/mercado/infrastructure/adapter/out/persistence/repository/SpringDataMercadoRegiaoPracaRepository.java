package com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoRegiaoPracaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataMercadoRegiaoPracaRepository extends JpaRepository<MercadoRegiaoPracaEntity, Long> {

    List<MercadoRegiaoPracaEntity> findAllByUfAndAtivoTrueOrderByNomeAsc(String uf);
}
