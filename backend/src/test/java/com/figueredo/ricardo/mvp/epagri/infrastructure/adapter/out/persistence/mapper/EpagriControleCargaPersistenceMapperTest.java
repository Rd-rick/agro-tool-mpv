package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.epagri.domain.model.ControleCargaEpagri;
import com.figueredo.ricardo.mvp.epagri.domain.model.StatusCarga;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.ControleCargaEpagriEntity;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpagriControleCargaPersistenceMapperTest {

    private final EpagriControleCargaPersistenceMapper mapper = new EpagriControleCargaPersistenceMapper();

    @Test
    void deveMapearDominioParaEntityEViceVersa() {
        OffsetDateTime inicio = OffsetDateTime.now().minusMinutes(3);
        OffsetDateTime fim = OffsetDateTime.now();

        ControleCargaEpagri domain = new ControleCargaEpagri(
                5L,
                "EPAGRI_CEPA",
                "EPAGRI_DIARIA",
                "etag-abc",
                StatusCarga.SUCESSO,
                320,
                null,
                inicio,
                fim
        );

        ControleCargaEpagriEntity entity = mapper.toEntity(domain);
        assertEquals(5L, entity.getId());
        assertEquals("EPAGRI_CEPA", entity.getFonte());
        assertEquals("EPAGRI_DIARIA", entity.getIdentificadorRecurso());
        assertEquals("etag-abc", entity.getUltimoModificadoFonte());
        assertEquals("SUCESSO", entity.getStatus());
        assertEquals(320, entity.getTotalRegistrosProcessados());

        ControleCargaEpagri reconvertido = mapper.toDomain(entity);
        assertEquals(domain.getId(), reconvertido.getId());
        assertEquals(domain.getFonte(), reconvertido.getFonte());
        assertEquals(domain.getStatus(), reconvertido.getStatus());
        assertEquals(domain.getTotalRegistrosProcessados(), reconvertido.getTotalRegistrosProcessados());
    }
}
