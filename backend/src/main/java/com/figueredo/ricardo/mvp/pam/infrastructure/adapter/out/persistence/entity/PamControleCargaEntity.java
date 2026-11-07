package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tb_controle_carga")
@Getter
@Setter
@NoArgsConstructor
public class PamControleCargaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fonte;

    private String identificadorRecurso;

    private String ultimoModificadoFonte;

    private String status;

    private Integer totalRegistrosProcessados;

    private String mensagemErro;

    private Instant iniciadoEm;

    private Instant finalizadoEm;
}
