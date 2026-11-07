package com.figueredo.ricardo.mvp.zarc.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.zarc.application.port.out.ZarcPersistencePort;
import com.figueredo.ricardo.mvp.zarc.domain.model.ZarcRegistro;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ZarcJdbcPersistenceAdapter implements ZarcPersistencePort {

    private static final String SQL_UPSERT = """
            INSERT INTO tb_zarc_zoneamento (
                municipio_id, cultura_codigo_fonte_id, safra_ini, safra_fim,
                ciclo_codigo, ciclo_descricao, solo_codigo, solo_descricao,
                manejo_codigo, manejo_descricao, portaria,
                dec_01, dec_02, dec_03, dec_04, dec_05, dec_06, dec_07, dec_08, dec_09, dec_10,
                dec_11, dec_12, dec_13, dec_14, dec_15, dec_16, dec_17, dec_18, dec_19, dec_20,
                dec_21, dec_22, dec_23, dec_24, dec_25, dec_26, dec_27, dec_28, dec_29, dec_30,
                dec_31, dec_32, dec_33, dec_34, dec_35, dec_36
            ) VALUES (
                ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?
            )
            ON CONFLICT (municipio_id, cultura_codigo_fonte_id, safra_ini, safra_fim, ciclo_codigo, solo_codigo, manejo_codigo, portaria)
            DO UPDATE SET
                ciclo_descricao = EXCLUDED.ciclo_descricao,
                solo_descricao = EXCLUDED.solo_descricao,
                manejo_descricao = EXCLUDED.manejo_descricao,
                dec_01 = EXCLUDED.dec_01,
                dec_02 = EXCLUDED.dec_02,
                dec_03 = EXCLUDED.dec_03,
                dec_04 = EXCLUDED.dec_04,
                dec_05 = EXCLUDED.dec_05,
                dec_06 = EXCLUDED.dec_06,
                dec_07 = EXCLUDED.dec_07,
                dec_08 = EXCLUDED.dec_08,
                dec_09 = EXCLUDED.dec_09,
                dec_10 = EXCLUDED.dec_10,
                dec_11 = EXCLUDED.dec_11,
                dec_12 = EXCLUDED.dec_12,
                dec_13 = EXCLUDED.dec_13,
                dec_14 = EXCLUDED.dec_14,
                dec_15 = EXCLUDED.dec_15,
                dec_16 = EXCLUDED.dec_16,
                dec_17 = EXCLUDED.dec_17,
                dec_18 = EXCLUDED.dec_18,
                dec_19 = EXCLUDED.dec_19,
                dec_20 = EXCLUDED.dec_20,
                dec_21 = EXCLUDED.dec_21,
                dec_22 = EXCLUDED.dec_22,
                dec_23 = EXCLUDED.dec_23,
                dec_24 = EXCLUDED.dec_24,
                dec_25 = EXCLUDED.dec_25,
                dec_26 = EXCLUDED.dec_26,
                dec_27 = EXCLUDED.dec_27,
                dec_28 = EXCLUDED.dec_28,
                dec_29 = EXCLUDED.dec_29,
                dec_30 = EXCLUDED.dec_30,
                dec_31 = EXCLUDED.dec_31,
                dec_32 = EXCLUDED.dec_32,
                dec_33 = EXCLUDED.dec_33,
                dec_34 = EXCLUDED.dec_34,
                dec_35 = EXCLUDED.dec_35,
                dec_36 = EXCLUDED.dec_36,
                atualizado_em = NOW()
            WHERE (
                tb_zarc_zoneamento.ciclo_descricao, tb_zarc_zoneamento.solo_descricao, tb_zarc_zoneamento.manejo_descricao,
                tb_zarc_zoneamento.dec_01, tb_zarc_zoneamento.dec_02, tb_zarc_zoneamento.dec_03, tb_zarc_zoneamento.dec_04,
                tb_zarc_zoneamento.dec_05, tb_zarc_zoneamento.dec_06, tb_zarc_zoneamento.dec_07, tb_zarc_zoneamento.dec_08,
                tb_zarc_zoneamento.dec_09, tb_zarc_zoneamento.dec_10, tb_zarc_zoneamento.dec_11, tb_zarc_zoneamento.dec_12,
                tb_zarc_zoneamento.dec_13, tb_zarc_zoneamento.dec_14, tb_zarc_zoneamento.dec_15, tb_zarc_zoneamento.dec_16,
                tb_zarc_zoneamento.dec_17, tb_zarc_zoneamento.dec_18, tb_zarc_zoneamento.dec_19, tb_zarc_zoneamento.dec_20,
                tb_zarc_zoneamento.dec_21, tb_zarc_zoneamento.dec_22, tb_zarc_zoneamento.dec_23, tb_zarc_zoneamento.dec_24,
                tb_zarc_zoneamento.dec_25, tb_zarc_zoneamento.dec_26, tb_zarc_zoneamento.dec_27, tb_zarc_zoneamento.dec_28,
                tb_zarc_zoneamento.dec_29, tb_zarc_zoneamento.dec_30, tb_zarc_zoneamento.dec_31, tb_zarc_zoneamento.dec_32,
                tb_zarc_zoneamento.dec_33, tb_zarc_zoneamento.dec_34, tb_zarc_zoneamento.dec_35, tb_zarc_zoneamento.dec_36
            ) IS DISTINCT FROM (
                EXCLUDED.ciclo_descricao, EXCLUDED.solo_descricao, EXCLUDED.manejo_descricao,
                EXCLUDED.dec_01, EXCLUDED.dec_02, EXCLUDED.dec_03, EXCLUDED.dec_04,
                EXCLUDED.dec_05, EXCLUDED.dec_06, EXCLUDED.dec_07, EXCLUDED.dec_08,
                EXCLUDED.dec_09, EXCLUDED.dec_10, EXCLUDED.dec_11, EXCLUDED.dec_12,
                EXCLUDED.dec_13, EXCLUDED.dec_14, EXCLUDED.dec_15, EXCLUDED.dec_16,
                EXCLUDED.dec_17, EXCLUDED.dec_18, EXCLUDED.dec_19, EXCLUDED.dec_20,
                EXCLUDED.dec_21, EXCLUDED.dec_22, EXCLUDED.dec_23, EXCLUDED.dec_24,
                EXCLUDED.dec_25, EXCLUDED.dec_26, EXCLUDED.dec_27, EXCLUDED.dec_28,
                EXCLUDED.dec_29, EXCLUDED.dec_30, EXCLUDED.dec_31, EXCLUDED.dec_32,
                EXCLUDED.dec_33, EXCLUDED.dec_34, EXCLUDED.dec_35, EXCLUDED.dec_36
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public ZarcJdbcPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public int salvarLote(List<ZarcRegistro> registros) {
        if (registros == null || registros.isEmpty()) {
            return 0;
        }

        List<ZarcRegistro> loteUnico = deduplicarLote(registros);

        int[][] updateCounts = jdbcTemplate.batchUpdate(
                SQL_UPSERT,
                loteUnico,
                loteUnico.size(),
                (ps, reg) -> {
                    ps.setLong(1, reg.municipioId());
                    ps.setLong(2, reg.culturaCodigoFonteId());
                    ps.setInt(3, reg.safraIni());
                    ps.setInt(4, reg.safraFim());
                    ps.setString(5, reg.cicloCodigo());
                    if (reg.cicloDescricao() != null) {
                        ps.setString(6, reg.cicloDescricao());
                    } else {
                        ps.setNull(6, Types.VARCHAR);
                    }
                    ps.setString(7, reg.soloCodigo());
                    if (reg.soloDescricao() != null) {
                        ps.setString(8, reg.soloDescricao());
                    } else {
                        ps.setNull(8, Types.VARCHAR);
                    }
                    ps.setString(9, reg.manejoCodigo());
                    if (reg.manejoDescricao() != null) {
                        ps.setString(10, reg.manejoDescricao());
                    } else {
                        ps.setNull(10, Types.VARCHAR);
                    }
                    ps.setString(11, reg.portaria());

                    int paramIndex = 12;
                    for (Integer decendio : reg.decendios()) {
                        ps.setShort(paramIndex++, decendio.shortValue());
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

    private List<ZarcRegistro> deduplicarLote(List<ZarcRegistro> registros) {
        Map<ChaveUnicaZarc, ZarcRegistro> unicos = new LinkedHashMap<>(registros.size());
        for (ZarcRegistro reg : registros) {
            ChaveUnicaZarc chave = new ChaveUnicaZarc(
                    reg.municipioId(),
                    reg.culturaCodigoFonteId(),
                    reg.safraIni(),
                    reg.safraFim(),
                    reg.cicloCodigo(),
                    reg.soloCodigo(),
                    reg.manejoCodigo(),
                    reg.portaria()
            );
            unicos.put(chave, reg);
        }
        return new ArrayList<>(unicos.values());
    }

    private record ChaveUnicaZarc(
            Long municipioId,
            Long culturaCodigoFonteId,
            Integer safraIni,
            Integer safraFim,
            String cicloCodigo,
            String soloCodigo,
            String manejoCodigo,
            String portaria
    ) {}
}
