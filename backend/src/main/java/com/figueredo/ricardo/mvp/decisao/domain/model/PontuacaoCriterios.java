package com.figueredo.ricardo.mvp.decisao.domain.model;

public record PontuacaoCriterios(
        int segurancaClimatica,
        int janelaPlantio,
        int dinamicaPrecos,
        int qualidadeInformacao
) {
    public static final int MAX_SEGURANCA_CLIMATICA = 35;
    public static final int MAX_JANELA_PLANTIO = 20;
    public static final int MAX_DINAMICA_PRECOS = 30;
    public static final int MAX_QUALIDADE_INFORMACAO = 15;

    public PontuacaoCriterios {
        validarIntervalo(segurancaClimatica, 0, MAX_SEGURANCA_CLIMATICA, "segurancaClimatica");
        validarIntervalo(janelaPlantio, 0, MAX_JANELA_PLANTIO, "janelaPlantio");
        validarIntervalo(dinamicaPrecos, 0, MAX_DINAMICA_PRECOS, "dinamicaPrecos");
        validarIntervalo(qualidadeInformacao, 0, MAX_QUALIDADE_INFORMACAO, "qualidadeInformacao");
    }

    public int notaFinal() {
        return segurancaClimatica + janelaPlantio + dinamicaPrecos + qualidadeInformacao;
    }

    private static void validarIntervalo(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(
                    campo + " deve estar entre " + minimo + " e " + maximo + ", recebido: " + valor
            );
        }
    }
}
