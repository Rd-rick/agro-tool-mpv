package com.figueredo.ricardo.mvp.clima.application.service;

import com.figueredo.ricardo.mvp.clima.application.port.in.ConsultarClimaAtualUseCase;
import com.figueredo.ricardo.mvp.clima.application.port.out.ClimaMunicipioPort;
import com.figueredo.ricardo.mvp.clima.domain.model.ClimaAtual;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConsultarClimaAtualService implements ConsultarClimaAtualUseCase {

    private static final Duration TTL_CACHE = Duration.ofMinutes(30);

    private final ClimaMunicipioPort climaMunicipioPort;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public ConsultarClimaAtualService(ClimaMunicipioPort climaMunicipioPort) {
        this.climaMunicipioPort = climaMunicipioPort;
    }

    @Override
    public ClimaAtual consultarClimaAtual(String codigoIbge) {
        if (codigoIbge == null || codigoIbge.isBlank()) {
            return ClimaAtual.indisponivel();
        }

        CacheEntry cached = cache.get(codigoIbge);
        if (cached != null && Duration.between(cached.criadoEm(), Instant.now()).compareTo(TTL_CACHE) < 0) {
            return cached.clima();
        }

        ClimaAtual clima = climaMunicipioPort.obterClimaDiaAtual(codigoIbge);
        if (clima != null && clima.isDisponivel()) {
            cache.put(codigoIbge, new CacheEntry(clima, Instant.now()));
            return clima;
        }

        return clima != null ? clima : ClimaAtual.indisponivel();
    }

    private record CacheEntry(ClimaAtual clima, Instant criadoEm) {}
}
