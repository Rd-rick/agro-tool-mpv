package com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository;

import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoCotacaoPrecoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SpringDataMercadoCotacaoPrecoRepository extends JpaRepository<MercadoCotacaoPrecoEntity, Long> {

    Optional<MercadoCotacaoPrecoEntity> findFirstByProdutoIdAndPracaIdAndFrequenciaOrderByDataCotacaoDesc(
            Long produtoId,
            Long pracaId,
            String frequencia
    );

    List<MercadoCotacaoPrecoEntity> findAllByProdutoIdAndPracaIdAndFrequenciaAndDataCotacaoBetweenOrderByDataCotacaoAsc(
            Long produtoId,
            Long pracaId,
            String frequencia,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    Optional<MercadoCotacaoPrecoEntity> findFirstByProdutoIdAndPracaIdInAndFrequenciaOrderByDataCotacaoDesc(
            Long produtoId,
            Collection<Long> pracaIds,
            String frequencia
    );

    List<MercadoCotacaoPrecoEntity> findAllByProdutoIdAndPracaIdInAndFrequenciaAndDataCotacaoBetweenOrderByDataCotacaoAsc(
            Long produtoId,
            Collection<Long> pracaIds,
            String frequencia,
            LocalDate dataInicio,
            LocalDate dataFim
    );
}
