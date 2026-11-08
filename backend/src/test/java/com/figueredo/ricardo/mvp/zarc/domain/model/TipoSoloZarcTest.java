package com.figueredo.ricardo.mvp.zarc.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TipoSoloZarcTest {

    @Test
    void deveConterTodasAsClassesDeAguaDisponivelConformeNormativa() {
        TipoSoloZarc ad1 = TipoSoloZarc.SOLO_AD1;
        assertEquals("11", ad1.getCodigo());
        assertEquals("Solo AD1", ad1.getTipoSolo());
        assertEquals("AD1", ad1.getClassificacao());
        assertEquals("0,34 a 0,46 mm/cm", ad1.getAguaDisponivel());
        assertNull(ad1.getTeorArgila());
        assertEquals("Solo AD1 - AD1 (0,34 a 0,46 mm/cm)", ad1.getDescricaoCompleta());

        TipoSoloZarc ad2 = TipoSoloZarc.SOLO_AD2;
        assertEquals("12", ad2.getCodigo());
        assertEquals("Solo AD2", ad2.getTipoSolo());
        assertEquals("AD2", ad2.getClassificacao());
        assertEquals("0,46 a 0,61 mm/cm", ad2.getAguaDisponivel());

        TipoSoloZarc ad3 = TipoSoloZarc.SOLO_AD3;
        assertEquals("13", ad3.getCodigo());
        assertEquals("Solo AD3", ad3.getTipoSolo());
        assertEquals("AD3", ad3.getClassificacao());
        assertEquals("0,61 a 0,80 mm/cm", ad3.getAguaDisponivel());

        TipoSoloZarc ad4 = TipoSoloZarc.SOLO_AD4;
        assertEquals("14", ad4.getCodigo());
        assertEquals("Solo AD4", ad4.getTipoSolo());
        assertEquals("AD4", ad4.getClassificacao());
        assertEquals("0,80 a 1,06 mm/cm", ad4.getAguaDisponivel());

        TipoSoloZarc ad5 = TipoSoloZarc.SOLO_AD5;
        assertEquals("15", ad5.getCodigo());
        assertEquals("Solo AD5", ad5.getTipoSolo());
        assertEquals("AD5", ad5.getClassificacao());
        assertEquals("1,06 a 1,40 mm/cm", ad5.getAguaDisponivel());

        TipoSoloZarc ad6 = TipoSoloZarc.SOLO_AD6;
        assertEquals("16", ad6.getCodigo());
        assertEquals("Solo AD6", ad6.getTipoSolo());
        assertEquals("AD6", ad6.getClassificacao());
        assertEquals("≥ 1,40 mm/cm", ad6.getAguaDisponivel());
    }

    @Test
    void devePreservarSolosLegadosPorTextura() {
        TipoSoloZarc s1 = TipoSoloZarc.SOLO_ARENOSO;
        assertEquals("1", s1.getCodigo());
        assertEquals("Solo arenoso", s1.getTipoSolo());
        assertEquals("Textura arenosa", s1.getClassificacao());
        assertEquals("10% a menos de 15%", s1.getTeorArgila());
        assertNull(s1.getAguaDisponivel());
        assertEquals("Solo arenoso - Textura arenosa (10% a menos de 15%)", s1.getDescricaoCompleta());

        TipoSoloZarc s2 = TipoSoloZarc.SOLO_TEXTURA_MEDIA;
        assertEquals("2", s2.getCodigo());
        assertEquals("15% a menos de 35%", s2.getTeorArgila());

        TipoSoloZarc s3 = TipoSoloZarc.SOLO_ARGILOSO;
        assertEquals("3", s3.getCodigo());
        assertEquals("35% ou mais", s3.getTeorArgila());
    }

    @ParameterizedTest
    @CsvSource({
            "11, SOLO_AD1",
            "AD1, SOLO_AD1",
            "ad1, SOLO_AD1",
            "AD 1, SOLO_AD1",
            "Solo AD1, SOLO_AD1",
            "Classe AD1, SOLO_AD1",
            "Tipo 11, SOLO_AD1",
            "12, SOLO_AD2",
            "AD2, SOLO_AD2",
            "13, SOLO_AD3",
            "AD3, SOLO_AD3",
            "14, SOLO_AD4",
            "AD4, SOLO_AD4",
            "15, SOLO_AD5",
            "AD5, SOLO_AD5",
            "16, SOLO_AD6",
            "AD6, SOLO_AD6",
            "1, SOLO_ARENOSO",
            "Solo arenoso, SOLO_ARENOSO",
            "2, SOLO_TEXTURA_MEDIA",
            "3, SOLO_ARGILOSO"
    })
    void deveBuscarPorCodigoOuNomeComSucesso(String entrada, TipoSoloZarc esperado) {
        Optional<TipoSoloZarc> resultado = TipoSoloZarc.buscarPorCodigoOuNome(entrada);
        assertTrue(resultado.isPresent());
        assertEquals(esperado, resultado.get());
    }

    @Test
    void deveLancarExcecaoParaSoloInvalidoEmDeCodigoOuNome() {
        assertThrows(IllegalArgumentException.class, () -> TipoSoloZarc.deCodigoOuNome("99"));
        assertThrows(IllegalArgumentException.class, () -> TipoSoloZarc.deCodigoOuNome(""));
        assertThrows(IllegalArgumentException.class, () -> TipoSoloZarc.deCodigoOuNome(null));
    }

    @Test
    void deveConverterParaSoloZoneamentoComAguaDisponivel() {
        SoloZoneamento soloZoneamento = TipoSoloZarc.SOLO_AD3.toSoloZoneamento();
        assertEquals("13", soloZoneamento.codigo());
        assertEquals("Solo AD3 - AD3 (0,61 a 0,80 mm/cm)", soloZoneamento.descricao());
        assertEquals("Solo AD3", soloZoneamento.tipoSolo());
        assertEquals("AD3", soloZoneamento.classificacao());
        assertEquals("0,61 a 0,80 mm/cm", soloZoneamento.aguaDisponivel());
        assertNull(soloZoneamento.teorArgila());
    }

    @Test
    void deveCriarSoloZoneamentoAPartirDeCodigoAD() {
        SoloZoneamento solo = new SoloZoneamento("14", null);
        assertEquals("14", solo.codigo());
        assertEquals("AD4", solo.classificacao());
        assertEquals("0,80 a 1,06 mm/cm", solo.aguaDisponivel());
        assertEquals("Solo AD4 - AD4 (0,80 a 1,06 mm/cm)", solo.descricao());
    }

    @Test
    void deveFormatarRecomendacaoDeSoloResolvendoCodigoNumericoParaNomeAmigavel() {
        PeriodoDecendio decendioNovembro = PeriodoDecendio.deDecendio(31, java.util.Set.of(20));
        assertEquals("01 a 10 de Novembro", decendioNovembro.descricaoPeriodo());

        String recomendacao = decendioNovembro.formatarRecomendacaoSolo("12");
        assertEquals("De 01 a 10 de Novembro (risco de 20%) para Solo AD2", recomendacao);

        String recomendacaoNome = decendioNovembro.formatarRecomendacaoSolo("Solo AD2");
        assertEquals("De 01 a 10 de Novembro (risco de 20%) para Solo AD2", recomendacaoNome);

        String recomendacaoLegado = decendioNovembro.formatarRecomendacaoSolo("2");
        assertEquals("De 01 a 10 de Novembro (risco de 20%) para Solo de textura média", recomendacaoLegado);

        String recomendacaoNulo = decendioNovembro.formatarRecomendacaoSolo(null);
        assertEquals("De 01 a 10 de Novembro (risco de 20%) para o solo selecionado", recomendacaoNulo);
    }
}
