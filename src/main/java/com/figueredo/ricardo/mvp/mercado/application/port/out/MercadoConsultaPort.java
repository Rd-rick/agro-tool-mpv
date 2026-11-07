package com.figueredo.ricardo.mvp.mercado.application.port.out;

import com.figueredo.ricardo.mvp.mercado.domain.model.CotacaoPrecoItem;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.MunicipioMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoCotacaoMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.RegiaoPracaMercado;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MercadoConsultaPort {

    Optional<MunicipioMercado> buscarMunicipioPorCodigoIbge(String codigoIbge);

    List<CulturaMercado> buscarCulturasAtivasPorIds(Collection<Long> culturaIds);

    List<ProdutoCotacaoMercado> buscarProdutosAtivosPorCulturasEFonte(Collection<Long> culturaIds, String fonte);

    default List<ProdutoCotacaoMercado> buscarProdutosAtivosConabPorCulturas(Collection<Long> culturaIds) {
        return List.of();
    }

    List<RegiaoPracaMercado> buscarPracasAtivasPorUf(String uf);

    Optional<LocalDate> buscarUltimaDataCotacao(Long produtoId, Long pracaId, String frequencia);

    List<CotacaoPrecoItem> buscarSeriePorPracaEPeriodo(
            Long produtoId,
            Long pracaId,
            String frequencia,
            LocalDate dataInicio,
            LocalDate dataFim
    );

    Optional<LocalDate> buscarUltimaDataCotacaoEmPracas(
            Long produtoId,
            Collection<Long> pracaIds,
            String frequencia
    );

    List<CotacaoPrecoItem> buscarSerieEmPracasEPeriodo(
            Long produtoId,
            Collection<Long> pracaIds,
            String frequencia,
            LocalDate dataInicio,
            LocalDate dataFim
    );
}
