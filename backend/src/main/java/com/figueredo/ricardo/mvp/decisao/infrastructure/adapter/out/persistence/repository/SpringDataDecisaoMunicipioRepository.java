package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoMunicipioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataDecisaoMunicipioRepository extends JpaRepository<DecisaoMunicipioEntity, Long> {

    Optional<DecisaoMunicipioEntity> findByCodigoIbge(String codigoIbge);
}
