package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_pam_producao")
@Getter
@Setter
@NoArgsConstructor
public class DecisaoPamProducaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long municipioId;

    private Long culturaCodigoFonteId;

    private Integer ano;

    private Integer areaPlantadaHa;

    private Integer areaColhidaHa;

    private Integer quantidadeProduzidaTon;

    private Integer rendimentoMedioKgHa;

    private BigDecimal valorProducaoMilReais;
}
