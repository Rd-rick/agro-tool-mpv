package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoPamProducaoEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DecisaoPersistenceMapper {

    public HistoricoPamCultura toHistoricoPam(Long culturaId, DecisaoPamProducaoEntity entity) {
        if (entity == null) {
            return HistoricoPamCultura.semHistorico(culturaId);
        }

        boolean possuiAtividade = maiorQueZero(entity.getAreaPlantadaHa())
                || maiorQueZero(entity.getAreaColhidaHa())
                || maiorQueZero(entity.getQuantidadeProduzidaTon())
                || maiorQueZero(entity.getRendimentoMedioKgHa())
                || (entity.getValorProducaoMilReais() != null
                        && entity.getValorProducaoMilReais().compareTo(BigDecimal.ZERO) > 0);

        if (!possuiAtividade) {
            return HistoricoPamCultura.semHistorico(culturaId);
        }

        return new HistoricoPamCultura(
                culturaId,
                true,
                entity.getAno(),
                entity.getAreaPlantadaHa(),
                entity.getQuantidadeProduzidaTon(),
                entity.getRendimentoMedioKgHa()
        );
    }

    private boolean maiorQueZero(Integer valor) {
        return valor != null && valor > 0;
    }
}
