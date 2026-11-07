package com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoMunicipioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataMercadoMunicipioRepository extends JpaRepository<MercadoMunicipioEntity, Long> {

    Optional<MercadoMunicipioEntity> findByCodigoIbge(String codigoIbge);
}
