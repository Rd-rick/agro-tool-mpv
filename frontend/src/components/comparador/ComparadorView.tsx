import React, { useState, useEffect } from 'react';
import { getComparador } from '../../api/endpoints';
import { formatCurrency, formatPercent, formatNumber } from '../../utils/formatters';
import { BadgeRisk } from '../common/BadgeRisk';
import type { CompararCulturasResponse, CulturaPossivelDashboard } from '../../types/api';
import { Cpu, Check, AlertCircle, Loader2 } from 'lucide-react';

interface ComparadorViewProps {
  codigoIbge: string;
  mesInicio: string;
  mesFim: string;
  soloCodigo?: string;
  culturasDisponiveis: CulturaPossivelDashboard[];
  selectedIds: number[];
  onToggleCompare: (id: number) => void;
}

export const ComparadorView: React.FC<ComparadorViewProps> = ({
  codigoIbge,
  mesInicio,
  mesFim,
  soloCodigo,
  culturasDisponiveis,
  selectedIds,
  onToggleCompare,
}) => {
  const [data, setData] = useState<CompararCulturasResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (selectedIds.length < 2) {
      setData(null);
      return;
    }

    let mounted = true;
    setLoading(true);
    setError(null);

    getComparador({
      codigoIbge,
      mesInicio,
      mesFim,
      soloCodigo,
      culturaIds: selectedIds.slice(0, 3),
    })
      .then((res) => {
        if (mounted) setData(res);
      })
      .catch((err) => {
        if (mounted) setError(err.message || 'Falha ao comparar culturas');
      })
      .finally(() => {
        if (mounted) setLoading(false);
      });

    return () => {
      mounted = false;
    };
  }, [codigoIbge, mesInicio, mesFim, soloCodigo, selectedIds]);

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-soft">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
          <div>
            <h3 className="text-base font-bold text-slate-900 tracking-tight flex items-center gap-2">
              <Cpu className="w-5 h-5 text-brand-600" />
              Comparador Multicritério de Culturas
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Selecione 2 ou 3 culturas para contrastar zoneamento ZARC, riscos, preços Epagri e rendimento PAM.
            </p>
          </div>

          <span className="text-xs font-semibold px-3 py-1 rounded-full bg-slate-100 text-slate-700 self-start sm:self-auto">
            {selectedIds.length} de 3 selecionadas
          </span>
        </div>

        <div className="flex flex-wrap gap-2 pt-2">
          {culturasDisponiveis.map((c) => {
            const isSelected = selectedIds.includes(c.culturaId);
            return (
              <button
                key={c.culturaId}
                type="button"
                onClick={() => onToggleCompare(c.culturaId)}
                className={`px-3 py-1.5 rounded-full text-xs font-bold transition-all flex items-center gap-1.5 ${
                  isSelected
                    ? 'bg-brand-600 text-white shadow-sm'
                    : 'bg-slate-100 text-slate-700 hover:bg-slate-200/70'
                }`}
              >
                {isSelected && <Check className="w-3.5 h-3.5" />}
                <span>{c.nomeCultura}</span>
              </button>
            );
          })}
        </div>
      </div>

      {selectedIds.length < 2 ? (
        <div className="bg-white rounded-3xl p-12 text-center border border-slate-100 shadow-soft">
          <Cpu className="w-10 h-10 text-slate-300 mx-auto mb-3" />
          <h4 className="text-sm font-bold text-slate-800">Selecione pelo menos 2 culturas</h4>
          <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
            Clique nas opções acima ou adicione culturas diretamente na aba &quot;Culturas&quot; para visualizar o comparativo completo.
          </p>
        </div>
      ) : loading ? (
        <div className="bg-white rounded-3xl p-16 text-center border border-slate-100 shadow-soft flex flex-col items-center justify-center gap-3">
          <Loader2 className="w-8 h-8 animate-spin text-brand-600" />
          <p className="text-xs font-semibold text-slate-500">Cruzando zoneamento e indicadores econômicos...</p>
        </div>
      ) : error ? (
        <div className="bg-white rounded-3xl p-8 text-center border border-slate-100 shadow-soft text-rose-600">
          <AlertCircle className="w-8 h-8 mx-auto mb-2" />
          <p className="text-xs font-semibold">{error}</p>
        </div>
      ) : data ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {data.culturas.map((c) => (
            <div
              key={c.culturaId}
              className="bg-white rounded-3xl p-6 border border-slate-100 shadow-soft flex flex-col justify-between space-y-6"
            >
              <div>
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h4 className="text-lg font-bold text-slate-900">{c.nomeCultura}</h4>
                    <span className="text-xs text-slate-500 italic">{c.nomeCientifico}</span>
                  </div>
                  <BadgeRisk risco={c.riscoClimatico.riscoMinimo} />
                </div>

                <div className="bg-emerald-50/60 rounded-2xl p-4 border border-emerald-100 mb-5">
                  <span className="text-xs font-bold text-emerald-800 block">
                    Motor de decisão
                  </span>
                  <div className="text-3xl font-bold text-emerald-950 mt-1 tabular-nums">
                    {c.scoreMotorDecisao.notaFinal} <span className="text-sm font-semibold text-emerald-700">/ 100</span>
                  </div>
                  <span className="text-xs text-emerald-800 font-semibold block mt-0.5">
                    {c.scoreMotorDecisao.posicaoRankingMunicipio
                      ? `${c.scoreMotorDecisao.posicaoRankingMunicipio}º lugar no ranking regional`
                      : 'Indicativo ZARC'}
                  </span>
                </div>

                <div className="space-y-4 text-xs sm:text-sm">
                  <div>
                    <span className="font-bold text-slate-900 block mb-1">Preço e mercado</span>
                    <div className="grid grid-cols-2 gap-2 bg-slate-50 p-3 rounded-xl border border-slate-100">
                      <div>
                        <span className="text-xs text-slate-500 block">Preço de referência</span>
                        <span className="font-bold text-slate-800 tabular-nums">
                          {formatCurrency(c.precosEVariacao.precoAtualReferencia)}
                        </span>
                      </div>
                      <div>
                        <span className="text-xs text-slate-500 block">Variação 30d</span>
                        <span className="font-bold text-brand-700 tabular-nums">
                          {formatPercent(c.precosEVariacao.variacao30d)}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div>
                    <span className="font-bold text-slate-900 block mb-1">Janela de plantio</span>
                    <div className="bg-slate-50 p-3 rounded-xl border border-slate-100 space-y-1">
                      <div className="flex justify-between">
                        <span className="text-slate-600">Decêndios favoráveis:</span>
                        <span className="font-bold text-slate-800 tabular-nums">
                          {c.janelasPlantio.quantidadeDecendiosFavoraveis}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-slate-600">Recomendação:</span>
                        <span className="font-bold text-brand-700 truncate max-w-[150px]">
                          {c.janelasPlantio.recomendacaoMelhorPeriodo || 'Safra atual'}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div>
                    <span className="font-bold text-slate-900 block mb-1">Produtividade PAM (IBGE)</span>
                    <div className="bg-slate-50 p-3 rounded-xl border border-slate-100">
                      {c.indicadoresProdutivos.possuiHistoricoPam ? (
                        <div className="flex justify-between">
                          <span className="text-slate-600">Rendimento médio:</span>
                          <span className="font-bold text-slate-800 tabular-nums">
                            {formatNumber(c.indicadoresProdutivos.ultimoRendimentoMedioKgHa)} kg/ha
                          </span>
                        </div>
                      ) : (
                        <span className="text-slate-500 text-xs">Sem histórico municipal cadastrado</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              <button
                type="button"
                onClick={() => onToggleCompare(c.culturaId)}
                className="w-full py-2.5 rounded-2xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-bold transition-colors"
              >
                Remover da Comparação
              </button>
            </div>
          ))}
        </div>
      ) : null}
    </div>
  );
};
