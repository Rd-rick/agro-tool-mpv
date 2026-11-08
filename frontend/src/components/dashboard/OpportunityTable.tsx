import React from 'react';
import { formatCurrency, formatPercent } from '../../utils/formatters';
import type { OportunidadeDashboard } from '../../types/api';
import { ArrowUpRight, ArrowDownRight, Award } from 'lucide-react';
import { cn } from '../../utils/cn';

interface OpportunityTableProps {
  oportunidades: OportunidadeDashboard[];
  onSelectCultura: (culturaId: number) => void;
  onViewAll: () => void;
}

export const OpportunityTable: React.FC<OpportunityTableProps> = ({
  oportunidades,
  onSelectCultura,
  onViewAll,
}) => {
  const topList = oportunidades.slice(0, 6);

  return (
    <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-soft flex flex-col justify-between h-full">
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <Award className="w-5 h-5 text-brand-600" />
          <div>
            <h3 className="text-base font-bold text-slate-900 tracking-tight">Ranking de oportunidades</h3>
            <p className="text-[11px] text-slate-500 font-medium">ZARC (40) · Janela (15) · Preços (20) · Receita (10) · Dados (15)</p>
          </div>
        </div>
        <button
          type="button"
          onClick={onViewAll}
          className="text-xs font-semibold text-brand-600 hover:text-brand-700 hover:underline transition-colors"
        >
          Ver todas
        </button>
      </div>

      <div className="overflow-x-auto flex-1">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="border-b border-slate-100 text-xs font-semibold text-slate-500">
              <th className="py-2.5 pl-2 font-medium">#</th>
              <th className="py-2.5 font-medium">Cultura</th>
              <th className="py-2.5 text-right font-medium">Preço / Rec. Bruta</th>
              <th className="py-2.5 text-right font-medium">30 dias</th>
              <th className="py-2.5 pr-2 text-right font-medium" title="Pontuação no motor de decisão (0 a 100 pts)">Nota</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100/70 text-xs sm:text-sm">
            {topList.map((op, idx) => {
              const isPositive = (op.variacao30d ?? 0) >= 0;
              return (
                <tr
                  key={op.culturaId}
                  onClick={() => onSelectCultura(op.culturaId)}
                  className="hover:bg-slate-50/80 cursor-pointer transition-colors group"
                >
                  <td className="py-3 pl-2 font-semibold text-slate-400 group-hover:text-brand-600 tabular-nums text-xs">
                    {String(idx + 1).padStart(2, '0')}
                  </td>
                  <td className="py-3 font-semibold text-slate-800 group-hover:text-brand-600">
                    <div>
                      <span className="text-sm font-semibold text-slate-900 group-hover:text-brand-600 block">
                        {op.nomeCultura}
                      </span>
                      <span className="text-xs text-slate-500 font-normal truncate block max-w-[120px]">
                        {op.grupoAgricola || 'Grãos'}
                      </span>
                    </div>
                  </td>
                  <td className="py-3 text-right tabular-nums text-xs sm:text-sm">
                    <span className="font-semibold text-slate-800 block">
                      {formatCurrency(op.precoAtualReferencia)}
                    </span>
                    {op.projecaoEconomica?.status === 'CALCULADO_COM_SUCESSO' && op.projecaoEconomica.receitaBrutaPorHa ? (
                      <span className="text-[11px] font-medium text-emerald-700 block" title="Receita bruta estimada por hectare">
                        {formatCurrency(op.projecaoEconomica.receitaBrutaPorHa)}/ha
                      </span>
                    ) : (
                      <span className="text-[10px] text-slate-400 block">
                        {op.unidadeMedida || 'saca'}
                      </span>
                    )}
                  </td>
                  <td className="py-3 text-right tabular-nums text-xs">
                    <span
                      className={cn(
                        'inline-flex items-center font-semibold',
                        isPositive ? 'text-emerald-700' : 'text-rose-600'
                      )}
                    >
                      {isPositive ? (
                        <ArrowUpRight className="w-3.5 h-3.5 inline mr-0.5" />
                      ) : (
                        <ArrowDownRight className="w-3.5 h-3.5 inline mr-0.5" />
                      )}
                      {formatPercent(op.variacao30d, false)}
                    </span>
                  </td>
                  <td className="py-3 pr-2 text-right tabular-nums">
                    <span className="inline-block px-2.5 py-1 rounded-full bg-brand-50 text-brand-700 font-bold text-xs border border-brand-200/60" title={`Critérios: Clima ${op.criteriosPontuacao?.segurancaClimatica ?? 0}/40 | Janela ${op.criteriosPontuacao?.amplitudeJanela ?? 0}/15 | Preço ${op.criteriosPontuacao?.dinamicaPrecos ?? 0}/20 | Receita ${op.criteriosPontuacao?.potencialReceita ?? 0}/10 | Qualidade ${op.criteriosPontuacao?.qualidadeInformacao ?? 0}/15`}>
                      {op.notaFinal}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
