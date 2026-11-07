import React from 'react';
import { ArrowUpRight, Sprout, TrendingUp, DollarSign, Calendar } from 'lucide-react';
import type { DashboardResponse } from '../../types/api';

interface QuickMetricsListProps {
  data: DashboardResponse;
  onNavigateCulturas: () => void;
  onNavigateComparador: () => void;
}

export const QuickMetricsList: React.FC<QuickMetricsListProps> = ({
  data,
  onNavigateCulturas,
  onNavigateComparador,
}) => {
  const culturasCount = data.culturasPossiveis?.length || 0;
  const oportunidadesEmAlta =
    data.rankingOportunidades?.filter((o) => (o.variacao30d || 0) > 0).length || 0;
  const decendiosTotal =
    data.indicadoresRisco?.reduce((acc, curr) => acc + (curr.totalDecendiosFavoraveis || 0), 0) || 0;

  const items = [
    {
      label: 'Culturas aptas',
      count: culturasCount,
      icon: Sprout,
      action: onNavigateCulturas,
    },
    {
      label: 'Em alta (30 dias)',
      count: oportunidadesEmAlta,
      icon: TrendingUp,
      action: onNavigateCulturas,
    },
    {
      label: 'Decêndios ZARC',
      count: decendiosTotal,
      icon: Calendar,
      action: onNavigateCulturas,
    },
    {
      label: 'Comparador',
      count: 'Multicritério',
      icon: DollarSign,
      action: onNavigateComparador,
    },
  ];

  return (
    <div className="flex flex-col gap-3 h-full justify-between">
      {items.map((item, idx) => {
        const Icon = item.icon;
        return (
          <div
            key={idx}
            onClick={item.action}
            className="bg-white rounded-2xl p-4 border border-slate-100 shadow-soft flex items-center justify-between hover:border-brand-200 transition-all cursor-pointer group"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-slate-50 flex items-center justify-center text-slate-500 group-hover:bg-brand-50 group-hover:text-brand-600 transition-colors">
                <Icon className="w-4 h-4" />
              </div>
              <span className="text-xs font-semibold text-slate-700 group-hover:text-slate-900 transition-colors">
                {item.label}
              </span>
            </div>

            <div className="flex items-center gap-3">
              <span className="text-base sm:text-lg font-bold text-slate-900 tabular-nums">
                {item.count}
              </span>
              <div className="w-8 h-8 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-center text-slate-400 group-hover:bg-brand-600 group-hover:text-white group-hover:border-brand-600 transition-all shadow-sm">
                <ArrowUpRight className="w-4 h-4" />
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
};
