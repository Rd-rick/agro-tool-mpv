export interface MunicipioItem {
  nome: string;
  uf: string;
  codigoIbge: string;
  pracaReferenciaId: number | null;
  observacaoPraca: string | null;
}

export interface ListarMunicipiosResponse {
  municipios: MunicipioItem[];
}

export interface SoloResumo {
  codigo: string;
  tipoSolo: string;
  classificacao: string;
  teorArgila: string;
  descricao: string;
}

export interface CicloResumo {
  codigo: string;
  descricao: string;
}

export interface JanelaPlantioResumo {
  decendio: number;
  mes: number;
  nomeMes: string;
  descricaoPeriodo: string;
  menorRisco: number | null;
  riscosDisponiveis: number[];
}

export type StatusProjecaoProducao =
  | 'CALCULADO_COM_SUCESSO'
  | 'INDISPONIVEL_SEM_HISTORICO_MUNICIPAL'
  | 'INDISPONIVEL_SEM_COTACAO'
  | 'INDISPONIVEL_UNIDADE_VARIAVEL';

export interface ProjecaoProducaoEconomica {
  areaInformadaHa: number | null;
  rendimentoHistoricoKgHa: number | null;
  pesoEmbalagemKg: number | null;
  unidadeComercializacao: string | null;
  produtividadeEstimadaPorHa: number | null;
  producaoTotalEstimada: number | null;
  precoReferencia: number | null;
  receitaBrutaPorHa: number | null;
  receitaBrutaTotal: number | null;
  status: StatusProjecaoProducao;
  avisoMetodologico: string | null;
}

export interface CriteriosPontuacao {
  segurancaClimatica: number;
  amplitudeJanela: number;
  dinamicaPrecos: number;
  potencialReceita: number;
  qualidadeInformacao: number;
  notaFinal: number;
}

export interface OportunidadeDashboard {
  culturaId: number;
  nomeCultura: string;
  nomeCientifico: string;
  grupoAgricola: string;
  posicaoRanking: number;
  notaFinal: number;
  criteriosPontuacao: CriteriosPontuacao;
  riscoMinimoZarc: number | null;
  quantidadeDecendiosFavoraveis: number;
  quantidadeSolosAptos: number;
  quantidadeCiclosAptos: number;
  melhorPeriodoPlantio: JanelaPlantioResumo | null;
  recomendacaoMelhorPeriodo: string | null;
  soloProjecao: SoloResumo | null;
  solosAptos: SoloResumo[];
  produtoReferencia: string | null;
  precoAtualReferencia: number | null;
  unidadeMedida: string | null;
  dataUltimaCotacao: string | null;
  variacao30d: number | null;
  variacao90d: number | null;
  variacao180d: number | null;
  praca: string | null;
  fonte: string | null;
  fallbackEstadual: boolean;
  possuiHistoricoPam: boolean;
  ultimoAnoPam: number | null;
  rendimentoMedioPamKgHa: number | null;
  projecaoEconomica: ProjecaoProducaoEconomica | null;
  justificativas: string[];
  avisosMetodologicos: string[];
}

export interface CulturaPossivelDashboard {
  culturaId: number;
  nomeCultura: string;
  nomeCientifico: string;
  grupoAgricola: string;
  situacao: string;
  epocaPlantio: string;
  melhorPeriodoPlantio: JanelaPlantioResumo | null;
  recomendacaoMelhorPeriodo: string | null;
  soloProjecao: SoloResumo | null;
  riscoMinimo: number | null;
  niveisRiscoDisponiveis: number[];
  janelasPlantio: JanelaPlantioResumo[];
  solos: SoloResumo[];
  ciclos: CicloResumo[];
}

export interface IndicadorRiscoDashboard {
  culturaId: number;
  nomeCultura: string;
  riscoMinimo: number | null;
  classificacaoRisco: string;
  niveisRiscoDisponiveis: number[];
  totalDecendiosFavoraveis: number;
  decendiosComRisco20: number;
  decendiosComRisco30: number;
  decendiosComRisco40: number;
}

