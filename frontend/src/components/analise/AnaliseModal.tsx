import React, { useState, useEffect } from 'react';
import {
  X,
  Sprout,
  ShieldCheck,
  TrendingUp,
  TrendingDown,
  AlertCircle,
  Award,
  Layers,
  Calendar,
  DollarSign,
  Loader2,
} from 'lucide-react';
import { getAnaliseCultura } from '../../api/endpoints';
import { formatCurrency, formatPercent, formatNumber, formatarSolosListaTexto } from '../../utils/formatters';
import { DecendioMatrix } from '../culturas/DecendioMatrix';
import type { AnaliseCulturaResponse } from '../../types/api';
import { cn } from '../../utils/cn';

interface AnaliseModalProps {
  culturaId: number | null;
  codigoIbge: string;
  mesInicio: string;
  mesFim: string;
  soloCodigo?: string;
  areaHa?: string;
  initialTab?: 'zoneamento' | 'mercado' | 'decisao';
  scrollToTabua?: boolean;
  onClose: () => void;
}

export const AnaliseModal: React.FC<AnaliseModalProps> = ({
  culturaId,
  codigoIbge,
  mesInicio,
  mesFim,
  soloCodigo,
  areaHa,
  initialTab,
  scrollToTabua,
  onClose,
}) => {
  const [data, setData] = useState<AnaliseCulturaResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'zoneamento' | 'mercado' | 'decisao'>(initialTab || 'zoneamento');

  useEffect(() => {
    if (initialTab) {
      setActiveTab(initialTab);
    }
  }, [initialTab, culturaId]);

  useEffect(() => {
    if (scrollToTabua && !loading && data?.zoneamento?.matrizDecendios) {
      const timer = setTimeout(() => {
        const el = document.getElementById('tabua-risco-section');
        if (el) {
          el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [scrollToTabua, loading, data]);

  useEffect(() => {
    if (!culturaId) return;
    let mounted = true;
    setLoading(true);
    setError(null);

    getAnaliseCultura(codigoIbge, culturaId, { mesInicio, mesFim, soloCodigo, areaHa })
      .then((res) => {
        if (mounted) setData(res);
      })
      .catch((err) => {
        if (mounted) setError(err.message || 'Falha ao carregar análise');
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [culturaId, codigoIbge, mesInicio, mesFim, soloCodigo, areaHa]);

  if (!culturaId) return null;

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl max-w-4xl w-full max-h-[90vh] shadow-2xl flex flex-col overflow-hidden border border-slate-100">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600 shadow-sm">
              <Sprout className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-xl font-bold text-slate-900">
                  {data?.nomeCultura || 'Análise da cultura'}
                </h2>
                <span className="text-xs px-2.5 py-1 rounded-full bg-brand-50 text-brand-700 font-semibold border border-brand-200/50">
                  {data?.grupoAgricola || 'Grãos'}
                </span>
              </div>
              <p className="text-xs text-slate-500 italic mt-0.5">{data?.nomeCientifico || 'Espécie vegetal'}</p>
            </div>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="w-9 h-9 rounded-full bg-white border border-slate-200 text-slate-500 hover:text-slate-900 flex items-center justify-center transition-colors shadow-sm"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="flex items-center gap-2 px-0 md:px-6 pt-4 border-b border-slate-100">
          <button
            type="button"
            onClick={() => setActiveTab('zoneamento')}
            className={`pb-3 px-3 text-xs sm:text-sm font-semibold border-b-2 transition-all flex items-center gap-1.5 ${
              activeTab === 'zoneamento'
                ? 'border-brand-600 text-brand-600 font-bold'
                : 'border-transparent text-slate-600 hover:text-slate-900'
            }`}
          >
            <ShieldCheck className="w-4 h-4" />
            <span>Zoneamento ZARC</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('mercado')}
            className={`pb-3 px-3 text-xs sm:text-sm font-semibold border-b-2 transition-all flex items-center gap-1.5 ${
              activeTab === 'mercado'
                ? 'border-brand-600 text-brand-600 font-bold'
                : 'border-transparent text-slate-600 hover:text-slate-900'
            }`}
          >
            <DollarSign className="w-4 h-4" />
            <span>Mercado & cotações</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('decisao')}
            className={`pb-3 px-3 text-xs sm:text-sm font-semibold border-b-2 transition-all flex items-center gap-1.5 ${
              activeTab === 'decisao'
                ? 'border-brand-600 text-brand-600 font-bold'
                : 'border-transparent text-slate-600 hover:text-slate-900'
            }`}
          >
            <Award className="w-4 h-4" />
            <span>Motor de decisão & PAM</span>
          </button>
        </div>

        <div className="p-6 overflow-y-auto flex-1 space-y-6">
          {loading ? (
            <div className="py-20 flex flex-col items-center justify-center text-slate-500 gap-3">
              <Loader2 className="w-8 h-8 animate-spin text-brand-600" />
              <p className="text-sm font-semibold">Carregando detalhes agronômicos e de mercado...</p>
            </div>
          ) : error ? (
            <div className="py-12 flex flex-col items-center justify-center text-rose-600 gap-2">
              <AlertCircle className="w-8 h-8" />
              <p className="text-sm font-semibold">{error}</p>
            </div>
          ) : data ? (
            <>
              {activeTab === 'zoneamento' && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100">
                      <span className="text-xs font-semibold text-slate-500 block mb-1">Situação ZARC</span>
                      <span className="text-sm font-bold text-slate-900 block">{data.zoneamento.situacao}</span>
                      <p className="text-xs text-brand-700 mt-1 font-semibold">
                        Risco mínimo: {data.zoneamento.riscoMinimo}%
                      </p>
                    </div>

                    <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100">
                      <span className="text-xs font-semibold text-slate-500 block mb-1">Melhor período</span>
                      <span className="text-sm font-bold text-slate-900 block">
                        {data.zoneamento.recomendacaoMelhorPeriodo || 'Conforme janela decendial'}
                      </span>
                    </div>

                    <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100">
                      <span className="text-xs font-semibold text-slate-500 block mb-1">Solos compatíveis</span>
                      <span className="text-sm font-bold text-slate-900 block">
                        {formatarSolosListaTexto(data.zoneamento.tiposSolo)}
                      </span>
                    </div>
                  </div>

                  {data.zoneamento.matrizDecendios && (
                    <div id="tabua-risco-section">
                      <DecendioMatrix matriz={data.zoneamento.matrizDecendios} />
                    </div>
                  )}

                  <div className="bg-slate-50/70 rounded-2xl p-5 border border-slate-100 space-y-2">
                    <h4 className="text-xs sm:text-sm font-bold text-slate-900 flex items-center gap-1.5">
                      <Calendar className="w-4 h-4 text-brand-600" />
                      <span>Ciclos agrícolas e portarias oficiais</span>
                    </h4>
                    <div className="flex flex-wrap gap-2 text-xs">
                      {data.zoneamento.ciclos?.map((c) => (
                        <span key={c.codigo} className="px-3 py-1.5 bg-white rounded-lg border border-slate-200 font-medium text-slate-800">
                          <strong>{c.codigo}:</strong> {c.descricao}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {activeTab === 'mercado' && (
                <div className="space-y-6">
                  <div className="grid grid-cols-1 sm:grid-cols-4 gap-4">
                    <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100">
                      <span className="text-xs font-semibold text-slate-500 block mb-1">Preço atual</span>
                      <span className="text-lg font-bold text-slate-900 block tabular-nums">
                        {formatCurrency(data.mercado.precoReferenciaAtual)}
                      </span>
                      <span className="text-xs text-slate-500 block mt-0.5">{data.mercado.unidadeMedida}</span>
                    </div>

                    <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100">
                      <span className="text-xs font-semibold text-slate-500 block mb-1">Variação 30 dias</span>
                      <span className={cn(
                          'text-base font-bold text-brand-700 block tabular-nums',
                          (data.mercado.variacao30d ?? 0) >= 0 ? 'text-emerald-700' : 'text-rose-600'
                        )}
                      >
                        {formatPercent(data.mercado.variacao30d)}
                      </span>
                    </div>

                    <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100">
                      <span className="text-xs font-semibold text-slate-500 block mb-1">Variação 90 dias</span>
                      <span className={cn(
                          'text-base font-bold text-slate-800 block tabular-nums',
                          (data.mercado.variacao90d ?? 0) >= 0 ? 'text-emerald-700' : 'text-rose-600'
                        )}
                      >
                        {formatPercent(data.mercado.variacao90d)}
                      </span>
                    </div>

                    <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100">
                      <span className="text-xs font-semibold text-slate-500 block mb-1">Praça & fonte</span>
                      <span className="text-xs font-bold text-slate-800 block truncate">
                        {data.mercado.pracaReferencia || 'Estadual SC'}
                      </span>
                      <span className="text-xs text-slate-500 font-medium block mt-0.5">
                        {data.mercado.fonteReferencia || 'Epagri/Cepa'}
                      </span>
                    </div>
                  </div>

                  <div className="bg-slate-50 rounded-2xl p-5 border border-slate-100">
                    <h4 className="text-xs sm:text-sm font-bold text-slate-900 mb-2 flex items-center gap-1.5">
                      {(data.mercado.tendencia === 'DESVALORIZACAO' ||
                        (data.mercado.variacao30d !== null && data.mercado.variacao30d !== undefined
                          ? data.mercado.variacao30d < 0
                          : (data.mercado.variacao90d ?? 0) < 0)) ? (
                        <TrendingDown className="w-4 h-4 text-rose-600" />
                      ) : (
                        <TrendingUp className="w-4 h-4 text-brand-600" />
                      )}
                      <span>Comportamento recente de mercado</span>
                    </h4>
                    <p className="text-xs sm:text-sm text-slate-700 leading-relaxed">
                      {data.analise.comportamentoPrecosUltimosMeses ||
                        'Preços com dinâmica estável em Santa Catarina, refletindo parâmetros de comercialização regional.'}
                    </p>
                  </div>
                </div>
              )}

              {activeTab === 'decisao' && (
                <div className="space-y-6">
                  <div className="bg-emerald-50/60 rounded-3xl p-6 border border-emerald-100 flex flex-col md:flex-row items-start md:items-center justify-between gap-6 shadow-sm">
                    <div>
                      <div className="flex items-center gap-1.5 text-xs font-bold text-emerald-800 uppercase tracking-wider">
                        <Award className="w-4 h-4 text-emerald-700" />
                        <span>Motor de decisão Agro Tool</span>
                      </div>
                      <div className="text-4xl font-black text-emerald-950 mt-1 tabular-nums">
                        {data.analise.scoreOportunidade.notaFinal} <span className="text-base font-semibold text-emerald-700">/ 100</span>
                      </div>
                      <span className="text-xs text-emerald-800 font-semibold block mt-1">
                        {data.analise.scoreOportunidade.posicaoRanking}ª posição no ranking regional
                      </span>
                    </div>

                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-3 w-full md:w-auto text-left text-xs bg-white/80 p-4 rounded-2xl border border-emerald-200/50">
                      <div>
                        <span className="text-[11px] text-slate-500 block">Clima ZARC</span>
                        <span className="font-bold text-slate-900 tabular-nums text-sm">
                          {data.analise.scoreOportunidade.segurancaClimatica} <span className="text-[10px] text-slate-400 font-normal">/ 40</span>
                        </span>
                      </div>
                      <div>
                        <span className="text-[11px] text-slate-500 block">Janela</span>
                        <span className="font-bold text-slate-900 tabular-nums text-sm">
                          {data.analise.scoreOportunidade.amplitudeJanela} <span className="text-[10px] text-slate-400 font-normal">/ 15</span>
                        </span>
                      </div>
                      <div>
                        <span className="text-[11px] text-slate-500 block">Preços Epagri</span>
                        <span className="font-bold text-slate-900 tabular-nums text-sm">
                          {data.analise.scoreOportunidade.dinamicaPrecos} <span className="text-[10px] text-slate-400 font-normal">/ 20</span>
                        </span>
                      </div>
                      <div>
                        <span className="text-[11px] text-slate-500 block">Receita Bruta</span>
                        <span className="font-bold text-emerald-700 tabular-nums text-sm">
                          {data.analise.scoreOportunidade.potencialReceita ?? 0} <span className="text-[10px] text-slate-400 font-normal">/ 10</span>
                        </span>
                      </div>
                      <div>
                        <span className="text-[11px] text-slate-500 block">Qualidade Info</span>
                        <span className="font-bold text-slate-900 tabular-nums text-sm">
                          {data.analise.scoreOportunidade.qualidadeInformacao} <span className="text-[10px] text-slate-400 font-normal">/ 15</span>
                        </span>
                      </div>
                    </div>
                  </div>

                  {(() => {
                    const projecao = data.analise.projecaoEconomica || data.analise.scoreOportunidade.projecaoEconomica;
                    if (!projecao) return null;

                    const isCalculado = projecao.status === 'CALCULADO_COM_SUCESSO';

                    return (
                      <div className="bg-white rounded-3xl p-6 border border-slate-200/90 shadow-soft space-y-4">
                        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
                          <div className="flex items-center gap-2">
                            <div className="w-9 h-9 rounded-xl bg-brand-50 text-brand-600 flex items-center justify-center">
                              <DollarSign className="w-5 h-5" />
                            </div>
                            <div>
                              <h4 className="text-sm font-bold text-slate-900">Projeção da Produção e Receita Bruta</h4>
                              <p className="text-xs text-slate-500">Estimativa com base no IBGE/PAM e cotação recebida pelo produtor (Epagri/Cepa)</p>
                            </div>
                          </div>
                          <span className={`text-xs px-3 py-1 rounded-full font-bold ${
                            isCalculado ? 'bg-emerald-50 text-emerald-800 border border-emerald-200' : 'bg-slate-100 text-slate-600'
                          }`}>
                            {isCalculado ? 'Projeção Ativa' : 'Sob Consulta'}
                          </span>
                        </div>

                        {isCalculado ? (
                          <>
                            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3 text-xs sm:text-sm">
                              <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-100">
                                <span className="text-xs text-slate-500 block mb-0.5">Área informada</span>
                                <span className="font-bold text-slate-900 tabular-nums text-base">
                                  {projecao.areaInformadaHa} <span className="text-xs font-normal text-slate-500">ha</span>
                                </span>
                              </div>

                              <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-100">
                                <span className="text-xs text-slate-500 block mb-0.5">Produtividade estimada</span>
                                <span className="font-bold text-slate-900 tabular-nums text-base">
                                  {formatNumber(projecao.produtividadeEstimadaPorHa)} <span className="text-xs font-normal text-slate-500">{projecao.unidadeComercializacao}/ha</span>
                                </span>
                              </div>

                              <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-100">
                                <span className="text-xs text-slate-500 block mb-0.5">Produção total projetada</span>
                                <span className="font-bold text-brand-700 tabular-nums text-base">
                                  {formatNumber(projecao.producaoTotalEstimada)} <span className="text-xs font-normal text-brand-600">{projecao.unidadeComercializacao}</span>
                                </span>
                              </div>

                              <div className="bg-slate-50 p-3.5 rounded-2xl border border-slate-100">
                                <span className="text-xs text-slate-500 block mb-0.5">Preço recebido (ref.)</span>
                                <span className="font-bold text-slate-900 tabular-nums text-base">
                                  {formatCurrency(projecao.precoReferencia)} <span className="text-xs font-normal text-slate-500">/{projecao.unidadeComercializacao}</span>
                                </span>
                              </div>

                              <div className="bg-emerald-50/70 p-3.5 rounded-2xl border border-emerald-100 col-span-2 sm:col-span-3 lg:col-span-4">
                                <span className="text-xs text-emerald-800 font-semibold block mb-0.5">Receita bruta estimada por hectare</span>
                                <span className="font-black text-emerald-900 tabular-nums text-lg">
                                  {formatCurrency(projecao.receitaBrutaPorHa)} <span className="text-xs font-medium text-emerald-700">/ ha</span>
                                </span>
                              </div>

                              <div className="bg-emerald-50/70 p-3.5 rounded-2xl border border-emerald-100 col-span-2 sm:col-span-3 lg:col-span-4">
                                <span className="text-xs text-emerald-800 font-semibold block mb-0.5">Receita bruta total ({projecao.areaInformadaHa} ha)</span>
                                <span className="font-black text-emerald-900 tabular-nums text-lg">
                                  {formatCurrency(projecao.receitaBrutaTotal)}
                                </span>
                              </div>
                            </div>

                            <div className="p-4 bg-amber-50/90 border border-amber-200/90 rounded-2xl flex items-start gap-3">
                              <AlertCircle className="w-5 h-5 text-amber-700 shrink-0 mt-0.5" />
                              <div className="text-xs text-amber-900 leading-relaxed space-y-1">
                                <span className="font-bold block">Aviso metodológico obrigatório:</span>
                                <p>
                                  {projecao.avisoMetodologico ||
                                    'A receita bruta representa a projeção do faturamento estimado da colheita e não desconta os custos de insumos (sementes, defensivos, adubação) nem operações agrícolas. Não representa lucro líquido.'}
                                </p>
                              </div>
                            </div>
                          </>
                        ) : (
                          <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200 text-xs text-slate-600 flex items-center gap-3">
                            <AlertCircle className="w-5 h-5 text-slate-400 shrink-0" />
                            <p>
                              {projecao.avisoMetodologico ||
                                'Projeção de receita bruta indisponível para esta cultura devido à ausência de dados de rendimento no município ou cotação ativa da Epagri/Cepa.'}
                            </p>
                          </div>
                        )}
                      </div>
                    );
                  })()}

                  {data.analise.produtividadeMunicipal && data.analise.produtividadeMunicipal.possuiDadosPam && (
                    <div className="bg-slate-50 rounded-2xl p-5 border border-slate-100">
                      <h4 className="text-xs sm:text-sm font-bold text-slate-900 mb-3 flex items-center gap-1.5">
                        <Layers className="w-4 h-4 text-brand-600" />
                        <span>Histórico agrícola municipal (IBGE / PAM)</span>
                      </h4>
                      <div className="grid grid-cols-3 gap-4 text-xs sm:text-sm">
                        <div>
                          <span className="text-xs text-slate-500 block mb-0.5">Último rendimento</span>
                          <span className="font-bold text-slate-800 tabular-nums">
                            {formatNumber(data.analise.produtividadeMunicipal.ultimoRendimentoMedioKgHa)} kg/ha
                          </span>
                        </div>
                        <div>
                          <span className="text-xs text-slate-500 block mb-0.5">Média histórica</span>
                          <span className="font-bold text-slate-800 tabular-nums">
                            {formatNumber(data.analise.produtividadeMunicipal.rendimentoMedioHistoricoKgHa)} kg/ha
                          </span>
                        </div>
                        <div>
                          <span className="text-xs text-slate-500 block mb-0.5">Ano de referência</span>
                          <span className="font-bold text-slate-800 tabular-nums">
                            {data.analise.produtividadeMunicipal.ultimoAno || 2024}
                          </span>
                        </div>
                      </div>
                    </div>
                  )}

                  {data.analise.scoreOportunidade.justificativas && (
                    <div className="space-y-2">
                      <span className="text-xs sm:text-sm font-bold text-slate-900 block">Justificativas do algoritmo:</span>
                      <ul className="list-disc pl-5 text-xs sm:text-sm text-slate-700 space-y-1">
                        {data.analise.scoreOportunidade.justificativas.map((j, i) => (
                          <li key={i}>{j}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}
            </>
          ) : null}
        </div>
      </div>
    </div>
  );
};
