import React, { useState, useEffect } from 'react';
import {
  X,
  Sprout,
  ShieldCheck,
  TrendingUp,
  AlertCircle,
  Award,
  Layers,
  Calendar,
  DollarSign,
  Loader2,
} from 'lucide-react';
import { getAnaliseCultura } from '../../api/endpoints';
import { formatCurrency, formatPercent, formatNumber } from '../../utils/formatters';
import { DecendioMatrix } from '../culturas/DecendioMatrix';
import type { AnaliseCulturaResponse } from '../../types/api';

interface AnaliseModalProps {
  culturaId: number | null;
  codigoIbge: string;
  mesInicio: string;
  mesFim: string;
  soloCodigo?: string;
  onClose: () => void;
}

export const AnaliseModal: React.FC<AnaliseModalProps> = ({
  culturaId,
  codigoIbge,
  mesInicio,
  mesFim,
  soloCodigo,
  onClose,
}) => {
  const [data, setData] = useState<AnaliseCulturaResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'zoneamento' | 'mercado' | 'decisao'>('zoneamento');

  useEffect(() => {
    if (!culturaId) return;
    let mounted = true;
    setLoading(true);
    setError(null);

    getAnaliseCultura(codigoIbge, culturaId, { mesInicio, mesFim, soloCodigo })
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
  }, [culturaId, codigoIbge, mesInicio, mesFim, soloCodigo]);

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

        <div className="flex items-center gap-2 px-6 pt-4 border-b border-slate-100">
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
                        {data.zoneamento.tiposSolo?.map((s) => s.tipoSolo).join(', ') || 'Todos'}
                      </span>
                    </div>
                  </div>

                  {data.zoneamento.matrizDecendios && (
                    <DecendioMatrix matriz={data.zoneamento.matrizDecendios} />
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
                      <span className="text-base font-bold text-brand-700 block tabular-nums">
                        {formatPercent(data.mercado.variacao30d)}
                      </span>
                    </div>

                    <div className="bg-slate-50 rounded-2xl p-4 border border-slate-100">
                      <span className="text-xs font-semibold text-slate-500 block mb-1">Variação 90 dias</span>
                      <span className="text-base font-bold text-slate-800 block tabular-nums">
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
                      <TrendingUp className="w-4 h-4 text-brand-600" />
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
                  <div className="bg-emerald-50/60 rounded-2xl p-5 border border-emerald-100 flex items-center justify-between">
                    <div>
                      <span className="text-xs font-bold text-emerald-800 block">
                        Pontuação no motor de decisão
                      </span>
                      <div className="text-3xl font-bold text-emerald-950 mt-1 tabular-nums">
                        {data.analise.scoreOportunidade.notaFinal} <span className="text-sm font-semibold text-emerald-700">/ 100</span>
                      </div>
                      <span className="text-xs text-emerald-800 font-medium block mt-0.5">
                        {data.analise.scoreOportunidade.posicaoRanking}ª posição no ranking municipal
                      </span>
                    </div>

                    <div className="grid grid-cols-2 gap-3 text-right text-xs">
                      <div>
                        <span className="text-xs text-slate-500 block">Segurança climática</span>
                        <span className="font-bold text-slate-800 tabular-nums">
                          {data.analise.scoreOportunidade.segurancaClimatica} pts
                        </span>
                      </div>
                      <div>
                        <span className="text-xs text-slate-500 block">Amplitude janela</span>
                        <span className="font-bold text-slate-800 tabular-nums">
                          {data.analise.scoreOportunidade.amplitudeJanela} pts
                        </span>
                      </div>
                      <div>
                        <span className="text-xs text-slate-500 block">Dinâmica preços</span>
                        <span className="font-bold text-slate-800 tabular-nums">
                          {data.analise.scoreOportunidade.dinamicaPrecos} pts
                        </span>
                      </div>
                      <div>
                        <span className="text-xs text-slate-500 block">Qualidade info</span>
                        <span className="font-bold text-slate-800 tabular-nums">
                          {data.analise.scoreOportunidade.qualidadeInformacao} pts
                        </span>
                      </div>
                    </div>
                  </div>

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
