package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_zarc_zoneamento")
@Getter
@Setter
@NoArgsConstructor
public class ZarcZoneamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long municipioId;

    private Long culturaCodigoFonteId;

    private Integer safraIni;

    private Integer safraFim;

    private String cicloCodigo;

    private String cicloDescricao;

    private String soloCodigo;

    private String soloDescricao;

    private String manejoCodigo;

    private String manejoDescricao;

    private String portaria;

    private Short dec_01;
    private Short dec_02;
    private Short dec_03;
    private Short dec_04;
    private Short dec_05;
    private Short dec_06;
    private Short dec_07;
    private Short dec_08;
    private Short dec_09;
    private Short dec_10;
    private Short dec_11;
    private Short dec_12;
    private Short dec_13;
    private Short dec_14;
    private Short dec_15;
    private Short dec_16;
    private Short dec_17;
    private Short dec_18;
    private Short dec_19;
    private Short dec_20;
    private Short dec_21;
    private Short dec_22;
    private Short dec_23;
    private Short dec_24;
    private Short dec_25;
    private Short dec_26;
    private Short dec_27;
    private Short dec_28;
    private Short dec_29;
    private Short dec_30;
    private Short dec_31;
    private Short dec_32;
    private Short dec_33;
    private Short dec_34;
    private Short dec_35;
    private Short dec_36;

    private Boolean temJanelaFavoravel;

    private Short riscoMinimo;
}
