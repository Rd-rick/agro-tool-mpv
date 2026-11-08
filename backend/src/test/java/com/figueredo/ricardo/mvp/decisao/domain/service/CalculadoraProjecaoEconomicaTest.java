package com.figueredo.ricardo.mvp.decisao.domain.service;

import com.figueredo.ricardo.mvp.decisao.domain.model.ProjecaoProducaoEconomica;
import com.figueredo.ricardo.mvp.decisao.domain.model.StatusProjecaoProducao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalculadoraProjecaoEconomicaTest {

    private final CalculadoraProjecaoEconomica calculadora = new CalculadoraProjecaoEconomica();

    @Test
    void deveCalcularProjecaoComSucessoParaMilhoEm10Hectares() {
        BigDecimal area = new BigDecimal("10.00");
        Integer rendimentoKgHa = 9000;
        BigDecimal pesoSaca = new BigDecimal("60.00");
        String unidade = "Saca";
        BigDecimal precoSaca = new BigDecimal("58.00");

        ProjecaoProducaoEconomica projecao = calculadora.calcular(
                area,
                rendimentoKgHa,
                pesoSaca,
                unidade,
                precoSaca
        );

        assertEquals(StatusProjecaoProducao.CALCULADO_COM_SUCESSO, projecao.status());
        assertEquals(new BigDecimal("150.00"), projecao.produtividadeEstimadaPorHa());
        assertEquals(new BigDecimal("1500.00"), projecao.producaoTotalEstimada());
        assertEquals(new BigDecimal("8700.00"), projecao.receitaBrutaPorHa());
        assertEquals(new BigDecimal("87000.00"), projecao.receitaBrutaTotal());
        assertNotNull(projecao.avisoMetodologico());
        assertTrue(projecao.avisoMetodologico().contains("não deduz os custos de produção"));
    }

    @Test
    void deveRetornarStatusIndisponivelQuandoNaoHouverRendimentoPam() {
        BigDecimal area = new BigDecimal("15.00");
        BigDecimal pesoSaca = new BigDecimal("60.00");
        BigDecimal precoSaca = new BigDecimal("120.00");

        ProjecaoProducaoEconomica projecao = calculadora.calcular(
                area,
                null,
                pesoSaca,
                "Saca",
                precoSaca
        );

        assertEquals(StatusProjecaoProducao.INDISPONIVEL_SEM_HISTORICO_MUNICIPAL, projecao.status());
        assertNull(projecao.producaoTotalEstimada());
        assertNull(projecao.receitaBrutaTotal());
        assertTrue(projecao.avisoMetodologico().contains("o município não possui histórico de rendimento registrado"));
    }

    @Test
    void deveRetornarStatusIndisponivelQuandoNaoHouverCotacao() {
        BigDecimal area = new BigDecimal("10.00");
        Integer rendimentoKgHa = 3600;
        BigDecimal pesoSaca = new BigDecimal("60.00");

        ProjecaoProducaoEconomica projecao = calculadora.calcular(
                area,
                rendimentoKgHa,
                pesoSaca,
                "Saca",
                null
        );

        assertEquals(StatusProjecaoProducao.INDISPONIVEL_SEM_COTACAO, projecao.status());
        assertNull(projecao.receitaBrutaTotal());
    }

    @Test
    void deveRetornarStatusIndisponivelQuandoPesoEmbalagemForNulo() {
        BigDecimal area = new BigDecimal("5.00");
        Integer rendimentoKgHa = 40000;
        BigDecimal precoCx = new BigDecimal("80.00");

        ProjecaoProducaoEconomica projecao = calculadora.calcular(
                area,
                rendimentoKgHa,
                null,
                "Caixa",
                precoCx
        );

        assertEquals(StatusProjecaoProducao.INDISPONIVEL_UNIDADE_VARIAVEL, projecao.status());
        assertNull(projecao.producaoTotalEstimada());
    }

    @Test
    void deveLancarExcecaoQuandoAreaForNula() {
        assertThrows(NullPointerException.class, () -> calculadora.calcular(
                null,
                9000,
                new BigDecimal("60.00"),
                "Saca",
                new BigDecimal("58.00")
        ));
    }
}
