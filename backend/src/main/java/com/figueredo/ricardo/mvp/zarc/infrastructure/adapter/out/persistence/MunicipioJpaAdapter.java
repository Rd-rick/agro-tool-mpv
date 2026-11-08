package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity.MunicipioEntity;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.mapper.MunicipioPersistenceMapper;
import com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.repository.SpringDataMunicipioRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class MunicipioJpaAdapter implements MunicipioPort {

    private final SpringDataMunicipioRepository repository;
    private final MunicipioPersistenceMapper mapper;

    public MunicipioJpaAdapter(SpringDataMunicipioRepository repository, MunicipioPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
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

    @Override
    @Transactional(readOnly = true)
    public List<Municipio> listarTodosOrdenadosPorNome() {
        List<MunicipioEntity> entities = repository.findAllByOrderByNomeAsc();
        return mapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Municipio> buscarPorCodigoIbge(String codigoIbge) {
        return repository.findByCodigoIbge(codigoIbge)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Municipio> buscarPorNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return Optional.empty();
        }
        return repository.findByNomeIgnoreCase(nome.trim())
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> buscarIdPorCodigoIbge(String codigoIbge) {
        return repository.findByCodigoIbge(codigoIbge)
                .map(MunicipioEntity::getId);
    }
}
