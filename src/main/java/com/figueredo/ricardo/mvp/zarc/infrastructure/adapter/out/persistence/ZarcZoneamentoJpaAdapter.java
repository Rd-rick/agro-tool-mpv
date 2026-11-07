package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcZoneamentoQueryPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.SafraVigencia;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaCodigoFonteEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ZarcZoneamentoEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.mapper.ZarcZoneamentoPersistenceMapper;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository.SpringDataCulturaCodigoFonteRepository;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository.SpringDataCulturaRepository;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository.SpringDataZarcZoneamentoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ZarcZoneamentoJpaAdapter implements ZarcZoneamentoQueryPort {

    private final SpringDataZarcZoneamentoRepository zarcRepository;
    private final SpringDataCulturaCodigoFonteRepository culturaFonteRepository;
    private final SpringDataCulturaRepository culturaRepository;
    private final ZarcZoneamentoPersistenceMapper mapper;

    public ZarcZoneamentoJpaAdapter(
            SpringDataZarcZoneamentoRepository zarcRepository,
            SpringDataCulturaCodigoFonteRepository culturaFonteRepository,
            SpringDataCulturaRepository culturaRepository,
            ZarcZoneamentoPersistenceMapper mapper
    ) {
        this.zarcRepository = zarcRepository;
        this.culturaFonteRepository = culturaFonteRepository;
        this.culturaRepository = culturaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ZarcZoneamentoItem> buscarAptosPorMunicipioESafra(
            Long municipioId,
            Integer safraIni,
            Integer safraFim
    ) {
        List<ZarcZoneamentoEntity> entities = zarcRepository
                .findAllByMunicipioIdAndSafraIniAndSafraFimAndTemJanelaFavoravelTrue(municipioId, safraIni, safraFim);

        if (entities.isEmpty()) {
            return List.of();
        }

        Set<Long> culturaFonteIds = entities.stream()
                .map(ZarcZoneamentoEntity::getCulturaCodigoFonteId)
                .collect(Collectors.toSet());

        Map<Long, CulturaCodigoFonteEntity> mapaCulturaFonte = culturaFonteRepository
                .findAllByIdInAndAtivoTrue(culturaFonteIds)
                .stream()
                .collect(Collectors.toMap(
                        CulturaCodigoFonteEntity::getId,
                        Function.identity(),
                        (existente, substituto) -> existente
                ));

        if (mapaCulturaFonte.isEmpty()) {
            return List.of();
        }

        Set<Long> culturaIds = mapaCulturaFonte.values().stream()
                .map(CulturaCodigoFonteEntity::getCulturaId)
                .collect(Collectors.toSet());

        Map<Long, CulturaEntity> mapaCultura = culturaRepository
                .findAllByIdInAndAtivoTrue(culturaIds)
                .stream()
                .collect(Collectors.toMap(
                        CulturaEntity::getId,
                        Function.identity(),
                        (existente, substituto) -> existente
                ));

        if (mapaCultura.isEmpty()) {
            return List.of();
        }

        List<ZarcZoneamentoItem> itens = new ArrayList<>();
        for (ZarcZoneamentoEntity entity : entities) {
            CulturaCodigoFonteEntity fonteEntity = mapaCulturaFonte.get(entity.getCulturaCodigoFonteId());
            if (fonteEntity == null) {
                continue;
            }
            CulturaEntity culturaEntity = mapaCultura.get(fonteEntity.getCulturaId());
            if (culturaEntity == null) {
                continue;
            }
            itens.add(mapper.toDomainItem(entity, fonteEntity, culturaEntity));
        }

        return itens;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SafraVigencia> buscarSafraMaisRecentePorMunicipio(Long municipioId) {
        return zarcRepository
                .findFirstByMunicipioIdAndTemJanelaFavoravelTrueOrderBySafraFimDescSafraIniDesc(municipioId)
                .map(mapper::toSafraVigencia);
    }
}
