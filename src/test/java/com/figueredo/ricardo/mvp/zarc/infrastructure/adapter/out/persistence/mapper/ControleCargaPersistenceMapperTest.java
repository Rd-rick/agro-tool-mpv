package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.zarc.domain.model.ControleCarga;
import com.figueredo.ricardo.mvp.zarc.domain.model.StatusCarga;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.ControleCargaEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControleCargaPersistenceMapperTest {

    private final ControleCargaPersistenceMapper mapper = new ControleCargaPersistenceMapper();

    @Test
    void deveMapearDominioParaEntityEViceVersa() {
        Instant inicio = Instant.now().minusSeconds(300);
        Instant fim = Instant.now();

        ControleCarga domain = new ControleCarga(
                10L,
                "MAPA_ZARC",
                "res-123",
                "2026-01-01",
                StatusCarga.SUCESSO,
                1500,
                null,
                inicio,
                fim
        );

        ControleCargaEntity entity = mapper.toEntity(domain);
        assertEquals(10L, entity.getId());
        assertEquals("MAPA_ZARC", entity.getFonte());
        assertEquals("res-123", entity.getIdentificadorRecurso());
        assertEquals("2026-01-01", entity.getUltimoModificadoFonte());
        assertEquals("SUCESSO", entity.getStatus());
        assertEquals(1500, entity.getTotalRegistrosProcessados());
        assertEquals(inicio, entity.getIniciadoEm());
        assertEquals(fim, entity.getFinalizadoEm());

        ControleCarga reconvertido = mapper.toDomain(entity);
        assertEquals(domain.getId(), reconvertido.getId());
        assertEquals(domain.getFonte(), reconvertido.getFonte());
        assertEquals(domain.getIdentificadorRecurso(), reconvertido.getIdentificadorRecurso());
        assertEquals(domain.getStatus(), reconvertido.getStatus());
        assertEquals(domain.getTotalRegistrosProcessados(), reconvertido.getTotalRegistrosProcessados());
    }
}
