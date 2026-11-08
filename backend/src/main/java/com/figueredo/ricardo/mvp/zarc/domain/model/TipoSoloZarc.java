package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.Optional;

public enum TipoSoloZarc {

    SOLO_ARENOSO("1", "Solo arenoso", "Textura arenosa", "10% a menos de 15%", null),
    SOLO_TEXTURA_MEDIA("2", "Solo de textura média", "Textura média", "15% a menos de 35%", null),
    SOLO_ARGILOSO("3", "Solo argiloso", "Textura argilosa", "35% ou mais", null),

    SOLO_AD1("11", "Solo AD1", "AD1", null, "0,34 a 0,46 mm/cm"),
    SOLO_AD2("12", "Solo AD2", "AD2", null, "0,46 a 0,61 mm/cm"),
    SOLO_AD3("13", "Solo AD3", "AD3", null, "0,61 a 0,80 mm/cm"),
    SOLO_AD4("14", "Solo AD4", "AD4", null, "0,80 a 1,06 mm/cm"),
    SOLO_AD5("15", "Solo AD5", "AD5", null, "1,06 a 1,40 mm/cm"),
    SOLO_AD6("16", "Solo AD6", "AD6", null, "≥ 1,40 mm/cm");

    private final String codigo;
    private final String tipoSolo;
    private final String classificacao;
    private final String teorArgila;
    private final String aguaDisponivel;

    TipoSoloZarc(String codigo, String tipoSolo, String classificacao, String teorArgila, String aguaDisponivel) {
        this.codigo = codigo;
        this.tipoSolo = tipoSolo;
        this.classificacao = classificacao;
        this.teorArgila = teorArgila;
        this.aguaDisponivel = aguaDisponivel;
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

    public String getAguaDisponivel() {
        return aguaDisponivel;
    }

    public String getDescricaoCompleta() {
        if (aguaDisponivel != null && !aguaDisponivel.isBlank()) {
            return String.format("%s - %s (%s)", tipoSolo, classificacao, aguaDisponivel);
        }
        return String.format("%s - %s (%s)", tipoSolo, classificacao, teorArgila);
    }

    public SoloZoneamento toSoloZoneamento() {
        return new SoloZoneamento(codigo, getDescricaoCompleta(), tipoSolo, classificacao, teorArgila, aguaDisponivel);
    }

    public static Optional<TipoSoloZarc> buscarPorCodigoOuNome(String valor) {
        if (valor == null || valor.isBlank()) {
            return Optional.empty();
        }
        String normalizado = valor.trim().replace(" ", "").toUpperCase();
        for (TipoSoloZarc item : values()) {
            if (item.codigo.equalsIgnoreCase(valor.trim())
                    || item.name().equalsIgnoreCase(valor.trim())
                    || item.tipoSolo.equalsIgnoreCase(valor.trim())
                    || item.classificacao.equalsIgnoreCase(valor.trim())
                    || ("Tipo " + item.codigo).equalsIgnoreCase(valor.trim())
                    || ("Classe " + item.classificacao).equalsIgnoreCase(valor.trim())
                    || item.name().replace("_", "").equalsIgnoreCase(normalizado)
                    || item.classificacao.replace(" ", "").equalsIgnoreCase(normalizado)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    public static TipoSoloZarc deCodigoOuNome(String valor) {
        return buscarPorCodigoOuNome(valor)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tipo de solo inválido: '" + valor + "'. Informe 1 a 3 (textura do solo) ou 11 a 16 (classes de água disponível AD1 a AD6 - IN MAPA 1/2022)"
                ));
    }
}
