package com.figueredo.ricardo.mvp.pam.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.pam.application.port.out.PamPersistencePort;
import com.figueredo.ricardo.mvp.pam.domain.model.PamProducaoRegistro;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PamJdbcPersistenceAdapter implements PamPersistencePort {

    private static final String SQL_UPSERT = """
            INSERT INTO tb_pam_producao (
                municipio_id, cultura_codigo_fonte_id, ano,
                area_plantada_ha, area_colhida_ha, quantidade_produzida_ton,
                rendimento_medio_kg_ha, valor_producao_mil_reais,
                criado_em, atualizado_em
            ) VALUES (
                ?, ?, ?,
                ?, ?, ?,
                ?, ?,
                NOW(), NOW()
            )
            ON CONFLICT (municipio_id, cultura_codigo_fonte_id, ano)
            DO UPDATE SET
                area_plantada_ha = EXCLUDED.area_plantada_ha,
                area_colhida_ha = EXCLUDED.area_colhida_ha,
                quantidade_produzida_ton = EXCLUDED.quantidade_produzida_ton,
                rendimento_medio_kg_ha = EXCLUDED.rendimento_medio_kg_ha,
                valor_producao_mil_reais = EXCLUDED.valor_producao_mil_reais,
                atualizado_em = NOW()
            WHERE (
                tb_pam_producao.area_plantada_ha,
                tb_pam_producao.area_colhida_ha,
                tb_pam_producao.quantidade_produzida_ton,
                tb_pam_producao.rendimento_medio_kg_ha,
                tb_pam_producao.valor_producao_mil_reais
            ) IS DISTINCT FROM (
                EXCLUDED.area_plantada_ha,
                EXCLUDED.area_colhida_ha,
                EXCLUDED.quantidade_produzida_ton,
                EXCLUDED.rendimento_medio_kg_ha,
                EXCLUDED.valor_producao_mil_reais
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public PamJdbcPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public int salvarLote(List<PamProducaoRegistro> registros) {
        if (registros == null || registros.isEmpty()) {
            return 0;
        }

        List<PamProducaoRegistro> loteUnico = deduplicarLote(registros);

        int[][] updateCounts = jdbcTemplate.batchUpdate(
                SQL_UPSERT,
                loteUnico,
                loteUnico.size(),
                (ps, reg) -> {
                    ps.setLong(1, reg.municipioId());
                    ps.setLong(2, reg.culturaCodigoFonteId());
                    ps.setInt(3, reg.ano());

                    if (reg.areaPlantadaHa() != null) {
                        ps.setInt(4, reg.areaPlantadaHa());
                    } else {
                        ps.setNull(4, Types.INTEGER);
                    }

                    if (reg.areaColhidaHa() != null) {
                        ps.setInt(5, reg.areaColhidaHa());
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }

                    if (reg.quantidadeProduzidaTon() != null) {
                        ps.setInt(6, reg.quantidadeProduzidaTon());
                    } else {
                        ps.setNull(6, Types.INTEGER);
                    }

                    if (reg.rendimentoMedioKgHa() != null) {
                        ps.setInt(7, reg.rendimentoMedioKgHa());
                    } else {
                        ps.setNull(7, Types.INTEGER);
                    }

                    if (reg.valorProducaoMilReais() != null) {
                        ps.setBigDecimal(8, reg.valorProducaoMilReais());
                    } else {
                        ps.setNull(8, Types.NUMERIC);
                    }
                }
        );

        int afetados = 0;
        for (int[] counts : updateCounts) {
            for (int c : counts) {
                if (c >= 0) {
                    afetados += c;
                }
            }
        }
        return afetados;
    }

    private List<PamProducaoRegistro> deduplicarLote(List<PamProducaoRegistro> registros) {
        Map<ChaveUnicaPam, PamProducaoRegistro> unicos = new LinkedHashMap<>(registros.size());
        for (PamProducaoRegistro reg : registros) {
            ChaveUnicaPam chave = new ChaveUnicaPam(
                    reg.municipioId(),
                    reg.culturaCodigoFonteId(),
                    reg.ano()
            );
            unicos.merge(chave, reg, this::mesclarRegistros);
        }
        return new ArrayList<>(unicos.values());
    }

    private PamProducaoRegistro mesclarRegistros(PamProducaoRegistro anterior, PamProducaoRegistro novo) {
        return new PamProducaoRegistro(
                novo.municipioId(),
                novo.culturaCodigoFonteId(),
                novo.ano(),
                novo.areaPlantadaHa() != null ? novo.areaPlantadaHa() : anterior.areaPlantadaHa(),
                novo.areaColhidaHa() != null ? novo.areaColhidaHa() : anterior.areaColhidaHa(),
                novo.quantidadeProduzidaTon() != null ? novo.quantidadeProduzidaTon() : anterior.quantidadeProduzidaTon(),
                novo.rendimentoMedioKgHa() != null ? novo.rendimentoMedioKgHa() : anterior.rendimentoMedioKgHa(),
                novo.valorProducaoMilReais() != null ? novo.valorProducaoMilReais() : anterior.valorProducaoMilReais()
        );
    }

    private record ChaveUnicaPam(
            Long municipioId,
            Long culturaCodigoFonteId,
            Integer ano
    ) {}
}
