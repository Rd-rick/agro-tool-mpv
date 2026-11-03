package com.figueredo.ricardo.mvp.epagri.infrastructure.adapter.out.persistence;

import com.figueredo.ricardo.mvp.epagri.application.port.out.CotacaoPersistencePort;
import com.figueredo.ricardo.mvp.epagri.domain.model.CotacaoPrecoRegistro;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
public class CotacaoJdbcPersistenceAdapter implements CotacaoPersistencePort {

    private static final String SQL_UPSERT = """
            INSERT INTO tb_cotacao_preco (
                produto_id, praca_id, data_cotacao, preco_minimo, preco_referencia, preco_maximo, frequencia, criado_em, atualizado_em
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()
            )
            ON CONFLICT (produto_id, praca_id, data_cotacao, frequencia)
            DO UPDATE SET
                preco_minimo = EXCLUDED.preco_minimo,
                preco_referencia = EXCLUDED.preco_referencia,
                preco_maximo = EXCLUDED.preco_maximo,
                atualizado_em = NOW()
            WHERE (
                tb_cotacao_preco.preco_minimo,
                tb_cotacao_preco.preco_referencia,
                tb_cotacao_preco.preco_maximo
            ) IS DISTINCT FROM (
                EXCLUDED.preco_minimo,
                EXCLUDED.preco_referencia,
                EXCLUDED.preco_maximo
            )
            """;

    private final JdbcTemplate jdbcTemplate;

    public CotacaoJdbcPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void salvarLote(List<CotacaoPrecoRegistro> lote) {
        if (lote == null || lote.isEmpty()) {
            return;
        }

        jdbcTemplate.batchUpdate(SQL_UPSERT, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                CotacaoPrecoRegistro reg = lote.get(index);
                ps.setLong(1, reg.produtoId());
                ps.setLong(2, reg.pracaId());
                ps.setDate(3, Date.valueOf(reg.dataCotacao()));
                ps.setBigDecimal(4, reg.precoMinimo());
                ps.setBigDecimal(5, reg.precoReferencia());
                ps.setBigDecimal(6, reg.precoMaximo());
                ps.setString(7, reg.frequencia().name());
            }

            @Override
            public int getBatchSize() {
                return lote.size();
            }
        });
    }
}
