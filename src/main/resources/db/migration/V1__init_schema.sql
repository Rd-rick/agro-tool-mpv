CREATE TABLE tb_regiao_praca (
    id BIGSERIAL CONSTRAINT pk_tb_regiao_praca PRIMARY KEY,
    nome VARCHAR(100) NOT NULL CONSTRAINT uk_tb_regiao_praca_nome UNIQUE,
    uf CHAR(2) NOT NULL DEFAULT 'SC',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_regiao_praca IS 'Praças e macrorregiões de referência de preços agrícolas monitoradas pela Epagri/Cepa em Santa Catarina';
COMMENT ON COLUMN tb_regiao_praca.nome IS 'Nome da praça regional (ex: Meio Oeste, Planalto Sul, Alto Vale do Itajaí)';

CREATE TABLE tb_municipio (
    id BIGSERIAL CONSTRAINT pk_tb_municipio PRIMARY KEY,
    codigo_ibge VARCHAR(7) NOT NULL CONSTRAINT uk_tb_municipio_codigo_ibge UNIQUE,
    nome VARCHAR(150) NOT NULL,
    uf CHAR(2) NOT NULL DEFAULT 'SC',
    praca_referencia_id BIGINT CONSTRAINT fk_tb_municipio_praca REFERENCES tb_regiao_praca(id),
    codigo_mesorregiao_ibge_legado VARCHAR(10),
    codigo_microrregiao_ibge_legado VARCHAR(10),
    observacao_praca TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tb_municipio_nome_uf ON tb_municipio (nome, uf);
CREATE INDEX idx_tb_municipio_praca ON tb_municipio (praca_referencia_id);

COMMENT ON TABLE tb_municipio IS 'Municípios catarinenses com código IBGE de 7 dígitos e vinculação regional de mercado';
COMMENT ON COLUMN tb_municipio.codigo_mesorregiao_ibge_legado IS 'Código da mesorregião da divisão antiga do IBGE (1989-2017), ainda referenciada pelo ZARC (Cod_Meso)';
COMMENT ON COLUMN tb_municipio.codigo_microrregiao_ibge_legado IS 'Código da microrregião da divisão antiga do IBGE (1989-2017), ainda referenciada pelo ZARC (Cod_Micro)';
COMMENT ON COLUMN tb_municipio.observacao_praca IS 'Registro técnico da regra de associação regional com a praça Epagri';

CREATE TABLE tb_cultura (
    id BIGSERIAL CONSTRAINT pk_tb_cultura PRIMARY KEY,
    nome VARCHAR(100) NOT NULL CONSTRAINT uk_tb_cultura_nome UNIQUE,
    nome_cientifico VARCHAR(150),
    grupo_agricola VARCHAR(50),
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE tb_cultura IS 'Entidades canônicas de culturas agrícolas do Agro Tool (ex: Soja, Milho, Feijão, Trigo)';
COMMENT ON COLUMN tb_cultura.nome_cientifico IS 'Nomenclatura botânica binomial oficial da espécie agrícola';

CREATE TABLE tb_cultura_codigo_fonte (
    id BIGSERIAL CONSTRAINT pk_tb_cultura_codigo_fonte PRIMARY KEY,
    cultura_id BIGINT NOT NULL CONSTRAINT fk_tb_cultura_codigo_fonte_cultura REFERENCES tb_cultura(id),
    fonte VARCHAR(30) NOT NULL,
    codigo_origem VARCHAR(50) NOT NULL,
    nome_origem VARCHAR(150),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_tb_cultura_codigo_fonte UNIQUE (fonte, codigo_origem)
);

CREATE INDEX idx_tb_cultura_codigo_fonte_cultura ON tb_cultura_codigo_fonte (cultura_id);

COMMENT ON TABLE tb_cultura_codigo_fonte IS 'Mapeia identificadores de fontes externas (como Milho 1ª safra e Milho 2ª safra do ZARC) para a cultura canônica';

CREATE TABLE tb_produto_cotacao (
    id BIGSERIAL CONSTRAINT pk_tb_produto_cotacao PRIMARY KEY,
    cultura_id BIGINT CONSTRAINT fk_tb_produto_cotacao_cultura REFERENCES tb_cultura(id),
    nome_bruto_origem VARCHAR(200) NOT NULL,
    nome_comercial VARCHAR(100) NOT NULL,
    unidade_comercializacao VARCHAR(30) NOT NULL,
    peso_embalagem_kg NUMERIC(8, 2),
    especificacao VARCHAR(100),
    fonte VARCHAR(50) NOT NULL DEFAULT 'EPAGRI_CEPA',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_tb_produto_cotacao_fonte_nome UNIQUE (fonte, nome_bruto_origem)
);

CREATE INDEX idx_tb_produto_cotacao_cultura ON tb_produto_cotacao (cultura_id);

COMMENT ON TABLE tb_produto_cotacao IS 'Produtos agropecuários com nomenclatura original preservada e atributos comerciais normalizados';
COMMENT ON COLUMN tb_produto_cotacao.peso_embalagem_kg IS 'Peso líquido padronizado em kg. NULL para produtos com peso variável (ex: caixas de 20 a 23 kg) ou vendidos por unidade';

CREATE TABLE tb_zarc_zoneamento (
    id BIGSERIAL CONSTRAINT pk_tb_zarc_zoneamento PRIMARY KEY,
    municipio_id BIGINT NOT NULL CONSTRAINT fk_tb_zarc_municipio REFERENCES tb_municipio(id),
    cultura_codigo_fonte_id BIGINT NOT NULL CONSTRAINT fk_tb_zarc_cultura_codigo_fonte REFERENCES tb_cultura_codigo_fonte(id),
    safra_ini INTEGER NOT NULL,
    safra_fim INTEGER NOT NULL,
    ciclo_codigo VARCHAR(20) NOT NULL,
    ciclo_descricao VARCHAR(100),
    solo_codigo VARCHAR(20) NOT NULL,
    solo_descricao VARCHAR(100),
    manejo_codigo VARCHAR(20) NOT NULL,
    manejo_descricao VARCHAR(50),
    portaria VARCHAR(100) NOT NULL,

    dec_01 SMALLINT NOT NULL,
    dec_02 SMALLINT NOT NULL,
    dec_03 SMALLINT NOT NULL,
    dec_04 SMALLINT NOT NULL,
    dec_05 SMALLINT NOT NULL,
    dec_06 SMALLINT NOT NULL,
    dec_07 SMALLINT NOT NULL,
    dec_08 SMALLINT NOT NULL,
    dec_09 SMALLINT NOT NULL,
    dec_10 SMALLINT NOT NULL,
    dec_11 SMALLINT NOT NULL,
    dec_12 SMALLINT NOT NULL,
    dec_13 SMALLINT NOT NULL,
    dec_14 SMALLINT NOT NULL,
    dec_15 SMALLINT NOT NULL,
    dec_16 SMALLINT NOT NULL,
    dec_17 SMALLINT NOT NULL,
    dec_18 SMALLINT NOT NULL,
    dec_19 SMALLINT NOT NULL,
    dec_20 SMALLINT NOT NULL,
    dec_21 SMALLINT NOT NULL,
    dec_22 SMALLINT NOT NULL,
    dec_23 SMALLINT NOT NULL,
    dec_24 SMALLINT NOT NULL,
    dec_25 SMALLINT NOT NULL,
    dec_26 SMALLINT NOT NULL,
    dec_27 SMALLINT NOT NULL,
    dec_28 SMALLINT NOT NULL,
    dec_29 SMALLINT NOT NULL,
    dec_30 SMALLINT NOT NULL,
    dec_31 SMALLINT NOT NULL,
    dec_32 SMALLINT NOT NULL,
    dec_33 SMALLINT NOT NULL,
    dec_34 SMALLINT NOT NULL,
    dec_35 SMALLINT NOT NULL,
    dec_36 SMALLINT NOT NULL,

    tem_janela_favoravel BOOLEAN GENERATED ALWAYS AS (
        dec_01 IN (20, 30, 40) OR dec_02 IN (20, 30, 40) OR dec_03 IN (20, 30, 40) OR dec_04 IN (20, 30, 40) OR
        dec_05 IN (20, 30, 40) OR dec_06 IN (20, 30, 40) OR dec_07 IN (20, 30, 40) OR dec_08 IN (20, 30, 40) OR
        dec_09 IN (20, 30, 40) OR dec_10 IN (20, 30, 40) OR dec_11 IN (20, 30, 40) OR dec_12 IN (20, 30, 40) OR
        dec_13 IN (20, 30, 40) OR dec_14 IN (20, 30, 40) OR dec_15 IN (20, 30, 40) OR dec_16 IN (20, 30, 40) OR
        dec_17 IN (20, 30, 40) OR dec_18 IN (20, 30, 40) OR dec_19 IN (20, 30, 40) OR dec_20 IN (20, 30, 40) OR
        dec_21 IN (20, 30, 40) OR dec_22 IN (20, 30, 40) OR dec_23 IN (20, 30, 40) OR dec_24 IN (20, 30, 40) OR
        dec_25 IN (20, 30, 40) OR dec_26 IN (20, 30, 40) OR dec_27 IN (20, 30, 40) OR dec_28 IN (20, 30, 40) OR
        dec_29 IN (20, 30, 40) OR dec_30 IN (20, 30, 40) OR dec_31 IN (20, 30, 40) OR dec_32 IN (20, 30, 40) OR
        dec_33 IN (20, 30, 40) OR dec_34 IN (20, 30, 40) OR dec_35 IN (20, 30, 40) OR dec_36 IN (20, 30, 40)
    ) STORED,

    risco_minimo SMALLINT GENERATED ALWAYS AS (
        LEAST(
            NULLIF(dec_01, 0), NULLIF(dec_02, 0), NULLIF(dec_03, 0), NULLIF(dec_04, 0),
            NULLIF(dec_05, 0), NULLIF(dec_06, 0), NULLIF(dec_07, 0), NULLIF(dec_08, 0),
            NULLIF(dec_09, 0), NULLIF(dec_10, 0), NULLIF(dec_11, 0), NULLIF(dec_12, 0),
            NULLIF(dec_13, 0), NULLIF(dec_14, 0), NULLIF(dec_15, 0), NULLIF(dec_16, 0),
            NULLIF(dec_17, 0), NULLIF(dec_18, 0), NULLIF(dec_19, 0), NULLIF(dec_20, 0),
            NULLIF(dec_21, 0), NULLIF(dec_22, 0), NULLIF(dec_23, 0), NULLIF(dec_24, 0),
            NULLIF(dec_25, 0), NULLIF(dec_26, 0), NULLIF(dec_27, 0), NULLIF(dec_28, 0),
            NULLIF(dec_29, 0), NULLIF(dec_30, 0), NULLIF(dec_31, 0), NULLIF(dec_32, 0),
            NULLIF(dec_33, 0), NULLIF(dec_34, 0), NULLIF(dec_35, 0), NULLIF(dec_36, 0)
        )
    ) STORED,
    
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_tb_zarc_zoneamento UNIQUE (
        municipio_id, cultura_codigo_fonte_id, safra_ini, safra_fim, ciclo_codigo, solo_codigo, manejo_codigo, portaria
    )
);

CREATE INDEX idx_tb_zarc_busca_apto ON tb_zarc_zoneamento (municipio_id, safra_fim, tem_janela_favoravel);
CREATE INDEX idx_tb_zarc_cultura_fonte ON tb_zarc_zoneamento (cultura_codigo_fonte_id);

COMMENT ON TABLE tb_zarc_zoneamento IS 'Tábua oficial do ZARC identificada pelo código de origem e portaria com indicadores calculados';
COMMENT ON COLUMN tb_zarc_zoneamento.tem_janela_favoravel IS 'Coluna gerada: TRUE se pelo menos um decêndio possui risco 20, 30 ou 40%';
COMMENT ON COLUMN tb_zarc_zoneamento.risco_minimo IS 'Coluna gerada: menor valor de risco favorável (> 0). NULL se todos forem 0 (inaptidão total)';

-- 7. SÉRIES HISTÓRICAS DE COTAÇÕES DE PREÇOS
CREATE TABLE tb_cotacao_preco (
    id BIGSERIAL CONSTRAINT pk_tb_cotacao_preco PRIMARY KEY,
    produto_id BIGINT NOT NULL CONSTRAINT fk_tb_cotacao_produto REFERENCES tb_produto_cotacao(id),
    praca_id BIGINT NOT NULL CONSTRAINT fk_tb_cotacao_praca REFERENCES tb_regiao_praca(id),
    data_cotacao DATE NOT NULL,
    preco_minimo NUMERIC(12, 2),
    preco_referencia NUMERIC(12, 2) NOT NULL,
    preco_maximo NUMERIC(12, 2),
    frequencia VARCHAR(20) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_tb_cotacao_preco UNIQUE (produto_id, praca_id, data_cotacao, frequencia)
);

CREATE INDEX idx_tb_cotacao_serie_temporal ON tb_cotacao_preco (produto_id, praca_id, frequencia, data_cotacao DESC);
CREATE INDEX idx_tb_cotacao_data ON tb_cotacao_preco (data_cotacao DESC);

COMMENT ON TABLE tb_cotacao_preco IS 'Cotações de preços físicos agropecuários com segregação de granularidade diária e semanal';
COMMENT ON COLUMN tb_cotacao_preco.data_cotacao IS 'Data oficial da cotação registrada na fonte (data da observação ou data de referência divulgada pela Epagri)';
