package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.MunicipioEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository.SpringDataMunicipioRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MunicipioJpaAdapter implements MunicipioPort {

    private final SpringDataMunicipioRepository repository;

    public MunicipioJpaAdapter(SpringDataMunicipioRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> mapearIbgeParaIdPorUf(String uf) {
        return repository.findAllByUf(uf).stream()
                .collect(Collectors.toMap(
                        MunicipioEntity::getCodigoIbge,
                        MunicipioEntity::getId,
                        (existing, replacement) -> existing
                ));
    }
}
