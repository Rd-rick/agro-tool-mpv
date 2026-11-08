package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record PeriodoDecendio(
        int decendio,
        int mes,
        String nomeMes,
        int decendioNoMes,
        int diaInicio,
        int diaFim,
        Integer menorRisco,
        Set<Integer> riscosDisponiveis
) {
    private static final List<String> NOMES_MESES = List.of(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
    );

    private static final List<Integer> ULTIMO_DIA_MES = List.of(
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    );

    public PeriodoDecendio {
        if (decendio < 1 || decendio > 36) {
            throw new IllegalArgumentException("decendio deve estar entre 1 e 36");
        }
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("mes deve estar entre 1 e 12");
        }
        if (nomeMes == null || nomeMes.isBlank()) {
            throw new IllegalArgumentException("nomeMes é obrigatório");
        }
        if (decendioNoMes < 1 || decendioNoMes > 3) {
            throw new IllegalArgumentException("decendioNoMes deve estar entre 1 e 3");
        }
        Objects.requireNonNull(menorRisco, "menorRisco é obrigatório");
        Objects.requireNonNull(riscosDisponiveis, "riscosDisponiveis é obrigatório");
        riscosDisponiveis = Set.copyOf(new TreeSet<>(riscosDisponiveis));
    }

    public static PeriodoDecendio deDecendio(int numeroDecendio, Set<Integer> riscos) {
        if (numeroDecendio < 1 || numeroDecendio > 36) {
            throw new IllegalArgumentException("decendio deve estar entre 1 e 36");
        }
        if (riscos == null || riscos.isEmpty()) {
            throw new IllegalArgumentException("riscos não pode ser vazio");
        }

        int indiceZeroBased = numeroDecendio - 1;
        int mesCalculado = (indiceZeroBased / 3) + 1;
        int decendioNoMesCalculado = (indiceZeroBased % 3) + 1;
        String nomeMesCalculado = NOMES_MESES.get(mesCalculado - 1);

        int inicio = switch (decendioNoMesCalculado) {
            case 1 -> 1;
            case 2 -> 11;
            default -> 21;
        };

        int fim = switch (decendioNoMesCalculado) {
            case 1 -> 10;
            case 2 -> 20;
            default -> ULTIMO_DIA_MES.get(mesCalculado - 1);
        };

        TreeSet<Integer> ordenados = new TreeSet<>(riscos);
        Integer menor = ordenados.first();

        return new PeriodoDecendio(
                numeroDecendio,
                mesCalculado,
                nomeMesCalculado,
                decendioNoMesCalculado,
                inicio,
                fim,
                menor,
                ordenados
        );
    }

    public String descricaoPeriodo() {
        return String.format("%02d a %02d de %s", diaInicio, diaFim, nomeMes);
    }

    public String formatarRecomendacaoSolo(String descricaoSolo) {
        String soloFormatado;
        if (descricaoSolo == null || descricaoSolo.isBlank()) {
            soloFormatado = "o solo selecionado";
        } else {
            String texto = descricaoSolo.trim();
            soloFormatado = TipoSoloZarc.buscarPorCodigoOuNome(texto)
                    .map(TipoSoloZarc::getTipoSolo)
                    .orElse(texto);
        }
        return String.format(
                "De %s (risco de %d%%) para %s",
                descricaoPeriodo(),
                menorRisco,
                soloFormatado
        );
    }
}
