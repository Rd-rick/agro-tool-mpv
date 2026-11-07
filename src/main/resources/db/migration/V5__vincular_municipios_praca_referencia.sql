WITH mapeamento_microrregiao (cod_micro, nome_micro, nome_praca) AS (
    VALUES
        ('001', 'São Miguel do Oeste', 'Extremo Oeste'),
        ('002', 'Chapecó', 'Oeste'),
        ('003', 'Xanxerê', 'Oeste'),
        ('004', 'Joaçaba', 'Alto Vale do Rio do Peixe'),
        ('005', 'Concórdia', 'Meio Oeste'),
        ('006', 'Canoinhas', 'Planalto Norte'),
        ('007', 'São Bento do Sul', 'Planalto Norte'),
        ('008', 'Joinville', 'Litoral Norte'),
        ('009', 'Curitibanos', 'Meio Oeste'),
        ('010', 'Campos de Lages', 'Planalto Sul'),
        ('011', 'Rio do Sul', 'Alto Vale do Itajaí'),
        ('012', 'Blumenau', 'Litoral Norte'),
        ('013', 'Itajaí', 'Litoral Norte'),
        ('014', 'Ituporanga', 'Alto Vale do Itajaí'),
        ('015', 'Tijucas', 'Grande Florianópolis'),
        ('016', 'Florianópolis', 'Grande Florianópolis'),
        ('017', 'Tabuleiro', 'Grande Florianópolis'),
        ('018', 'Tubarão', 'Litoral Sul'),
        ('019', 'Criciúma', 'Litoral Sul'),
        ('020', 'Araranguá', 'Litoral Sul')
)
UPDATE tb_municipio m
SET praca_referencia_id = rp.id,
    observacao_praca = FORMAT(
        'Praça regional atribuída por microrregião IBGE %s (%s) -> %s (Epagri/Cepa)',
        mm.cod_micro,
        mm.nome_micro,
        mm.nome_praca
    )
FROM mapeamento_microrregiao mm
JOIN tb_regiao_praca rp ON rp.nome = mm.nome_praca AND rp.uf = 'SC'
WHERE m.uf = 'SC'
  AND m.codigo_microrregiao_ibge_legado = mm.cod_micro;