export interface MunicipioDashboard {
  codigoIbge: string;
  nome: string;
  uf: string;
  pracaReferenciaId: number | null;
  pracaReferenciaNome: string | null;
  observacaoRegional: string | null;
}

export interface DashboardResponse {
  municipio: MunicipioDashboard;
  mesInicio: string;
  mesFim: string;
  soloSelecionado: SoloResumo | null;
  safraIni: number;
  safraFim: number;
  safra: string;
  areaHa: number | null;
  melhorOportunidade: OportunidadeDashboard | null;
  culturasPossiveis: CulturaPossivelDashboard[];
  rankingOportunidades: OportunidadeDashboard[];
  indicadoresRisco: IndicadorRiscoDashboard[];
  avisosGerais: string[];
}

export interface PontoGraficoPreco {
  data: string;
  precoMinimo: number | null;
  precoReferencia: number | null;
  precoMaximo: number | null;
}

export interface SeriePeriodo {
  diasJanela: number;
  dataInicio: string;
  dataFim: string;
  precoInicial: number | null;
  precoFinal: number | null;
  precoMinimoPeriodo: number | null;
  precoMaximoPeriodo: number | null;
  precoMedioPeriodo: number | null;
  variacaoPercentual: number | null;
  pontos: PontoGraficoPreco[];
}

export interface OrigemCotacao {
  fonte: string;
  pracaId: number | null;
  pracaNome: string | null;
  fallbackEstadual: boolean;
  observacaoOrigem: string | null;
}

export interface ProdutoFonteCotacao {
  produtoId: number;
  nomeComercial: string;
  especificacao: string | null;
  unidadeComercializacao: string;
  pesoEmbalagemKg: number | null;
  frequenciaUtilizada: string;
  dataUltimaCotacao: string | null;
  precoAtualReferencia: number | null;
  variacao30d: number | null;
  variacao90d: number | null;
  variacao180d: number | null;
  origem: OrigemCotacao;
  serie30Dias: SeriePeriodo | null;
  serie90Dias: SeriePeriodo | null;
  serie180Dias: SeriePeriodo | null;
}

export interface DecendioMatriz {
  decendio: number;
  mes: number;
  nomeMes: string;
  decendioNoMes: number;
  diaInicio: number;
  diaFim: number;
  descricaoPeriodo: string;
  favoravel: boolean;
  menorRisco: number | null;
  riscosDisponiveis: number[];
}

export interface AlertaVariacao {
  tipoAlerta: string;
  severidade: string;
  janelaReferencia: string;
  percentualVariacaoOuOscilacao: number | null;
  descricao: string;
}

export interface RegistroAnualPam {
  ano: number;
  areaPlantadaHa: number | null;
  quantidadeProduzidaTon: number | null;
  rendimentoMedioKgHa: number | null;
}

export interface ProdutividadeMunicipal {
  possuiDadosPam: boolean;
  ultimoAno: number | null;
  ultimoRendimentoMedioKgHa: number | null;
  rendimentoMedioHistoricoKgHa: number | null;
  ultimaAreaPlantadaHa: number | null;
  ultimaQuantidadeProduzidaTon: number | null;
  serieHistoricaAnual: RegistroAnualPam[];
  avisoCustosProducao: string | null;
}

export interface ScoreResumoAnalise {
  posicaoRanking: number;
  notaFinal: number;
  segurancaClimatica: number;
  amplitudeJanela: number;
  dinamicaPrecos: number;
  potencialReceita: number;
  qualidadeInformacao: number;
  recomendacaoMelhorPeriodo: string | null;
  projecaoEconomica?: ProjecaoProducaoEconomica | null;
  justificativas: string[];
}

