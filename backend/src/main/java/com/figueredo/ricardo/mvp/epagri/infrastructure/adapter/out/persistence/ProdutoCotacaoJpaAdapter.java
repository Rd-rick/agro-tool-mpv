package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.epagri.application.port.out.ProdutoCotacaoPort;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.ProdutoCotacaoEntity;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.repository.SpringDataProdutoCotacaoRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProdutoCotacaoJpaAdapter implements ProdutoCotacaoPort {

    private final SpringDataProdutoCotacaoRepository repository;

    public ProdutoCotacaoJpaAdapter(SpringDataProdutoCotacaoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> mapearNomeBrutoParaIdPorFonte(String fonte) {
        return repository.findAllByFonteAndAtivoTrue(fonte).stream()
                .collect(Collectors.toMap(
                        ProdutoCotacaoEntity::getNomeBrutoOrigem,
                        ProdutoCotacaoEntity::getId,
                        (existente, substituto) -> existente
                ));
    }
}
