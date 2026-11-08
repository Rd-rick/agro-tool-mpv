package com.figueredo.ricardo.mvp.zarc.domain.model;

import java.util.Optional;

public record SoloZoneamento(
        String codigo,
        String descricao,
        String tipoSolo,
        String classificacao,
        String teorArgila,
        String aguaDisponivel
) implements Comparable<SoloZoneamento> {

    public SoloZoneamento {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("codigo do solo é obrigatório");
        }
        codigo = codigo.trim();
        Optional<TipoSoloZarc> enumOpt = TipoSoloZarc.buscarPorCodigoOuNome(codigo);
        if (enumOpt.isPresent()) {
            TipoSoloZarc t = enumOpt.get();
            codigo = t.getCodigo();
            tipoSolo = tipoSolo != null && !tipoSolo.isBlank() ? tipoSolo.trim() : t.getTipoSolo();
            classificacao = classificacao != null && !classificacao.isBlank() ? classificacao.trim() : t.getClassificacao();
            teorArgila = teorArgila != null && !teorArgila.isBlank() ? teorArgila.trim() : t.getTeorArgila();
            aguaDisponivel = aguaDisponivel != null && !aguaDisponivel.isBlank() ? aguaDisponivel.trim() : t.getAguaDisponivel();
            boolean descricaoGenerica = descricao == null
                    || descricao.isBlank()
                    || descricao.trim().equalsIgnoreCase(codigo)
                    || descricao.trim().equalsIgnoreCase("Tipo " + codigo)
                    || descricao.trim().equalsIgnoreCase("AD " + codigo)
                    || descricao.trim().equalsIgnoreCase("AD" + codigo);
            descricao = descricaoGenerica ? t.getDescricaoCompleta() : descricao.trim();
        } else {
            descricao = descricao != null && !descricao.isBlank() ? descricao.trim() : codigo;
            tipoSolo = tipoSolo != null && !tipoSolo.isBlank() ? tipoSolo.trim() : descricao;
            classificacao = classificacao != null && !classificacao.isBlank() ? classificacao.trim() : null;
            teorArgila = teorArgila != null && !teorArgila.isBlank() ? teorArgila.trim() : null;
            aguaDisponivel = aguaDisponivel != null && !aguaDisponivel.isBlank() ? aguaDisponivel.trim() : null;
        }
    }

    public SoloZoneamento(String codigo, String descricao, String tipoSolo, String classificacao, String teorArgila) {
        this(codigo, descricao, tipoSolo, classificacao, teorArgila, null);
    }

    public SoloZoneamento(String codigo, String descricao) {
        this(codigo, descricao, null, null, null, null);
    }

    public static SoloZoneamento deTipoSoloZarc(TipoSoloZarc tipoSoloZarc) {
        if (tipoSoloZarc == null) {
            throw new IllegalArgumentException("tipoSoloZarc é obrigatório");
        }
        return tipoSoloZarc.toSoloZoneamento();
    }

    @Override
    public int compareTo(SoloZoneamento outro) {
        return this.codigo.compareToIgnoreCase(outro.codigo);
    }
}
