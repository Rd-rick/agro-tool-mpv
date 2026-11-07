package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.decisao.application.port.out.DecisaoHistoricoPamPort;
import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoCulturaCodigoFonteEntity;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoMunicipioEntity;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoPamProducaoEntity;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.mapper.DecisaoPersistenceMapper;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository.SpringDataDecisaoCulturaCodigoFonteRepository;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository.SpringDataDecisaoMunicipioRepository;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository.SpringDataDecisaoPamProducaoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DecisaoHistoricoPamJpaAdapter implements DecisaoHistoricoPamPort {

    private static final String FONTE_PAM = "IBGE_PAM";

    private final SpringDataDecisaoMunicipioRepository municipioRepository;
    private final SpringDataDecisaoCulturaCodigoFonteRepository culturaFonteRepository;
    private final SpringDataDecisaoPamProducaoRepository pamProducaoRepository;
    private final DecisaoPersistenceMapper mapper;

    public DecisaoHistoricoPamJpaAdapter(
            SpringDataDecisaoMunicipioRepository municipioRepository,
            SpringDataDecisaoCulturaCodigoFonteRepository culturaFonteRepository,
            SpringDataDecisaoPamProducaoRepository pamProducaoRepository,
            DecisaoPersistenceMapper mapper
    ) {
        this.municipioRepository = municipioRepository;
        this.culturaFonteRepository = culturaFonteRepository;
        this.pamProducaoRepository = pamProducaoRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, HistoricoPamCultura> buscarUltimoHistoricoPamPorMunicipioECulturas(
            String codigoIbge,
            Collection<Long> culturaIds
    ) {
        if (codigoIbge == null || codigoIbge.isBlank() || culturaIds == null || culturaIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, HistoricoPamCultura> resultado = new LinkedHashMap<>();
        for (Long culturaId : culturaIds) {
            resultado.put(culturaId, HistoricoPamCultura.semHistorico(culturaId));
        }

        Optional<DecisaoMunicipioEntity> municipioOpt = municipioRepository.findByCodigoIbge(codigoIbge.trim());
        if (municipioOpt.isEmpty()) {
            return resultado;
        }

        List<DecisaoCulturaCodigoFonteEntity> codigosFonte = culturaFonteRepository
                .findAllByCulturaIdInAndFonteAndAtivoTrue(culturaIds, FONTE_PAM);

        if (codigosFonte.isEmpty()) {
            return resultado;
        }

        Map<Long, Long> culturaPorCodigoFonteId = new LinkedHashMap<>();
        for (DecisaoCulturaCodigoFonteEntity cf : codigosFonte) {
            culturaPorCodigoFonteId.put(cf.getId(), cf.getCulturaId());
        }

        List<DecisaoPamProducaoEntity> registrosPam = pamProducaoRepository
                .findAllByMunicipioIdAndCulturaCodigoFonteIdInOrderByAnoDesc(
                        municipioOpt.get().getId(),
                        culturaPorCodigoFonteId.keySet()
                );

        for (DecisaoPamProducaoEntity reg : registrosPam) {
            Long culturaId = culturaPorCodigoFonteId.get(reg.getCulturaCodigoFonteId());
            if (culturaId == null) {
                continue;
            }
            HistoricoPamCultura atual = resultado.get(culturaId);
            if (atual != null && atual.possuiHistorico()) {
                continue;
            }
            HistoricoPamCultura mapeado = mapper.toHistoricoPam(culturaId, reg);
            if (mapeado.possuiHistorico()) {
                resultado.put(culturaId, mapeado);
            }
        }

        return resultado;
    }
}
