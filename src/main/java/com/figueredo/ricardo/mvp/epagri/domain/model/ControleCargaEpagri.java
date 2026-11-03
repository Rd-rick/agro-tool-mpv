package com.figueredo.ricardo.mvp.epagri.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;

public class ControleCargaEpagri {

    private Long id;
    private final String fonte;
    private final String identificadorRecurso;
    private final String ultimoModificadoFonte;
    private StatusCarga status;
    private int totalRegistrosProcessados;
    private String mensagemErro;
    private final OffsetDateTime iniciadoEm;
    private OffsetDateTime finalizadoEm;

    public ControleCargaEpagri(String fonte, String identificadorRecurso, String ultimoModificadoFonte) {
        this.fonte = Objects.requireNonNull(fonte, "fonte não pode ser nula");
        this.identificadorRecurso = truncar(Objects.requireNonNull(identificadorRecurso, "identificadorRecurso não pode ser nulo"), 150);
        this.ultimoModificadoFonte = truncar(ultimoModificadoFonte, 100);
        this.status = StatusCarga.EM_ANDAMENTO;
        this.totalRegistrosProcessados = 0;
        this.iniciadoEm = OffsetDateTime.now();
    }

    public ControleCargaEpagri(
            Long id,
            String fonte,
            String identificadorRecurso,
            String ultimoModificadoFonte,
            StatusCarga status,
            int totalRegistrosProcessados,
            String mensagemErro,
            OffsetDateTime iniciadoEm,
            OffsetDateTime finalizadoEm
    ) {
        this.id = id;
        this.fonte = Objects.requireNonNull(fonte, "fonte não pode ser nula");
        this.identificadorRecurso = truncar(Objects.requireNonNull(identificadorRecurso, "identificadorRecurso não pode ser nulo"), 150);
        this.ultimoModificadoFonte = truncar(ultimoModificadoFonte, 100);
        this.status = Objects.requireNonNull(status, "status não pode ser nulo");
        this.totalRegistrosProcessados = totalRegistrosProcessados;
        this.mensagemErro = mensagemErro;
        this.iniciadoEm = Objects.requireNonNull(iniciadoEm, "iniciadoEm não pode ser nulo");
        this.finalizadoEm = finalizadoEm;
    }

    private static String truncar(String valor, int limite) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.length() > limite ? limpo.substring(0, limite) : limpo;
    }

    public void marcarSucesso(int totalRegistrosProcessados) {
        this.status = StatusCarga.SUCESSO;
        this.totalRegistrosProcessados = totalRegistrosProcessados;
        this.finalizadoEm = OffsetDateTime.now();
        this.mensagemErro = null;
    }

    public void marcarErro(String mensagemErro) {
        this.status = StatusCarga.ERRO;
        this.mensagemErro = mensagemErro;
        this.finalizadoEm = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
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

    public int getTotalRegistrosProcessados() {
        return totalRegistrosProcessados;
    }

    public String getMensagemErro() {
        return mensagemErro;
    }

    public OffsetDateTime getIniciadoEm() {
        return iniciadoEm;
    }

    public OffsetDateTime getFinalizadoEm() {
        return finalizadoEm;
    }
}
