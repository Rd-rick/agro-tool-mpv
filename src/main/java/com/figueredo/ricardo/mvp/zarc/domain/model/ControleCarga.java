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
        this.identificadorRecurso = truncar(Objects.requireNonNull(identificadorRecurso, "identificadorRecurso é obrigatório"), 150);
        this.ultimoModificadoFonte = truncar(ultimoModificadoFonte, 100);
        this.status = StatusCarga.EM_ANDAMENTO;
        this.totalRegistrosProcessados = 0;
        this.iniciadoEm = Instant.now();
    }

    public ControleCarga(Long id, String fonte, String identificadorRecurso, String ultimoModificadoFonte,
                         StatusCarga status, Integer totalRegistrosProcessados, String mensagemErro,
                         Instant iniciadoEm, Instant finalizadoEm) {
        this.id = id;
        this.fonte = Objects.requireNonNull(fonte, "fonte é obrigatória");
        this.identificadorRecurso = truncar(Objects.requireNonNull(identificadorRecurso, "identificadorRecurso é obrigatório"), 150);
        this.ultimoModificadoFonte = truncar(ultimoModificadoFonte, 100);
        this.status = Objects.requireNonNull(status, "status é obrigatório");
        this.totalRegistrosProcessados = totalRegistrosProcessados != null ? totalRegistrosProcessados : 0;
        this.mensagemErro = mensagemErro;
        this.iniciadoEm = Objects.requireNonNull(iniciadoEm, "iniciadoEm é obrigatório");
        this.finalizadoEm = finalizadoEm;
    }

    public void marcarSucesso(int totalProcessados) {
        this.status = StatusCarga.SUCESSO;
        this.totalRegistrosProcessados = totalProcessados;
        this.mensagemErro = null;
        this.finalizadoEm = Instant.now();
    }

    private static String truncar(String valor, int limite) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.length() > limite ? limpo.substring(0, limite) : limpo;
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
