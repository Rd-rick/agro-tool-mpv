package com.figueredo.ricardo.mvp.decisao.domain.service;

import com.figueredo.ricardo.mvp.decisao.domain.model.HistoricoPamCultura;
import com.figueredo.ricardo.mvp.decisao.domain.model.PontuacaoCriterios;
import com.figueredo.ricardo.mvp.decisao.domain.model.ProjecaoProducaoEconomica;
import com.figueredo.ricardo.mvp.decisao.domain.model.ScoreOportunidade;
import com.figueredo.ricardo.mvp.decisao.domain.model.StatusProjecaoProducao;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.zarc.domain.model.CulturaZoneamentoConsolidado;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CalculadoraPontuacaoOportunidade {

    private static final String AVISO_METODOLOGICO_RENTABILIDADE =
            "A pontuação de oportunidade e a receita bruta estimada não representam estimativa de lucro líquido, pois dependem dos custos de produção (sementes, defensivos, fertilizantes, operações e frete).";

    private static final String AVISO_METODOLOGICO_TENDENCIA_HISTORICA =
            "A dinâmica de preços avalia variações percentuais históricas recentes (30, 90 e 180 dias) e não constitui garantia ou projeção de preço futuro na época da colheita.";

    private static final BigDecimal CINCO_POSITIVO = new BigDecimal("5.00");
    private static final BigDecimal UM_POSITIVO = new BigDecimal("1.00");
    private static final BigDecimal UM_NEGATIVO = new BigDecimal("-1.00");
    private static final BigDecimal CINCO_NEGATIVO = new BigDecimal("-5.00");

    private static final BigDecimal OITO_POSITIVO = new BigDecimal("8.00");
    private static final BigDecimal DOIS_POSITIVO = new BigDecimal("2.00");
    private static final BigDecimal DOIS_NEGATIVO = new BigDecimal("-2.00");
    private static final BigDecimal OITO_NEGATIVO = new BigDecimal("-8.00");

    private static final BigDecimal DEZ_POSITIVO = new BigDecimal("10.00");
    private static final BigDecimal DEZ_NEGATIVO = new BigDecimal("-10.00");

    private static final BigDecimal RECEITA_DEZ_MIL = new BigDecimal("10000.00");
    private static final BigDecimal RECEITA_OITO_MIL = new BigDecimal("8000.00");
    private static final BigDecimal RECEITA_SEIS_MIL = new BigDecimal("6000.00");
    private static final BigDecimal RECEITA_QUATRO_MIL = new BigDecimal("4000.00");
    private static final BigDecimal RECEITA_DOIS_MIL = new BigDecimal("2000.00");

    private final CalculadoraProjecaoEconomica calculadoraProjecao = new CalculadoraProjecaoEconomica();

    public ScoreOportunidade calcular(
            CulturaZoneamentoConsolidado zoneamento,
            CulturaMercadoResultado mercado,
            HistoricoPamCultura historicoPam,
            BigDecimal areaHa,
            LocalDate dataReferenciaCotacao
    ) {
        Objects.requireNonNull(zoneamento, "zoneamento é obrigatório");
        BigDecimal areaResolvida = areaHa != null ? areaHa : BigDecimal.ONE;

        HistoricoPamCultura pamResolvido = historicoPam != null
                ? historicoPam
                : HistoricoPamCultura.semHistorico(zoneamento.culturaId());

        List<String> justificativas = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        avisos.add(AVISO_METODOLOGICO_RENTABILIDADE);
        avisos.add(AVISO_METODOLOGICO_TENDENCIA_HISTORICA);

        int pontosSeguranca = calcularSegurancaClimatica(zoneamento, justificativas);
        int pontosJanela = calcularAmplitudeJanela(zoneamento, justificativas);

        Optional<ProdutoMercadoAnalise> produtoOpt = selecionarProdutoRepresentativo(mercado);
        ProdutoMercadoAnalise prod = produtoOpt.orElse(null);

        ProjecaoProducaoEconomica projecaoEconomica = calculadoraProjecao.calcular(
                areaResolvida,
                pamResolvido.rendimentoMedioKgHa(),
                prod != null ? prod.pesoEmbalagemKg() : null,
                prod != null ? prod.unidadeComercializacao() : null,
                prod != null ? prod.precoAtualReferencia() : null
        );

        int pontosPrecos = calcularDinamicaPrecos(prod, justificativas, avisos);
        int pontosPotencialReceita = calcularPotencialReceita(projecaoEconomica, justificativas, avisos);
        int pontosQualidade = calcularQualidadeInformacao(
                prod,
                pamResolvido,
                dataReferenciaCotacao,
                justificativas,
                avisos
        );

        PontuacaoCriterios criterios = new PontuacaoCriterios(
                pontosSeguranca,
                pontosJanela,
                pontosPrecos,
                pontosPotencialReceita,
                pontosQualidade
        );

        return new ScoreOportunidade(
                zoneamento.culturaId(),
                zoneamento.nomeCultura(),
                zoneamento.nomeCientifico(),
                zoneamento.grupoAgricola(),
                0,
                criterios.notaFinal(),
                criterios,
                zoneamento.riscoMinimo(),
                zoneamento.janelasPlantio().size(),
                zoneamento.tiposSolo().size(),
                zoneamento.ciclos().size(),
                zoneamento.melhorPeriodoPlantio(),
                zoneamento.recomendacaoMelhorPeriodo(),
                zoneamento.soloProjecao(),
                zoneamento.tiposSolo(),
                prod != null ? prod.nomeComercial() : null,
                prod != null ? prod.precoAtualReferencia() : null,
                prod != null ? prod.unidadeComercializacao() : null,
                prod != null ? prod.dataUltimaCotacao() : null,
                prod != null ? prod.variacao30DiasPercentual() : null,
                prod != null ? prod.variacao90DiasPercentual() : null,
                prod != null ? prod.variacao180DiasPercentual() : null,
                prod != null ? prod.origem().pracaNome() : null,
                prod != null ? prod.origem().fonte() : null,
                prod != null && prod.origem().fallbackEstadual(),
                pamResolvido.possuiHistorico(),
                pamResolvido.ultimoAno(),
                pamResolvido.rendimentoMedioKgHa(),
                projecaoEconomica,
                justificativas,
                avisos
        );
    }

    public ScoreOportunidade calcular(
            CulturaZoneamentoConsolidado zoneamento,
            CulturaMercadoResultado mercado,
            HistoricoPamCultura historicoPam,
            LocalDate dataReferenciaCotacao
    ) {
        return calcular(zoneamento, mercado, historicoPam, BigDecimal.ONE, dataReferenciaCotacao);
    }

    private int calcularSegurancaClimatica(CulturaZoneamentoConsolidado zoneamento, List<String> justificativas) {
        int riscoMinimo = zoneamento.riscoMinimo();
        int pontosRiscoBase = switch (riscoMinimo) {
            case 20 -> 34;
            case 30 -> 24;
            case 40 -> 12;
            default -> 0;
        };

        int qtdSolos = zoneamento.tiposSolo().size();
        int qtdCiclos = zoneamento.ciclos().size();

        int bonusSolos = Math.min(3, qtdSolos);
        int bonusCiclos = Math.min(3, qtdCiclos);
        int total = Math.min(PontuacaoCriterios.MAX_SEGURANCA_CLIMATICA, pontosRiscoBase + bonusSolos + bonusCiclos);

        justificativas.add(String.format(
                "Segurança Climática ZARC (%d/40 pts): menor risco climático de %d%% (%d pts base) com %d tipo(s) de solo (+%d pts) e %d ciclo(s) apto(s) (+%d pts).",
                total,
                riscoMinimo,
                pontosRiscoBase,
                qtdSolos,
                bonusSolos,
                qtdCiclos,
                bonusCiclos
        ));

        return total;
    }

    private int calcularAmplitudeJanela(CulturaZoneamentoConsolidado zoneamento, List<String> justificativas) {
        int decendios = zoneamento.janelasPlantio().size();
        int pontos;
        if (decendios >= 12) {
            pontos = 15;
        } else if (decendios >= 10) {
            pontos = 13;
        } else if (decendios >= 7) {
            pontos = 11;
        } else if (decendios >= 5) {
            pontos = 9;
        } else if (decendios >= 3) {
            pontos = 6;
        } else if (decendios >= 1) {
            pontos = 3;
        } else {
            pontos = 0;
        }

        String complementoMelhorPeriodo = zoneamento.recomendacaoMelhorPeriodo() != null && !zoneamento.recomendacaoMelhorPeriodo().isBlank()
                ? " " + zoneamento.recomendacaoMelhorPeriodo() + "."
                : "";

        justificativas.add(String.format(
                "Amplitude da Janela de Plantio (%d/15 pts): %d decêndio(s) favorável(is) identificado(s) na safra.%s",
                pontos,
                decendios,
                complementoMelhorPeriodo
        ));

        return pontos;
    }

    private int calcularDinamicaPrecos(
            ProdutoMercadoAnalise produto,
            List<String> justificativas,
            List<String> avisos
    ) {
        if (produto == null) {
            justificativas.add(
                    "Dinâmica e Tendência de Preços (0/20 pts): sem série de cotações disponível na Epagri/Cepa para avaliar comportamento de preços."
            );
            avisos.add("Cultura sem cotação ativa na base da Epagri/Cepa; a pontuação de dinâmica de preços foi zerada.");
            return 0;
        }

        BigDecimal var30 = valorOuZero(produto.variacao30DiasPercentual());
        BigDecimal var90 = valorOuZero(produto.variacao90DiasPercentual());
        BigDecimal var180 = valorOuZero(produto.variacao180DiasPercentual());

        int pts30 = pontuarCurtoPrazo30Dias(var30);
        int pts90 = pontuarMedioPrazo90Dias(var90);
        int pts180 = pontuarLongoPrazo180Dias(var180);
        int ptsConsistencia = pontuarConsistenciaTendencia(var30, var90, var180);

        int total = Math.min(PontuacaoCriterios.MAX_DINAMICA_PRECOS, pts30 + pts90 + pts180 + ptsConsistencia);

        justificativas.add(String.format(
                "Dinâmica e Tendência de Preços (%d/20 pts) para '%s': variação 30d de %s%% (%d/5 pts), 90d de %s%% (%d/7 pts), 180d de %s%% (%d/5 pts) e consistência (%d/3 pts).",
                total,
                produto.nomeComercial(),
                var30.toPlainString(),
                pts30,
                var90.toPlainString(),
                pts90,
                var180.toPlainString(),
                pts180,
                ptsConsistencia
        ));

        return total;
    }

    private int calcularPotencialReceita(
            ProjecaoProducaoEconomica projecao,
            List<String> justificativas,
            List<String> avisos
    ) {
        if (projecao == null || projecao.status() != StatusProjecaoProducao.CALCULADO_COM_SUCESSO || projecao.receitaBrutaPorHa() == null) {
            justificativas.add(
                    "Potencial de Receita Bruta (0/10 pts): projeção indisponível por ausência de dados oficiais de produtividade no município ou cotação regional."
            );
            if (projecao != null && projecao.status() == StatusProjecaoProducao.INDISPONIVEL_SEM_HISTORICO_MUNICIPAL) {
                avisos.add(projecao.avisoMetodologico());
            }
            return 0;
        }

        BigDecimal receitaPorHa = projecao.receitaBrutaPorHa();
        int pontos;
        if (receitaPorHa.compareTo(RECEITA_DEZ_MIL) >= 0) {
            pontos = 10;
        } else if (receitaPorHa.compareTo(RECEITA_OITO_MIL) >= 0) {
            pontos = 8;
        } else if (receitaPorHa.compareTo(RECEITA_SEIS_MIL) >= 0) {
            pontos = 6;
        } else if (receitaPorHa.compareTo(RECEITA_QUATRO_MIL) >= 0) {
            pontos = 4;
        } else if (receitaPorHa.compareTo(RECEITA_DOIS_MIL) >= 0) {
            pontos = 2;
        } else if (receitaPorHa.compareTo(BigDecimal.ZERO) > 0) {
            pontos = 1;
        } else {
            pontos = 0;
        }

        justificativas.add(String.format(
                "Potencial de Receita Bruta (%d/10 pts): receita bruta estimada de R$ %s/ha (%s %s/ha a R$ %s/%s).",
                pontos,
                receitaPorHa.toPlainString(),
                projecao.produtividadeEstimadaPorHa().toPlainString(),
                projecao.unidadeComercializacao(),
                projecao.precoReferencia().toPlainString(),
                projecao.unidadeComercializacao()
        ));

        return pontos;
    }

    private int calcularQualidadeInformacao(
            ProdutoMercadoAnalise produto,
            HistoricoPamCultura pam,
            LocalDate dataReferenciaCotacao,
            List<String> justificativas,
            List<String> avisos
    ) {
        int ptsPraca = 0;
        int ptsAtualidade = 0;

        if (produto != null) {
            if (!produto.origem().fallbackEstadual()) {
                ptsPraca = 6;
            } else {
                ptsPraca = 5;
                avisos.add(String.format(
                        "Cotação obtida por referência estadual (%s) devido à ausência de levantamento específico na praça regional do município.",
                        produto.origem().pracaNome()
                ));
            }

            ptsAtualidade = pontuarAtualidadeCotacao(produto.dataUltimaCotacao(), dataReferenciaCotacao);
        }

        int ptsPam = 0;
        if (pam.possuiProdutividadeApurada()) {
            ptsPam = 5;
        } else if (pam.possuiHistorico()) {
            ptsPam = 3;
        } else {
            avisos.add("Sem histórico de produção municipal registrado no IBGE/PAM para esta cultura nos anos consultados.");
        }

        int total = Math.min(PontuacaoCriterios.MAX_QUALIDADE_INFORMACAO, ptsPraca + ptsAtualidade + ptsPam);

        String descricaoPraca = produto == null
                ? "sem cotação (0/6 pts)"
                : (!produto.origem().fallbackEstadual()
                        ? "praça regional direta '" + produto.origem().pracaNome() + "' (6/6 pts)"
                        : "referência estadual '" + produto.origem().pracaNome() + "' (5/6 pts)");

        String descricaoPam = pam.possuiProdutividadeApurada()
                ? String.format("histórico PAM em %d com rendimento médio de %d kg/ha (5/5 pts)", pam.ultimoAno(), pam.rendimentoMedioKgHa())
                : (pam.possuiHistorico()
                        ? String.format("histórico PAM em %d com área registrada (3/5 pts)", pam.ultimoAno())
                        : "sem histórico PAM no município (0/5 pts)");

        justificativas.add(String.format(
                "Qualidade e Cobertura da Informação (%d/15 pts): %s, atualidade da cotação (%d/4 pts) e %s.",
                total,
                descricaoPraca,
                ptsAtualidade,
                descricaoPam
        ));

        return total;
    }

    private Optional<ProdutoMercadoAnalise> selecionarProdutoRepresentativo(CulturaMercadoResultado mercado) {
        if (mercado == null || mercado.produtos().isEmpty()) {
            return Optional.empty();
        }

        return mercado.produtos().stream()
                .max(Comparator
                        .comparing((ProdutoMercadoAnalise p) -> !p.origem().fallbackEstadual())
                        .thenComparing(
                                ProdutoMercadoAnalise::dataUltimaCotacao,
                                Comparator.nullsFirst(Comparator.naturalOrder())
                        )
                        .thenComparingInt(this::calcularPontuacaoBrutaProduto)
                        .thenComparing(
                                ProdutoMercadoAnalise::produtoId,
                                Comparator.reverseOrder()
                        ));
    }

    private int calcularPontuacaoBrutaProduto(ProdutoMercadoAnalise produto) {
        BigDecimal var30 = valorOuZero(produto.variacao30DiasPercentual());
        BigDecimal var90 = valorOuZero(produto.variacao90DiasPercentual());
        BigDecimal var180 = valorOuZero(produto.variacao180DiasPercentual());
        return pontuarCurtoPrazo30Dias(var30)
                + pontuarMedioPrazo90Dias(var90)
                + pontuarLongoPrazo180Dias(var180)
                + pontuarConsistenciaTendencia(var30, var90, var180);
    }

    private int pontuarCurtoPrazo30Dias(BigDecimal var30) {
        if (var30.compareTo(CINCO_POSITIVO) >= 0) {
            return 5;
        }
        if (var30.compareTo(UM_POSITIVO) >= 0) {
            return 4;
        }
        if (var30.compareTo(UM_NEGATIVO) >= 0) {
            return 3;
        }
        if (var30.compareTo(CINCO_NEGATIVO) >= 0) {
            return 1;
        }
        return 0;
    }

    private int pontuarMedioPrazo90Dias(BigDecimal var90) {
        if (var90.compareTo(OITO_POSITIVO) >= 0) {
            return 7;
        }
        if (var90.compareTo(DOIS_POSITIVO) >= 0) {
            return 5;
        }
        if (var90.compareTo(DOIS_NEGATIVO) >= 0) {
            return 3;
        }
        if (var90.compareTo(OITO_NEGATIVO) >= 0) {
            return 1;
        }
        return 0;
    }

    private int pontuarLongoPrazo180Dias(BigDecimal var180) {
        if (var180.compareTo(DEZ_POSITIVO) >= 0) {
            return 5;
        }
        if (var180.compareTo(DOIS_POSITIVO) >= 0) {
            return 4;
        }
        if (var180.compareTo(DOIS_NEGATIVO) >= 0) {
            return 3;
        }
        if (var180.compareTo(DEZ_NEGATIVO) >= 0) {
            return 1;
        }
        return 0;
    }

    private int pontuarConsistenciaTendencia(BigDecimal var30, BigDecimal var90, BigDecimal var180) {
        boolean alta30 = var30.compareTo(BigDecimal.ZERO) > 0;
        boolean alta90 = var90.compareTo(BigDecimal.ZERO) > 0;
        boolean alta180 = var180.compareTo(BigDecimal.ZERO) > 0;

        if (alta30 && alta90 && alta180) {
            return 3;
        }

        boolean semQuedaNosTres = var30.compareTo(BigDecimal.ZERO) >= 0
                && var90.compareTo(BigDecimal.ZERO) >= 0
                && var180.compareTo(BigDecimal.ZERO) >= 0;
        if (semQuedaNosTres) {
            return 2;
        }

        int horizontesPositivos = (alta30 ? 1 : 0) + (alta90 ? 1 : 0) + (alta180 ? 1 : 0);
        boolean semQuedaAbrupta = var30.compareTo(CINCO_NEGATIVO) >= 0
                && var90.compareTo(OITO_NEGATIVO) >= 0
                && var180.compareTo(DEZ_NEGATIVO) >= 0;

        if (horizontesPositivos >= 2 && semQuedaAbrupta) {
            return 1;
        }

        return 0;
    }

    private int pontuarAtualidadeCotacao(LocalDate dataUltimaCotacao, LocalDate dataReferencia) {
        if (dataUltimaCotacao == null) {
            return 0;
        }
        LocalDate referencia = dataReferencia != null ? dataReferencia : dataUltimaCotacao;
        long diasDefasagem = Math.max(0L, ChronoUnit.DAYS.between(dataUltimaCotacao, referencia));

        if (diasDefasagem <= 7) {
            return 4;
        }
        if (diasDefasagem <= 30) {
            return 3;
        }
        if (diasDefasagem <= 60) {
            return 2;
        }
        return 0;
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}
