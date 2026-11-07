package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.Optional;

public enum TipoSoloZarc {

    SOLO_ARENOSO("1", "Solo arenoso", "Textura arenosa", "10% a menos de 15%"),
    SOLO_TEXTURA_MEDIA("2", "Solo de textura média", "Textura média", "15% a menos de 35%"),
    SOLO_ARGILOSO("3", "Solo argiloso", "Textura argilosa", "35% ou mais");

    private final String codigo;
    private final String tipoSolo;
    private final String classificacao;
    private final String teorArgila;

    TipoSoloZarc(String codigo, String tipoSolo, String classificacao, String teorArgila) {
        this.codigo = codigo;
        this.tipoSolo = tipoSolo;
        this.classificacao = classificacao;
        this.teorArgila = teorArgila;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTipoSolo() {
        return tipoSolo;
    }

    public String getClassificacao() {
        return classificacao;
    }

    public String getTeorArgila() {
        return teorArgila;
    }

    public String getDescricaoCompleta() {
        return String.format("%s - %s (%s)", tipoSolo, classificacao, teorArgila);
    }

    public SoloZoneamento toSoloZoneamento() {
        return new SoloZoneamento(codigo, getDescricaoCompleta(), tipoSolo, classificacao, teorArgila);
    }

    public static Optional<TipoSoloZarc> buscarPorCodigoOuNome(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        String normalizado = valor.trim();
        for (TipoSoloZarc item : values()) {
            if (item.codigo.equalsIgnoreCase(normalizado)
                    || item.name().equalsIgnoreCase(normalizado)
                    || item.tipoSolo.equalsIgnoreCase(normalizado)
                    || ("Tipo " + item.codigo).equalsIgnoreCase(normalizado)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public static TipoSoloZarc deCodigoOuNome(String valor) {
        return buscarPorCodigoOuNome(valor)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de solo inválido: '" + valor + "'. Informe 1 (Solo arenoso), 2 (Solo de textura média) ou 3 (Solo argiloso)"
                ));
    }
}
