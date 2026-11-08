package com.figueredo.ricardo.mvp.clima.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CondicaoTempoTest {

    @Test
    void deveClassificarComoChovendoQuandoContemTermosDeChuva() {
        assertThat(CondicaoTempo.fromResumo("Pancadas de chuva")).isEqualTo(CondicaoTempo.CHOVENDO);
        assertThat(CondicaoTempo.fromResumo("Chuva isolada")).isEqualTo(CondicaoTempo.CHOVENDO);
        assertThat(CondicaoTempo.fromResumo("Trovoada")).isEqualTo(CondicaoTempo.CHOVENDO);
        assertThat(CondicaoTempo.fromResumo("Chuvisco")).isEqualTo(CondicaoTempo.CHOVENDO);
    }

    @Test
    void deveClassificarComoNubladoQuandoContemTermosDeNuvens() {
        assertThat(CondicaoTempo.fromResumo("Nublado")).isEqualTo(CondicaoTempo.NUBLADO);
        assertThat(CondicaoTempo.fromResumo("Muitas nuvens")).isEqualTo(CondicaoTempo.NUBLADO);
        assertThat(CondicaoTempo.fromResumo("Encoberto")).isEqualTo(CondicaoTempo.NUBLADO);
        assertThat(CondicaoTempo.fromResumo("Nevoeiro")).isEqualTo(CondicaoTempo.NUBLADO);
    }

    @Test
    void deveClassificarComoEnsolaradoPadraoOuCeuClaro() {
        assertThat(CondicaoTempo.fromResumo("Poucas nuvens")).isEqualTo(CondicaoTempo.ENSOLARADO);
        assertThat(CondicaoTempo.fromResumo("Céu claro")).isEqualTo(CondicaoTempo.ENSOLARADO);
        assertThat(CondicaoTempo.fromResumo("Ensolarado")).isEqualTo(CondicaoTempo.ENSOLARADO);
        assertThat(CondicaoTempo.fromResumo(null)).isEqualTo(CondicaoTempo.ENSOLARADO);
        assertThat(CondicaoTempo.fromResumo("")).isEqualTo(CondicaoTempo.ENSOLARADO);
    }
}
