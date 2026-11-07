package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.zarc.domain.model.SafraVigencia;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaCodigoFonteEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ZarcZoneamentoEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class ZarcZoneamentoPersistenceMapper {

    public ZarcZoneamentoItem toDomainItem(
            ZarcZoneamentoEntity entity,
            CulturaCodigoFonteEntity culturaFonte,
            CulturaEntity cultura
    ) {
        List<Integer> decendios = extrairDecendios(entity);
        Integer riscoMinimo = entity.getRiscoMinimo() != null
                ? entity.getRiscoMinimo().intValue()
                : null;

        return new ZarcZoneamentoItem(
                cultura.getId(),
                cultura.getNome(),
                cultura.getNomeCientifico(),
                cultura.getGrupoAgricola(),
                culturaFonte.getNomeOrigem(),
                entity.getSafraIni(),
                entity.getSafraFim(),
                entity.getCicloCodigo(),
                entity.getCicloDescricao(),
                entity.getSoloCodigo(),
                entity.getSoloDescricao(),
                entity.getManejoCodigo(),
                entity.getManejoDescricao(),
                entity.getPortaria(),
                riscoMinimo,
                decendios
        );
    }

    public SafraVigencia toSafraVigencia(ZarcZoneamentoEntity entity) {
        return new SafraVigencia(entity.getSafraIni(), entity.getSafraFim());
    }

    private List<Integer> extrairDecendios(ZarcZoneamentoEntity e) {
        return Stream.of(
                        e.getDec_01(), e.getDec_02(), e.getDec_03(), e.getDec_04(), e.getDec_05(), e.getDec_06(),
                        e.getDec_07(), e.getDec_08(), e.getDec_09(), e.getDec_10(), e.getDec_11(), e.getDec_12(),
                        e.getDec_13(), e.getDec_14(), e.getDec_15(), e.getDec_16(), e.getDec_17(), e.getDec_18(),
                        e.getDec_19(), e.getDec_20(), e.getDec_21(), e.getDec_22(), e.getDec_23(), e.getDec_24(),
                        e.getDec_25(), e.getDec_26(), e.getDec_27(), e.getDec_28(), e.getDec_29(), e.getDec_30(),
                        e.getDec_31(), e.getDec_32(), e.getDec_33(), e.getDec_34(), e.getDec_35(), e.getDec_36()
                )
                .map(valor -> valor != null ? valor.intValue() : 0)
                .toList();
    }
}
