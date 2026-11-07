package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.pam.application.port.out.PamCulturaFontePort;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.entity.PamCulturaCodigoFonteEntity;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.repository.SpringDataPamCulturaCodigoFonteRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PamCulturaFonteJpaAdapter implements PamCulturaFontePort {

    private final SpringDataPamCulturaCodigoFonteRepository repository;

    public PamCulturaFonteJpaAdapter(SpringDataPamCulturaCodigoFonteRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> mapearCodigoOrigemParaIdPorFonte(String fonte) {
        return repository.findAllByFonteAndAtivoTrue(fonte).stream()
                .collect(Collectors.toMap(
                        PamCulturaCodigoFonteEntity::getCodigoOrigem,
                        PamCulturaCodigoFonteEntity::getId,
                        (existing, replacement) -> existing
                ));
    }
}
