package com.figueredo.ricardo.mvp.assistente.domain.model;

import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public record ContextoProdutor(
        String codigoIbge,
        String nomeMunicipio,
        PeriodoPlantioMeses periodoPlantio,
        TipoSoloZarc tipoSolo,
        BigDecimal areaHectares
) {
    public ContextoProdutor(
            String codigoIbge,
            PeriodoPlantioMeses periodoPlantio,
            TipoSoloZarc tipoSolo,
            BigDecimal areaHectares
    ) {
        this(codigoIbge, null, periodoPlantio, tipoSolo, areaHectares);
    }

    public ContextoProdutor {
        if (codigoIbge != null && !codigoIbge.isBlank()) {
            codigoIbge = codigoIbge.trim();
            if (!codigoIbge.matches("^42\\d{5}$")) {
                throw new IllegalArgumentException("codigoIbge deve ser um código IBGE de SC com 7 dígitos");
            }
        } else {
            codigoIbge = null;
        }

        if (nomeMunicipio != null && !nomeMunicipio.isBlank()) {
            nomeMunicipio = nomeMunicipio.trim();
        } else {
            nomeMunicipio = null;
        }

        if (areaHectares != null && areaHectares.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("areaHectares deve ser maior que zero quando informada");
        }
    }

    public boolean possuiMunicipio() {
        return codigoIbge != null && !codigoIbge.isBlank();
    }

    public boolean possuiPeriodo() {
        return periodoPlantio != null;
    }

    public boolean possuiArea() {
        return areaHectares != null && areaHectares.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean possuiContextoMinimoCompleto() {
        return possuiMunicipio() && possuiPeriodo() && possuiArea();
    }

    public List<String> obterCamposFaltantesContextoMinimo() {
        List<String> faltantes = new ArrayList<>();
        if (!possuiMunicipio()) {
            faltantes.add("a cidade em Santa Catarina");
        }
        if (!possuiPeriodo()) {
            faltantes.add("o período de plantio (safra)");
        }
        if (!possuiArea()) {
            faltantes.add("a quantidade de hectares da sua área");
        }
        return faltantes;
    }

    public ContextoProdutor comMunicipio(String novoIbge, String novoNome) {
        return new ContextoProdutor(
                novoIbge,
                novoNome != null ? novoNome : this.nomeMunicipio,
                this.periodoPlantio,
                this.tipoSolo,
                this.areaHectares
        );
    }

    public ContextoProdutor comPeriodo(PeriodoPlantioMeses novoPeriodo) {
        return new ContextoProdutor(
                this.codigoIbge,
                this.nomeMunicipio,
                novoPeriodo,
                this.tipoSolo,
                this.areaHectares
        );
    }

    public ContextoProdutor comArea(BigDecimal novaArea) {
        return new ContextoProdutor(
                this.codigoIbge,
                this.nomeMunicipio,
                this.periodoPlantio,
                this.tipoSolo,
                novaArea
        );
    }

    public ContextoProdutor comTipoSolo(TipoSoloZarc novoTipoSolo) {
        return new ContextoProdutor(
                this.codigoIbge,
                this.nomeMunicipio,
                this.periodoPlantio,
                novoTipoSolo,
                this.areaHectares
        );
    }
}
