package com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoProdutoCotacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SpringDataMercadoProdutoCotacaoRepository extends JpaRepository<MercadoProdutoCotacaoEntity, Long> {

    List<MercadoProdutoCotacaoEntity> findAllByCulturaIdInAndFonteAndAtivoTrueOrderByIdAsc(
            Collection<Long> culturaIds,
            String fonte
    );
}
