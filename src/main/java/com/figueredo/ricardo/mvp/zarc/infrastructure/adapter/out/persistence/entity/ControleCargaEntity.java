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

import java.time.Instant;

@Entity
@Table(name = "tb_controle_carga")
@Getter
@Setter
@NoArgsConstructor
public class ControleCargaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String fonte;

    @Column(name = "identificador_recurso", nullable = false, length = 150)
    private String identificadorRecurso;

    @Column(name = "ultimo_modificado_fonte", length = 100)
    private String ultimoModificadoFonte;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "total_registros_processados", nullable = false)
    private Integer totalRegistrosProcessados;

    @Column(name = "mensagem_erro", columnDefinition = "TEXT")
    private String mensagemErro;

    @Column(name = "iniciado_em", nullable = false)
    private Instant iniciadoEm;

    @Column(name = "finalizado_em")
    private Instant finalizadoEm;
}
