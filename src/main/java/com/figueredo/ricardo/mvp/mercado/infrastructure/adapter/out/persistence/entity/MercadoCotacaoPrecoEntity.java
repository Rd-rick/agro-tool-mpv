package com.figueredo.ricardo.mvp.mercado.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_cotacao_preco")
@Getter
@Setter
@NoArgsConstructor
public class MercadoCotacaoPrecoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produtoId;

    private Long pracaId;

    private LocalDate dataCotacao;

    private BigDecimal precoMinimo;

    private BigDecimal precoReferencia;

    private BigDecimal precoMaximo;

    private String frequencia;
}
