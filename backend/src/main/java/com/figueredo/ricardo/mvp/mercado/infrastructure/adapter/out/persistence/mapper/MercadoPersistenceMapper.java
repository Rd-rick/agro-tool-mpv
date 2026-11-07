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
import org.springframework.stereotype.Component;

@Component
public class MercadoPersistenceMapper {

    public MunicipioMercado toDomain(MercadoMunicipioEntity entity) {
        return new MunicipioMercado(
                entity.getId(),
                entity.getCodigoIbge(),
                entity.getNome(),
                entity.getUf(),
                entity.getPracaReferenciaId(),
                entity.getObservacaoPraca()
        );
    }

    public CulturaMercado toDomain(MercadoCulturaEntity entity) {
        return new CulturaMercado(entity.getId(), entity.getNome());
    }

    public ProdutoCotacaoMercado toDomain(MercadoProdutoCotacaoEntity entity) {
        return new ProdutoCotacaoMercado(
                entity.getId(),
                entity.getCulturaId(),
                entity.getNomeComercial(),
                entity.getEspecificacao(),
                entity.getUnidadeComercializacao(),
                entity.getPesoEmbalagemKg(),
                entity.getFonte()
        );
    }

    public RegiaoPracaMercado toDomain(MercadoRegiaoPracaEntity entity) {
        return new RegiaoPracaMercado(
                entity.getId(),
                entity.getNome(),
                entity.getUf()
        );
    }

    public CotacaoPrecoItem toDomain(MercadoCotacaoPrecoEntity entity) {
        return new CotacaoPrecoItem(
                entity.getProdutoId(),
                entity.getPracaId(),
                entity.getDataCotacao(),
                entity.getPrecoMinimo(),
                entity.getPrecoReferencia(),
                entity.getPrecoMaximo(),
                entity.getFrequencia()
        );
    }
}
