package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoCulturaCodigoFonteEntity;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoMunicipioEntity;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity.DecisaoPamProducaoEntity;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.mapper.DecisaoPersistenceMapper;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository.SpringDataDecisaoCulturaCodigoFonteRepository;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository.SpringDataDecisaoMunicipioRepository;
import com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.repository.SpringDataDecisaoPamProducaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisaoHistoricoPamJpaAdapterTest {

    @Mock
    private SpringDataDecisaoMunicipioRepository municipioRepository;

    @Mock
    private SpringDataDecisaoCulturaCodigoFonteRepository culturaFonteRepository;

    @Mock
    private SpringDataDecisaoPamProducaoRepository pamProducaoRepository;

    private DecisaoHistoricoPamJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DecisaoHistoricoPamJpaAdapter(
                municipioRepository,
                culturaFonteRepository,
                pamProducaoRepository,
                new DecisaoPersistenceMapper()
        );
    }

    @Test
    void deveBuscarUltimoHistoricoPamPorMunicipioECulturasPriorizandoAnoMaisRecente() {
        DecisaoMunicipioEntity municipio = new DecisaoMunicipioEntity();
        municipio.setId(93L);
        municipio.setCodigoIbge("4204806");
        municipio.setNome("Curitibanos");
        municipio.setUf("SC");

        when(municipioRepository.findByCodigoIbge("4204806")).thenReturn(Optional.of(municipio));

        DecisaoCulturaCodigoFonteEntity cfSoja = new DecisaoCulturaCodigoFonteEntity();
        cfSoja.setId(100L);
        cfSoja.setCulturaId(1L);
        cfSoja.setFonte("IBGE_PAM");
        cfSoja.setAtivo(true);

        when(culturaFonteRepository.findAllByCulturaIdInAndFonteAndAtivoTrue(List.of(1L, 8L), "IBGE_PAM"))
                .thenReturn(List.of(cfSoja));

        DecisaoPamProducaoEntity pam2025 = new DecisaoPamProducaoEntity();
        pam2025.setId(10L);
        pam2025.setMunicipioId(93L);
        pam2025.setCulturaCodigoFonteId(100L);
        pam2025.setAno(2025);
        pam2025.setAreaPlantadaHa(18000);
        pam2025.setQuantidadeProduzidaTon(68400);
        pam2025.setRendimentoMedioKgHa(3800);
        pam2025.setValorProducaoMilReais(new BigDecimal("110000.00"));

        DecisaoPamProducaoEntity pam2024 = new DecisaoPamProducaoEntity();
        pam2024.setId(9L);
        pam2024.setMunicipioId(93L);
        pam2024.setCulturaCodigoFonteId(100L);
        pam2024.setAno(2024);
        pam2024.setAreaPlantadaHa(17000);
        pam2024.setQuantidadeProduzidaTon(61200);
        pam2024.setRendimentoMedioKgHa(3600);

        when(pamProducaoRepository.findAllByMunicipioIdAndCulturaCodigoFonteIdInOrderByAnoDesc(93L, Set.of(100L)))
                .thenReturn(List.of(pam2025, pam2024));

        Map<Long, HistoricoPamCultura> resultado = adapter.buscarUltimoHistoricoPamPorMunicipioECulturas(
                "4204806",
                List.of(1L, 8L)
        );

        assertEquals(2, resultado.size());

        HistoricoPamCultura sojaPam = resultado.get(1L);
        assertTrue(sojaPam.possuiHistorico());
        assertEquals(2025, sojaPam.ultimoAno());
        assertEquals(3800, sojaPam.rendimentoMedioKgHa());

        HistoricoPamCultura aveiaPam = resultado.get(8L);
        assertFalse(aveiaPam.possuiHistorico());
    }
}
