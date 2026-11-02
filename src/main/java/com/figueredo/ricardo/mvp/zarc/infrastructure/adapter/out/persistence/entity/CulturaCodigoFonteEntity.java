package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
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
public class CulturaCodigoFonteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cultura_id", nullable = false)
    private Long culturaId;

    @Column(nullable = false, length = 30)
    private String fonte;

    @Column(name = "codigo_origem", nullable = false, length = 50)
    private String codigoOrigem;

    @Column(name = "nome_origem", length = 150)
    private String nomeOrigem;

    @Column(nullable = false)
    private Boolean ativo;
}
