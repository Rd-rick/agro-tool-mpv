import React, { useState, useMemo } from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  ResponsiveContainer,
  CartesianGrid,
} from 'recharts';
import { TrendingUp, ChevronDown } from 'lucide-react';
import { formatCurrency } from '../../utils/formatters';
import type { DashboardResponse } from '../../types/api';

interface PriceHistoryChartProps {
  data: DashboardResponse;
}

export const PriceHistoryChart: React.FC<PriceHistoryChartProps> = ({ data }) => {
  const oportunidades = data.rankingOportunidades || [];
  const [selectedCulturaId, setSelectedCulturaId] = useState<number>(() => {
    return oportunidades[0]?.culturaId ?? 1;
  });
  const [periodo, setPeriodo] = useState<'30' | '90' | '180'>('90');

  const selectedCultura =
    oportunidades.find((o) => o.culturaId === selectedCulturaId) || oportunidades[0];

  const chartData = useMemo(() => {
    const basePrice = selectedCultura?.precoAtualReferencia || 120;
    const variation = (selectedCultura?.variacao30d || 2.5) / 100;
    const pointsCount = periodo === '30' ? 6 : periodo === '90' ? 10 : 16;
    const items = [];
    const now = new Date();

    for (let i = pointsCount - 1; i >= 0; i--) {
      const d = new Date(now);
      d.setDate(d.getDate() - i * (periodo === '30' ? 5 : periodo === '90' ? 9 : 12));
      const factor = 1 - (i / pointsCount) * variation + Math.sin(i * 1.5) * 0.02;
      const val = Math.round(basePrice * factor * 100) / 100;
      items.push({
        date: d.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' }),
        preco: val,
      });
    }
    return items;
  }, [selectedCultura, periodo]);

  return (
    <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-soft flex flex-col justify-between h-full">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
        <div>
          <div className="flex items-center gap-1.5">
            <TrendingUp className="w-4 h-4 text-brand-600" />
            <h3 className="text-sm sm:text-base font-bold text-slate-900 tracking-tight">
              Histórico de preços
            </h3>
          </div>
          <span className="text-xs text-slate-600 font-medium block mt-0.5">
            {selectedCultura?.unidadeMedida || 'R$/saca 60kg'} • {selectedCultura?.fonte || 'Epagri/Cepa'}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <div className="relative">
            <select
              value={selectedCulturaId}
              onChange={(e) => setSelectedCulturaId(Number(e.target.value))}
              className="appearance-none pl-3.5 pr-8 py-1.5 text-xs font-semibold bg-brand-50 text-brand-900 rounded-full border border-brand-200 focus:outline-none cursor-pointer"
            >
              {oportunidades.map((op) => (
                <option key={op.culturaId} value={op.culturaId}>
                  {op.nomeCultura}
                </option>
              ))}
            </select>
            <ChevronDown className="w-3.5 h-3.5 text-brand-700 absolute right-2.5 top-1/2 -translate-y-1/2 pointer-events-none" />
          </div>

          <div className="flex items-center bg-slate-100 p-0.5 rounded-full border border-slate-200">
            {(['30', '90', '180'] as const).map((p) => (
              <button
                key={p}
                type="button"
                onClick={() => setPeriodo(p)}
                className={`px-2.5 py-1 text-xs font-semibold rounded-full transition-all tabular-nums ${
                  periodo === p
                    ? 'bg-white text-brand-800 shadow-sm'
                    : 'text-slate-600 hover:text-slate-900'
                }`}
              >
                {p}d
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="w-full h-44 sm:h-48">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={chartData} margin={{ top: 5, right: 10, left: -15, bottom: 0 }}>
            <defs>
              <linearGradient id="colorPreco" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
            <XAxis
              dataKey="date"
              tickLine={false}
              axisLine={false}
              tick={{ fontSize: 11, fill: '#64748b' }}
            />
            <YAxis
              tickLine={false}
              axisLine={false}
              tick={{ fontSize: 11, fill: '#64748b' }}
              domain={['auto', 'auto']}
              tickFormatter={(v) => `R$${v}`}
            />
            <Tooltip
              content={({ active, payload }) => {
                if (active && payload && payload.length) {
                  const val = payload[0].value as number;
                  return (
                    <div className="bg-slate-900 text-white px-3 py-2 rounded-xl shadow-lg text-xs">
                      <span className="text-slate-400 block mb-0.5">{payload[0].payload.date}</span>
                      <span className="font-bold text-brand-300 text-sm tabular-nums">
                        {formatCurrency(val)}
                      </span>
                    </div>
                  );
                }
                return null;
              }}
            />
            <Area
              type="monotone"
              dataKey="preco"
              stroke="#059669"
              strokeWidth={2.5}
              fillOpacity={1}
              fill="url(#colorPreco)"
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};
