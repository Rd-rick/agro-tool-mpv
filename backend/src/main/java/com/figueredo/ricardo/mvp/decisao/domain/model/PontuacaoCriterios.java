package com.figueredo.ricardo.mvp.decisao.domain.model;

public record PontuacaoCriterios(
        int segurancaClimatica,
        int janelaPlantio,
        int dinamicaPrecos,
        int potencialReceita,
        int qualidadeInformacao
) {
    public static final int MAX_SEGURANCA_CLIMATICA = 40;
    public static final int MAX_JANELA_PLANTIO = 15;
    public static final int MAX_DINAMICA_PRECOS = 20;
    public static final int MAX_POTENCIAL_RECEITA = 10;
    public static final int MAX_QUALIDADE_INFORMACAO = 15;

    public PontuacaoCriterios {
        validarIntervalo(segurancaClimatica, 0, MAX_SEGURANCA_CLIMATICA, "segurancaClimatica");
        validarIntervalo(janelaPlantio, 0, MAX_JANELA_PLANTIO, "janelaPlantio");
        validarIntervalo(dinamicaPrecos, 0, MAX_DINAMICA_PRECOS, "dinamicaPrecos");
        validarIntervalo(potencialReceita, 0, MAX_POTENCIAL_RECEITA, "potencialReceita");
        validarIntervalo(qualidadeInformacao, 0, MAX_QUALIDADE_INFORMACAO, "qualidadeInformacao");
    }

    public PontuacaoCriterios(
            int segurancaClimatica,
            int janelaPlantio,
            int dinamicaPrecos,
            int qualidadeInformacao
    ) {
        this(segurancaClimatica, janelaPlantio, dinamicaPrecos, 0, qualidadeInformacao);
    }

    public static PontuacaoCriterios zerada() {
        return new PontuacaoCriterios(0, 0, 0, 0, 0);
    }

    public int notaFinal() {
        return segurancaClimatica + janelaPlantio + dinamicaPrecos + potencialReceita + qualidadeInformacao;
    }

    private static void validarIntervalo(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(
                    campo + " deve estar entre " + minimo + " e " + maximo + ", recebido: " + valor
            );
        }
    }
}
