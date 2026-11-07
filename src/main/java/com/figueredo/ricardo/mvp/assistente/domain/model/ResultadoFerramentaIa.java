package com.figueredo.ricardo.mvp.assistente.domain.model;

public record ResultadoFerramentaIa(
        String nomeFerramenta,
        boolean sucesso,
        String dadosJson,
        String mensagemErro
) {
    public static ResultadoFerramentaIa comSucesso(String nomeFerramenta, String dadosJson) {
        return new ResultadoFerramentaIa(nomeFerramenta, true, dadosJson, null);
    }

    public static ResultadoFerramentaIa comErro(String nomeFerramenta, String mensagemErro) {
        return new ResultadoFerramentaIa(nomeFerramenta, false, "{}", mensagemErro);
    }
}
