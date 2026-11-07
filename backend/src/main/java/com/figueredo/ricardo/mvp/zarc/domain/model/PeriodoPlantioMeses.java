package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeSet;

public record PeriodoPlantioMeses(
        YearMonth mesInicio,
        YearMonth mesFim
) {
    private static final String REGEX_ANO_MES = "^\\d{4}-(0[1-9]|1[0-2])$";

    public PeriodoPlantioMeses {
        Objects.requireNonNull(mesInicio, "mesInicio é obrigatório");
        Objects.requireNonNull(mesFim, "mesFim é obrigatório");
        if (mesFim.isBefore(mesInicio)) {
            throw new IllegalArgumentException("mesFim não pode ser anterior a mesInicio");
        }
        if (ChronoUnit.MONTHS.between(mesInicio, mesFim) >= 12) {
            throw new IllegalArgumentException("O período de plantio não deve exceder 12 meses");
        }
    }

    public static PeriodoPlantioMeses deTexto(
            String mesInicioTexto,
            String mesFimTexto,
            YearMonth mesAtualReferencia
    ) {
        YearMonth inicio = parseAnoMes(mesInicioTexto, "mesInicio");
        YearMonth fim = parseAnoMes(mesFimTexto, "mesFim");
        if (mesAtualReferencia != null && inicio.isBefore(mesAtualReferencia)) {
            throw new IllegalArgumentException(
                    "mesInicio deve ser maior ou igual ao mês atual (" + mesAtualReferencia + ")"
            );
        }
        return new PeriodoPlantioMeses(inicio, fim);
    }

    public static PeriodoPlantioMeses resolverOuPadrao(
            String mesInicioTexto,
            String mesFimTexto,
            YearMonth mesAtualReferencia
    ) {
        YearMonth referencia = mesAtualReferencia != null ? mesAtualReferencia : YearMonth.now();
        boolean inicioVazio = mesInicioTexto == null || mesInicioTexto.isBlank();
        boolean fimVazio = mesFimTexto == null || mesFimTexto.isBlank();

        if (inicioVazio && fimVazio) {
            return new PeriodoPlantioMeses(referencia, referencia.plusMonths(3));
        }
        if (inicioVazio) {
            YearMonth fim = parseAnoMes(mesFimTexto, "mesFim");
            return new PeriodoPlantioMeses(referencia, fim);
        }
        if (fimVazio) {
            YearMonth inicio = parseAnoMes(mesInicioTexto, "mesInicio");
            if (inicio.isBefore(referencia)) {
                throw new IllegalArgumentException(
                        "mesInicio deve ser maior ou igual ao mês atual (" + referencia + ")"
                );
            }
            return new PeriodoPlantioMeses(inicio, inicio.plusMonths(3));
        }
        return deTexto(mesInicioTexto, mesFimTexto, referencia);
    }

    public boolean contemMes(int mes) {
        if (mes < 1 || mes > 12) {
            return false;
        }
        YearMonth cursor = mesInicio;
        while (!cursor.isAfter(mesFim)) {
            if (cursor.getMonthValue() == mes) {
                return true;
            }
            cursor = cursor.plusMonths(1);
        }
        return false;
    }

    public boolean contemDecendio(int numeroDecendio) {
        if (numeroDecendio < 1 || numeroDecendio > 36) {
            return false;
        }
        int mes = ((numeroDecendio - 1) / 3) + 1;
        return contemMes(mes);
    }

    public boolean possuiDecendioFavoravelNoPeriodo(List<Integer> decendios, Set<Integer> riscosFavoraveis) {
        if (decendios == null || decendios.isEmpty() || riscosFavoraveis == null || riscosFavoraveis.isEmpty()) {
            return false;
        }
        int numeroDecendio = 1;
        for (Integer risco : decendios) {
            if (risco != null && risco > 0 && riscosFavoraveis.contains(risco) && contemDecendio(numeroDecendio)) {
                return true;
            }
            numeroDecendio++;
        }
        return false;
    }

    public int ordemCronologicaDoMes(int mes) {
        YearMonth cursor = mesInicio;
        int ordem = 0;
        while (!cursor.isAfter(mesFim)) {
            if (cursor.getMonthValue() == mes) {
                return ordem;
            }
            cursor = cursor.plusMonths(1);
            ordem++;
        }
        return Integer.MAX_VALUE;
    }

    public Optional<PeriodoDecendio> selecionarMelhorDecendio(List<PeriodoDecendio> janelas) {
        if (janelas == null || janelas.isEmpty()) {
            return Optional.empty();
        }
        return janelas.stream()
                .filter(p -> p.menorRisco() != null && p.menorRisco() > 0 && contemMes(p.mes()))
                .min(Comparator
                        .comparingInt(PeriodoDecendio::menorRisco)
                        .thenComparingInt(p -> ordemCronologicaDoMes(p.mes()))
                        .thenComparingInt(PeriodoDecendio::decendioNoMes));
    }

    public boolean inicioPlantioDentroDoPeriodo(List<Integer> decendios, Set<Integer> riscosFavoraveis) {
        OptionalInt mesInicioPlantio = identificarMesInicioPlantio(decendios, riscosFavoraveis);
        return mesInicioPlantio.isPresent() && contemMes(mesInicioPlantio.getAsInt());
    }

    public static OptionalInt identificarMesInicioPlantio(List<Integer> decendios, Set<Integer> riscosFavoraveis) {
        if (decendios == null || decendios.isEmpty() || riscosFavoraveis == null || riscosFavoraveis.isEmpty()) {
            return OptionalInt.empty();
        }

        TreeSet<Integer> mesesFavoraveis = new TreeSet<>();
        int numeroDecendio = 1;
        for (Integer risco : decendios) {
            if (risco != null && risco > 0 && riscosFavoraveis.contains(risco)) {
                int mes = ((numeroDecendio - 1) / 3) + 1;
                mesesFavoraveis.add(mes);
            }
            numeroDecendio++;
        }

        if (mesesFavoraveis.isEmpty()) {
            return OptionalInt.empty();
        }

        if (mesesFavoraveis.size() == 12) {
            return OptionalInt.of(mesesFavoraveis.first());
        }

        if (mesesFavoraveis.contains(12) && mesesFavoraveis.contains(1)) {
            for (Integer mes : mesesFavoraveis) {
                if (mes > 1 && !mesesFavoraveis.contains(mes - 1)) {
                    return OptionalInt.of(mes);
                }
            }
        }

        return OptionalInt.of(mesesFavoraveis.first());
    }

    public String formatado() {
        return mesInicio + " a " + mesFim;
    }

    private static YearMonth parseAnoMes(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(nomeCampo + " é obrigatório");
        }
        String limpo = valor.trim();
        if (!limpo.matches(REGEX_ANO_MES)) {
            throw new IllegalArgumentException(nomeCampo + " deve estar no formato AAAA-MM");
        }
        return YearMonth.parse(limpo);
    }
}
