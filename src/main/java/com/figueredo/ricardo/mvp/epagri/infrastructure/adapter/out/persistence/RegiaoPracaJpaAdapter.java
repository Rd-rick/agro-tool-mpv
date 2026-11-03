package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.epagri.application.port.out.RegiaoPracaPort;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity.RegiaoPracaEntity;
import com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.repository.SpringDataRegiaoPracaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RegiaoPracaJpaAdapter implements RegiaoPracaPort {

    private final SpringDataRegiaoPracaRepository repository;

    public RegiaoPracaJpaAdapter(SpringDataRegiaoPracaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> mapearNomeParaIdPorUf(String uf) {
        return repository.findAllByUfAndAtivoTrue(uf).stream()
                .collect(Collectors.toMap(
                        RegiaoPracaEntity::getNome,
                        RegiaoPracaEntity::getId,
                        (existente, substituto) -> existente
                ));
    }
}
