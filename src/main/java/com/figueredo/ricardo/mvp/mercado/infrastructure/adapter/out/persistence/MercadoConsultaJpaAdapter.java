package com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.mercado.application.port.out.MercadoConsultaPort;
import com.figueredo.ricardo.mvp.mercado.domain.model.CotacaoPrecoItem;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.MunicipioMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoCotacaoMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.RegiaoPracaMercado;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity.MercadoCotacaoPrecoEntity;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.mapper.MercadoPersistenceMapper;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository.SpringDataMercadoCotacaoPrecoRepository;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository.SpringDataMercadoCulturaRepository;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository.SpringDataMercadoMunicipioRepository;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository.SpringDataMercadoProdutoCotacaoRepository;
import com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.repository.SpringDataMercadoRegiaoPracaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class MercadoConsultaJpaAdapter implements MercadoConsultaPort {

    private final SpringDataMercadoMunicipioRepository municipioRepository;
    private final SpringDataMercadoCulturaRepository culturaRepository;
    private final SpringDataMercadoProdutoCotacaoRepository produtoRepository;
    private final SpringDataMercadoRegiaoPracaRepository pracaRepository;
    private final SpringDataMercadoCotacaoPrecoRepository cotacaoRepository;
    private final MercadoPersistenceMapper mapper;

    public MercadoConsultaJpaAdapter(
            SpringDataMercadoMunicipioRepository municipioRepository,
            SpringDataMercadoCulturaRepository culturaRepository,
            SpringDataMercadoProdutoCotacaoRepository produtoRepository,
            SpringDataMercadoRegiaoPracaRepository pracaRepository,
            SpringDataMercadoCotacaoPrecoRepository cotacaoRepository,
            MercadoPersistenceMapper mapper
    ) {
        this.municipioRepository = municipioRepository;
        this.culturaRepository = culturaRepository;
        this.produtoRepository = produtoRepository;
        this.pracaRepository = pracaRepository;
        this.cotacaoRepository = cotacaoRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MunicipioMercado> buscarMunicipioPorCodigoIbge(String codigoIbge) {
        return municipioRepository.findByCodigoIbge(codigoIbge)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CulturaMercado> buscarCulturasAtivasPorIds(Collection<Long> culturaIds) {
        if (culturaIds == null || culturaIds.isEmpty()) {
            return List.of();
        }
        return culturaRepository.findAllByIdInAndAtivoTrue(culturaIds).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProdutoCotacaoMercado> buscarProdutosAtivosPorCulturasEFonte(
            Collection<Long> culturaIds,
            String fonte
    ) {
        if (culturaIds == null || culturaIds.isEmpty()) {
            return List.of();
        }
        return produtoRepository.findAllByCulturaIdInAndFonteAndAtivoTrueOrderByIdAsc(culturaIds, fonte).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegiaoPracaMercado> buscarPracasAtivasPorUf(String uf) {
        return pracaRepository.findAllByUfAndAtivoTrueOrderByNomeAsc(uf).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocalDate> buscarUltimaDataCotacao(Long produtoId, Long pracaId, String frequencia) {
        return cotacaoRepository.findFirstByProdutoIdAndPracaIdAndFrequenciaOrderByDataCotacaoDesc(
                        produtoId,
                        pracaId,
                        frequencia
                )
                .map(MercadoCotacaoPrecoEntity::getDataCotacao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CotacaoPrecoItem> buscarSeriePorPracaEPeriodo(
            Long produtoId,
            Long pracaId,
            String frequencia,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        return cotacaoRepository.findAllByProdutoIdAndPracaIdAndFrequenciaAndDataCotacaoBetweenOrderByDataCotacaoAsc(
                        produtoId,
                        pracaId,
                        frequencia,
                        dataInicio,
                        dataFim
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LocalDate> buscarUltimaDataCotacaoEmPracas(
            Long produtoId,
            Collection<Long> pracaIds,
            String frequencia
    ) {
        if (pracaIds == null || pracaIds.isEmpty()) {
            return Optional.empty();
        }
        return cotacaoRepository.findFirstByProdutoIdAndPracaIdInAndFrequenciaOrderByDataCotacaoDesc(
                        produtoId,
                        pracaIds,
                        frequencia
                )
                .map(MercadoCotacaoPrecoEntity::getDataCotacao);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CotacaoPrecoItem> buscarSerieEmPracasEPeriodo(
            Long produtoId,
            Collection<Long> pracaIds,
            String frequencia,
            LocalDate dataInicio,
            LocalDate dataFim
    ) {
        if (pracaIds == null || pracaIds.isEmpty()) {
            return List.of();
        }
        return cotacaoRepository.findAllByProdutoIdAndPracaIdInAndFrequenciaAndDataCotacaoBetweenOrderByDataCotacaoAsc(
                        produtoId,
                        pracaIds,
                        frequencia,
                        dataInicio,
                        dataFim
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
