package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.time.Instant;
import java.util.Objects;

public class ControleCarga {

    private Long id;
    private final String fonte;
    private final String identificadorRecurso;
    private final String ultimoModificadoFonte;
    private StatusCarga status;
    private Integer totalRegistrosProcessados;
    private String mensagemErro;
    private final Instant iniciadoEm;
    private Instant finalizadoEm;

    public ControleCarga(String fonte, String identificadorRecurso, String ultimoModificadoFonte) {
        this.fonte = Objects.requireNonNull(fonte, "fonte é obrigatória");
        this.identificadorRecurso = Objects.requireNonNull(identificadorRecurso, "identificadorRecurso é obrigatório");
        this.ultimoModificadoFonte = ultimoModificadoFonte;
        this.status = StatusCarga.EM_ANDAMENTO;
        this.totalRegistrosProcessados = 0;
        this.iniciadoEm = Instant.now();
    }

    public ControleCarga(Long id, String fonte, String identificadorRecurso, String ultimoModificadoFonte,
                         StatusCarga status, Integer totalRegistrosProcessados, String mensagemErro,
                         Instant iniciadoEm, Instant finalizadoEm) {
        this.id = id;
        this.fonte = fonte;
        this.identificadorRecurso = identificadorRecurso;
        this.ultimoModificadoFonte = ultimoModificadoFonte;
        this.status = status;
        this.totalRegistrosProcessados = totalRegistrosProcessados;
        this.mensagemErro = mensagemErro;
        this.iniciadoEm = iniciadoEm;
        this.finalizadoEm = finalizadoEm;
    }

    public void marcarSucesso(int totalProcessados) {
        this.status = StatusCarga.SUCESSO;
        this.totalRegistrosProcessados = totalProcessados;
        this.finalizadoEm = Instant.now();
    }

    public void marcarErro(String mensagem) {
        this.status = StatusCarga.ERRO;
        this.mensagemErro = mensagem;
        this.finalizadoEm = Instant.now();
    }

    public void marcarNaoModificado() {
        this.status = StatusCarga.NAO_MODIFICADO;
        this.finalizadoEm = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFonte() {
        return fonte;
    }

    public String getIdentificadorRecurso() {
        return identificadorRecurso;
    }

    public String getUltimoModificadoFonte() {
        return ultimoModificadoFonte;
    }

    public StatusCarga getStatus() {
        return status;
    }

    public Integer getTotalRegistrosProcessados() {
        return totalRegistrosProcessados;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public Instant getIniciadoEm() {
        return iniciadoEm;
    }

    public Instant getFinalizadoEm() {
        return finalizadoEm;
    }
}
