INSERT INTO tb_regiao_praca (nome, uf) VALUES
('Alto Vale do Itajaí', 'SC'),
('Alto Vale do Rio do Peixe', 'SC'),
('Extremo Oeste', 'SC'),
('Grande Florianópolis', 'SC'),
('Litoral Norte', 'SC'),
('Litoral Sul', 'SC'),
('Meio Oeste', 'SC'),
('Oeste', 'SC'),
('Planalto Norte', 'SC'),
('Planalto Sul', 'SC');

INSERT INTO tb_cultura (nome, nome_cientifico, grupo_agricola, descricao) VALUES
('Soja', 'Glycine max', 'GRAOS', 'Cultura granífera de verão de alta liquidez comercial e exportação'),
('Milho', 'Zea mays', 'GRAOS', 'Milho grão comercializado para ração animal, indústria e exportação (1ª e 2ª safras)'),
('Feijão', 'Phaseolus vulgaris', 'GRAOS', 'Feijão comum (tipos comerciais preto, carioca e vermelho)'),
('Trigo', 'Triticum aestivum', 'GRAOS', 'Cereal de inverno para panificação, moagem e ração'),
('Arroz', 'Oryza sativa', 'GRAOS', 'Arroz irrigado em várzea com alta produtividade no litoral e vales'),
('Cebola', 'Allium cepa', 'HORTALICAS', 'Hortaliça de bulbo de grande destaque socioeconômico no Alto Vale e Planalto de SC'),
('Alho', 'Allium sativum', 'HORTALICAS', 'Hortaliça nobre de inverno cultivada no Meio-Oeste e Planalto'),
('Aveia', 'Avena sativa', 'GRAOS', 'Cereal de inverno utilizado para cobertura de solo, forragem e grãos'),
('Tomate', 'Solanum lycopersicum', 'HORTALICAS', 'Tomate de mesa tutorado comercializado nas Centrais de Abastecimento');

INSERT INTO tb_cultura_codigo_fonte (cultura_id, fonte, codigo_origem, nome_origem) VALUES
((SELECT id FROM tb_cultura WHERE nome = 'Soja'), 'MAPA_ZARC', '12012010000011', 'Soja'),
((SELECT id FROM tb_cultura WHERE nome = 'Milho'), 'MAPA_ZARC', '12011010000011', 'Milho 1ª safra'),
((SELECT id FROM tb_cultura WHERE nome = 'Milho'), 'MAPA_ZARC', '12011010000021', 'Milho 2ª safra'),
((SELECT id FROM tb_cultura WHERE nome = 'Feijão'), 'MAPA_ZARC', '12013560000011', 'Feijão'),
((SELECT id FROM tb_cultura WHERE nome = 'Trigo'), 'MAPA_ZARC', '12011030000011', 'Trigo'),
((SELECT id FROM tb_cultura WHERE nome = 'Arroz'), 'MAPA_ZARC', '12010900000011', 'Arroz irrigado'),
((SELECT id FROM tb_cultura WHERE nome = 'Aveia'), 'MAPA_ZARC', '12011000000031', 'Aveia');

INSERT INTO tb_produto_cotacao (cultura_id, nome_bruto_origem, nome_comercial, unidade_comercializacao, peso_embalagem_kg, especificacao, fonte) VALUES
((SELECT id FROM tb_cultura WHERE nome = 'Soja'), 'Soja industrial - sc 60 kg', 'Soja industrial', 'Saca', 60.00, 'industrial', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Milho'), 'Milho-amarelo - sc 60 kg', 'Milho amarelo', 'Saca', 60.00, 'amarelo tipo 1', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Feijão'), 'Feijão-preto - sc 60 kg', 'Feijão preto', 'Saca', 60.00, 'tipo 1 preto', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Feijão'), 'Feijão-carioca - sc 60 kg', 'Feijão carioca', 'Saca', 60.00, 'tipo 1 carioca', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Feijão'), 'Feijão-vermelho - sc 60 kg', 'Feijão vermelho', 'Saca', 60.00, 'vermelho', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Trigo'), 'Trigo superior PH78 - sc 60 kg', 'Trigo superior PH78', 'Saca', 60.00, 'superior PH78', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Arroz'), 'Arroz irrigado em casca - sc 50 kg', 'Arroz irrigado em casca', 'Saca', 50.00, 'em casca', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Cebola'), 'Cebola pera - SC classe 3 a 5 (pgto 35 dias) - sc 20 kg', 'Cebola pera', 'Saca', 20.00, 'classe 3 a 5', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Tomate'), 'Tomate longa vida extra A - cx 20 a 23 kg', 'Tomate longa vida extra A', 'Caixa', NULL, 'extra A (cx 20 a 23 kg)', 'EPAGRI_CEPA'),
((SELECT id FROM tb_cultura WHERE nome = 'Tomate'), 'Tomate longa vida extra AA - cx 20 a 23 kg', 'Tomate longa vida extra AA', 'Caixa', NULL, 'extra AA (cx 20 a 23 kg)', 'EPAGRI_CEPA');
