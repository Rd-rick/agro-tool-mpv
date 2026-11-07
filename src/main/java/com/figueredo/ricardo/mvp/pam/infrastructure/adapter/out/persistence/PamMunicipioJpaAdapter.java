package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.pam.application.port.out.PamMunicipioPort;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.entity.PamMunicipioEntity;
import com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.repository.SpringDataPamMunicipioRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PamMunicipioJpaAdapter implements PamMunicipioPort {

    private final SpringDataPamMunicipioRepository repository;

    public PamMunicipioJpaAdapter(SpringDataPamMunicipioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> mapearIbgeParaIdPorUf(String uf) {
        return repository.findAllByUf(uf).stream()
                .collect(Collectors.toMap(
                        PamMunicipioEntity::getCodigoIbge,
                        PamMunicipioEntity::getId,
                        (existing, replacement) -> existing
                ));
    }
}
