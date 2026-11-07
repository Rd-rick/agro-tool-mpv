package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ZarcZoneamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataZarcZoneamentoRepository extends JpaRepository<ZarcZoneamentoEntity, Long> {

    List<ZarcZoneamentoEntity> findAllByMunicipioIdAndSafraIniAndSafraFimAndTemJanelaFavoravelTrue(
            Long municipioId,
            Integer safraIni,
            Integer safraFim
    );

    Optional<ZarcZoneamentoEntity> findFirstByMunicipioIdAndTemJanelaFavoravelTrueOrderBySafraFimDescSafraIniDesc(
            Long municipioId
    );
}
