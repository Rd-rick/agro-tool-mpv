package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoPamProducaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataDecisaoPamProducaoRepository extends JpaRepository<DecisaoPamProducaoEntity, Long> {

    List<DecisaoPamProducaoEntity> findAllByMunicipioIdAndCulturaCodigoFonteIdInOrderByAnoDesc(
            Long municipioId,
            Collection<Long> culturaCodigoFonteIds
    );
}
