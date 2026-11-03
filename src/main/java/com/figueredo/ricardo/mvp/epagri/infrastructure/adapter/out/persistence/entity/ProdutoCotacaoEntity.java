package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity;

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
@Table(name = "tb_produto_cotacao")
@Getter
@Setter
@NoArgsConstructor
public class ProdutoCotacaoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long culturaId;

    private String nomeBrutoOrigem;

    private String nomeComercial;

    private String unidadeComercializacao;

    private BigDecimal pesoEmbalagemKg;

    private String especificacao;

    private String fonte;

    private Boolean ativo;
}
