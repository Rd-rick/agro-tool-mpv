package com.figueredo.ricardo.mvp.decisao.domain.service;

import com.figueredo.ricardo.mvp.decisao.domain.model.ProjecaoProducaoEconomica;
import com.figueredo.ricardo.mvp.decisao.domain.model.StatusProjecaoProducao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class CalculadoraProjecaoEconomica {

    private static final String AVISO_RECEITA_BRUTA_METODOLOGIA =
            "A projeção de receita bruta refere-se ao faturamento potencial da colheita e não deduz os custos de produção (sementes, fertilizantes, defensivos, operações de maquinário e mão de obra), não representando lucro líquido.";

    private static final String AVISO_SEM_HISTORICO_PAM =
            "Projeção de produção e receita bruta indisponível: o município não possui histórico de rendimento registrado na pesquisa PAM do IBGE para esta cultura. O sistema não utiliza estimativas arbitrárias para evitar distorções econômicas.";

    private static final String AVISO_SEM_COTACAO =
            "Projeção de receita bruta indisponível: sem cotação de preço de comercialização vigente na base da Epagri/Cepa para a cultura.";

    private static final String AVISO_PESO_VARIAVEL =
            "Projeção de produção indisponível: produto comercializado em unidade com peso variável sem fator de conversão de quilogramas padronizado.";

    public ProjecaoProducaoEconomica calcular(
            BigDecimal areaHa,
            Integer rendimentoMedioKgHa,
            BigDecimal pesoEmbalagemKg,
            String unidadeComercializacao,
            BigDecimal precoReferenciaSaca
    ) {
        Objects.requireNonNull(areaHa, "areaHa é obrigatória");

        if (rendimentoMedioKgHa == null || rendimentoMedioKgHa <= 0) {
            return ProjecaoProducaoEconomica.indisponivel(
                    areaHa,
                    StatusProjecaoProducao.INDISPONIVEL_SEM_HISTORICO_MUNICIPAL,
                    AVISO_SEM_HISTORICO_PAM
            );
        }

        if (precoReferenciaSaca == null || precoReferenciaSaca.compareTo(BigDecimal.ZERO) <= 0) {
            return ProjecaoProducaoEconomica.indisponivel(
                    areaHa,
                    StatusProjecaoProducao.INDISPONIVEL_SEM_COTACAO,
                    AVISO_SEM_COTACAO
            );
        }

        if (pesoEmbalagemKg == null || pesoEmbalagemKg.compareTo(BigDecimal.ZERO) <= 0) {
            return ProjecaoProducaoEconomica.indisponivel(
                    areaHa,
                    StatusProjecaoProducao.INDISPONIVEL_UNIDADE_VARIAVEL,
                    AVISO_PESO_VARIAVEL
            );
        }

        BigDecimal rendimentoBd = BigDecimal.valueOf(rendimentoMedioKgHa);
        BigDecimal produtividadePorHa = rendimentoBd.divide(pesoEmbalagemKg, 2, RoundingMode.HALF_UP);
        BigDecimal producaoTotal = produtividadePorHa.multiply(areaHa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal receitaPorHa = produtividadePorHa.multiply(precoReferenciaSaca).setScale(2, RoundingMode.HALF_UP);
        BigDecimal receitaTotal = producaoTotal.multiply(precoReferenciaSaca).setScale(2, RoundingMode.HALF_UP);

        return ProjecaoProducaoEconomica.calculada(
                areaHa,
                rendimentoMedioKgHa,
                pesoEmbalagemKg,
                unidadeComercializacao != null ? unidadeComercializacao : "Saca",
                produtividadePorHa,
                producaoTotal,
                precoReferenciaSaca,
                receitaPorHa,
                receitaTotal,
                AVISO_RECEITA_BRUTA_METODOLOGIA
        );
    }
}
