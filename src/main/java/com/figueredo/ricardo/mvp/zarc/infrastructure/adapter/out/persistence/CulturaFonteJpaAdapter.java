package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.zarc.application.port.out.CulturaFontePort;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.CulturaCodigoFonteEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository.SpringDataCulturaCodigoFonteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CulturaFonteJpaAdapter implements CulturaFontePort {

    private final SpringDataCulturaCodigoFonteRepository repository;

    public CulturaFonteJpaAdapter(SpringDataCulturaCodigoFonteRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> mapearCodigoOrigemParaIdPorFonte(String fonte) {
        return repository.findAllByFonteAndAtivoTrue(fonte).stream()
                .collect(Collectors.toMap(
                        CulturaCodigoFonteEntity::getCodigoOrigem,
                        CulturaCodigoFonteEntity::getId,
                        (existing, replacement) -> existing
                ));
    }
}