export interface AnaliseCulturaResponse {
  culturaId: number;
  nomeCultura: string;
  nomeCientifico: string;
  grupoAgricola: string;
  zoneamento: {
    municipio: MunicipioDashboard;
    safraIni: number;
    safraFim: number;
    safra: string;
    soloSelecionado: SoloResumo | null;
    aptaZarc: boolean;
    situacao: string;
    recomendacaoMelhorPeriodo: string | null;
    riscoMinimo: number | null;
    niveisRiscoDisponiveis: number[];
    cultivaresENomesOrigem: string[];
    ciclos: CicloResumo[];
    tiposSolo: SoloResumo[];
    portarias: string[];
    matrizDecendios: DecendioMatriz[];
  };
  mercado: {
    produtoReferencia: string | null;
    precoReferenciaAtual: number | null;
    unidadeMedida: string | null;
    dataUltimaCotacao: string | null;
    pracaReferencia: string | null;
    fonteReferencia: string | null;
    fallbackEstadual: boolean;
    tendencia: string;
    variacao30d: number | null;
    variacao90d: number | null;
    variacao180d: number | null;
    cotacoesEpagriCepa: ProdutoFonteCotacao[];
    cotacoesConab: ProdutoFonteCotacao[];
  };
  analise: {
    scoreOportunidade: ScoreResumoAnalise;
    comportamentoPrecosUltimosMeses: string;
    alertasVariacoesRelevantes: AlertaVariacao[];
    produtividadeMunicipal: ProdutividadeMunicipal;
    projecaoEconomica?: ProjecaoProducaoEconomica | null;
    avisosMetodologicos: string[];
  };
}

export interface CulturaComparada {
  culturaId: number;
  nomeCultura: string;
  nomeCientifico: string;
  grupoAgricola: string;
  viabilidadeZoneamento: {
    aptaZarc: boolean;
    situacao: string;
    quantidadeSolosAptos: number;
    quantidadeCiclosAptos: number;
    tiposSolo: SoloResumo[];
    ciclos: CicloResumo[];
  };
  janelasPlantio: {
    quantidadeDecendiosFavoraveis: number;
    epocaPlantioResumo: string;
    recomendacaoMelhorPeriodo: string | null;
  };
  riscoClimatico: {
    riscoMinimo: number | null;
    classificacaoRisco: string;
    niveisRiscoDisponiveis: number[];
    decendiosRisco20: number;
    decendiosRisco30: number;
    decendiosRisco40: number;
  };
  precosEVariacao: {
    produtoReferencia: string | null;
    precoAtualReferencia: number | null;
    unidadeMedida: string | null;
    dataUltimaCotacao: string | null;
    pracaReferencia: string | null;
    fonteReferencia: string | null;
    fallbackEstadual: boolean;
    variacao30d: number | null;
    variacao90d: number | null;
    variacao180d: number | null;
  };
  scoreMotorDecisao: {
    elegivelRanking: boolean;
    posicaoRankingMunicipio: number | null;
    notaFinal: number;
    segurancaClimatica: number;
    amplitudeJanela: number;
    dinamicaPrecos: number;
    potencialReceita: number;
    qualidadeInformacao: number;
    justificativas: string[];
  };
  indicadoresProdutivos: {
    possuiHistoricoPam: boolean;
    ultimoAnoPam: number | null;
    ultimoRendimentoMedioKgHa: number | null;
    rendimentoMedioHistoricoKgHa: number | null;
    avisoPotencialEconomico: string | null;
  };
}

export interface CompararCulturasResponse {
  municipio: MunicipioDashboard;
  soloSelecionado: SoloResumo | null;
  safraIni: number;
  safraFim: number;
  safra: string;
  culturas: CulturaComparada[];
  avisosComparacao: string[];
}

export interface ChamadaFerramenta {
  ferramenta: string;
  argumentos: Record<string, unknown>;
}

export interface RespostaAssistenteResponse {
  resposta: string;
  ferramentasUtilizadas: ChamadaFerramenta[];
  fontesCitadas: string[];
}

export interface PerguntaAssistenteRequest {
  texto: string;
  codigoIbge: string;
  mesInicio?: string;
  mesFim?: string;
  tipoSolo?: string;
  areaHectares?: number | null;
}

export interface JobExecutionResponse {
  sucesso: boolean;
  mensagem: string;
  registrosProcessados?: number;
  detalhes?: string;
}
