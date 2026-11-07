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
@Table(name = "tb_cultura")
@Getter
@Setter
@NoArgsConstructor
public class CulturaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String nomeCientifico;

    private String grupoAgricola;

    private String descricao;

    private Boolean ativo;
}
