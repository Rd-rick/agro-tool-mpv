package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.zarc.domain.model.SafraVigencia;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcZoneamentoItem;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaCodigoFonteEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ZarcZoneamentoEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ZarcZoneamentoPersistenceMapperTest {

    private final ZarcZoneamentoPersistenceMapper mapper = new ZarcZoneamentoPersistenceMapper();

    @Test
    void deveMapearEntidadeParaDominioExtraindo36Decendios() {
        ZarcZoneamentoEntity entity = new ZarcZoneamentoEntity();
        entity.setSafraIni(2025);
        entity.setSafraFim(2026);
        entity.setCicloCodigo("1");
        entity.setCicloDescricao("Grupo I");
        entity.setSoloCodigo("2");
        entity.setSoloDescricao("Tipo 2");
        entity.setManejoCodigo("1");
        entity.setManejoDescricao("Sequeiro");
        entity.setPortaria("Portaria 123");
        entity.setRiscoMinimo((short) 20);
        entity.setDec_01((short) 20);
        entity.setDec_36((short) 40);

        CulturaCodigoFonteEntity fonte = new CulturaCodigoFonteEntity();
        fonte.setId(10L);
        fonte.setCulturaId(1L);
        fonte.setNomeOrigem("Soja");

        CulturaEntity cultura = new CulturaEntity();
        cultura.setId(1L);
        cultura.setNome("Soja");
        cultura.setNomeCientifico("Glycine max");
        cultura.setGrupoAgricola("GRAOS");

        ZarcZoneamentoItem item = mapper.toDomainItem(entity, fonte, cultura);

        assertEquals(1L, item.culturaId());
        assertEquals("Soja", item.nomeCultura());
        assertEquals("Glycine max", item.nomeCientifico());
        assertEquals(36, item.decendios().size());
        assertEquals(20, item.decendios().get(0));
        assertEquals(0, item.decendios().get(1));
        assertEquals(40, item.decendios().get(35));
        assertEquals(20, item.riscoMinimo());

        SafraVigencia safra = mapper.toSafraVigencia(entity);
        assertEquals(2025, safra.safraIni());
        assertEquals(2026, safra.safraFim());
    }
}
