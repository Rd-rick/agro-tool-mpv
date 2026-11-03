package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.ProdutoCotacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataProdutoCotacaoRepository extends JpaRepository<ProdutoCotacaoEntity, Long> {

    List<ProdutoCotacaoEntity> findAllByFonteAndAtivoTrue(String fonte);
}
