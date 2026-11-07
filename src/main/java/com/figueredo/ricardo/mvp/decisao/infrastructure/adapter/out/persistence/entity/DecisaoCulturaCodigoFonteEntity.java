package com.figueredo.ricardo.mvp.decisao.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_cultura_codigo_fonte")
@Getter
@Setter
@NoArgsConstructor
public class DecisaoCulturaCodigoFonteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long culturaId;

    private String fonte;

    private String codigoOrigem;

    private String nomeOrigem;

    private Boolean ativo;
}
