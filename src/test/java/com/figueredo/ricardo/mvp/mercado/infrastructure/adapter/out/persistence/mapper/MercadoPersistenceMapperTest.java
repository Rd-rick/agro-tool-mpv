package com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.mapper;

import com.figueredo.ricardo.mvp.mercado.domain.model.CotacaoPrecoItem;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.MunicipioMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoCotacaoMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.RegiaoPracaMercado;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoCotacaoPrecoEntity;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoCulturaEntity;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoMunicipioEntity;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoProdutoCotacaoEntity;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoRegiaoPracaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MercadoPersistenceMapperTest {

    private final MercadoPersistenceMapper mapper = new MercadoPersistenceMapper();

    @Test
    void deveMapearEntidadesDeMercadoParaModelosDeDominio() {
        MercadoMunicipioEntity munEntity = new MercadoMunicipioEntity();
        munEntity.setId(93L);
        munEntity.setCodigoIbge("4204806");
        munEntity.setNome("Curitibanos");
        munEntity.setUf("SC");
        munEntity.setPracaReferenciaId(7L);
        munEntity.setObservacaoPraca("Meio Oeste");

        MunicipioMercado mun = mapper.toDomain(munEntity);
        assertEquals(93L, mun.id());
        assertEquals("4204806", mun.codigoIbge());
        assertEquals(7L, mun.pracaReferenciaId());

        MercadoCulturaEntity cultEntity = new MercadoCulturaEntity();
        cultEntity.setId(1L);
        cultEntity.setNome("Soja");
        CulturaMercado cult = mapper.toDomain(cultEntity);
        assertEquals(1L, cult.id());
        assertEquals("Soja", cult.nome());

        MercadoProdutoCotacaoEntity prodEntity = new MercadoProdutoCotacaoEntity();
        prodEntity.setId(10L);
        prodEntity.setCulturaId(1L);
        prodEntity.setNomeComercial("Soja industrial");
        prodEntity.setEspecificacao("industrial");
        prodEntity.setUnidadeComercializacao("Saca");
        prodEntity.setPesoEmbalagemKg(new BigDecimal("60.00"));
        prodEntity.setFonte("EPAGRI_CEPA");
        ProdutoCotacaoMercado prod = mapper.toDomain(prodEntity);
        assertEquals(10L, prod.id());
        assertEquals("Soja industrial", prod.nomeComercial());

        MercadoRegiaoPracaEntity pracaEntity = new MercadoRegiaoPracaEntity();
        pracaEntity.setId(7L);
        pracaEntity.setNome("Meio Oeste");
        pracaEntity.setUf("SC");
        RegiaoPracaMercado praca = mapper.toDomain(pracaEntity);
        assertEquals(7L, praca.id());
        assertEquals("Meio Oeste", praca.nome());

        MercadoCotacaoPrecoEntity cotEntity = new MercadoCotacaoPrecoEntity();
        cotEntity.setProdutoId(10L);
        cotEntity.setPracaId(7L);
        cotEntity.setDataCotacao(LocalDate.of(2026, 10, 30));
        cotEntity.setPrecoMinimo(new BigDecimal("120.00"));
        cotEntity.setPrecoReferencia(new BigDecimal("125.00"));
        cotEntity.setPrecoMaximo(new BigDecimal("130.00"));
        cotEntity.setFrequencia("DIARIA");
        CotacaoPrecoItem cot = mapper.toDomain(cotEntity);
        assertEquals(new BigDecimal("125.00"), cot.precoReferencia());
        assertEquals("DIARIA", cot.frequencia());
    }
}
