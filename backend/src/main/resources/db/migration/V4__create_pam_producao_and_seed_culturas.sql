CREATE TABLE tb_pam_producao (
    id BIGSERIAL CONSTRAINT pk_tb_pam_producao PRIMARY KEY,
    municipio_id BIGINT NOT NULL CONSTRAINT fk_tb_pam_municipio REFERENCES tb_municipio(id),
    cultura_codigo_fonte_id BIGINT NOT NULL CONSTRAINT fk_tb_pam_cultura_codigo_fonte REFERENCES tb_cultura_codigo_fonte(id),
    ano INTEGER NOT NULL,
    area_plantada_ha INTEGER,
    area_colhida_ha INTEGER,
    quantidade_produzida_ton INTEGER,
    rendimento_medio_kg_ha INTEGER,
    valor_producao_mil_reais NUMERIC(14, 2),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_tb_pam_producao UNIQUE (municipio_id, cultura_codigo_fonte_id, ano)
);

CREATE INDEX idx_tb_pam_municipio_ano ON tb_pam_producao (municipio_id, ano DESC);
CREATE INDEX idx_tb_pam_cultura_fonte ON tb_pam_producao (cultura_codigo_fonte_id);

INSERT INTO tb_cultura_codigo_fonte (cultura_id, fonte, codigo_origem, nome_origem) VALUES
((SELECT id FROM tb_cultura WHERE nome = 'Soja'), 'IBGE_PAM', '40124', 'Soja (em grão)'),
((SELECT id FROM tb_cultura WHERE nome = 'Milho'), 'IBGE_PAM', '40122', 'Milho (em grão)'),
((SELECT id FROM tb_cultura WHERE nome = 'Feijão'), 'IBGE_PAM', '40112', 'Feijão (em grão)'),
((SELECT id FROM tb_cultura WHERE nome = 'Trigo'), 'IBGE_PAM', '40127', 'Trigo (em grão)'),
((SELECT id FROM tb_cultura WHERE nome = 'Arroz'), 'IBGE_PAM', '40102', 'Arroz (em casca)'),
((SELECT id FROM tb_cultura WHERE nome = 'Cebola'), 'IBGE_PAM', '40107', 'Cebola'),
((SELECT id FROM tb_cultura WHERE nome = 'Alho'), 'IBGE_PAM', '40100', 'Alho'),
((SELECT id FROM tb_cultura WHERE nome = 'Aveia'), 'IBGE_PAM', '40103', 'Aveia (em grão)'),
((SELECT id FROM tb_cultura WHERE nome = 'Tomate'), 'IBGE_PAM', '40126', 'Tomate')
ON CONFLICT (fonte, codigo_origem) DO NOTHING;
