package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_controle_carga")
@Getter
@Setter
@NoArgsConstructor
public class ControleCargaEpagriEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fonte;

    private String identificadorRecurso;

    private String ultimoModificadoFonte;

    private String status;

    private Integer totalRegistrosProcessados;

    private String mensagemErro;

    private OffsetDateTime iniciadoEm;

    private OffsetDateTime finalizadoEm;
}
