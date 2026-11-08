package com.figueredo.ricardo.mvp.clima.domain.model;

public enum CondicaoTempo {
    ENSOLARADO,
    NUBLADO,
    CHOVENDO;

    public static CondicaoTempo fromResumo(String resumo) {
        if (resumo == null || resumo.isBlank()) {
            return ENSOLARADO;
        }
        String r = resumo.toLowerCase();
        if (r.contains("chuva") || r.contains("pancada") || r.contains("trovoada") || r.contains("chuvisco")) {
            return CHOVENDO;
        }
        if (r.contains("nublado") || r.contains("encoberto") || r.contains("nevoeiro") || r.contains("muitas nuvens")) {
            return NUBLADO;
        }
        return ENSOLARADO;
    }
}
