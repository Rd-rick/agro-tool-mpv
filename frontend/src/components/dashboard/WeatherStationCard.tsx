import React, { useState } from 'react';
import { MapPin, Sun, Cloud, Info } from 'lucide-react';
import { cn } from '../../utils/cn';
import type { DashboardResponse } from '../../types/api';

interface WeatherStationCardProps {
  data: DashboardResponse;
}

export const WeatherStationCard: React.FC<WeatherStationCardProps> = ({ data }) => {
  const [unit, setUnit] = useState<'C' | 'F'>('C');
  const tempC = 22;
  const tempF = Math.round((tempC * 9) / 5 + 32);

  const praca = data.municipio.pracaReferenciaNome || 'Planalto Serrano / SC';
  const hoje = new Intl.DateTimeFormat('pt-BR', {
    weekday: 'long',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(new Date());

  return (
    <div className="bg-white rounded-3xl p-6 border border-slate-100 shadow-soft flex flex-col justify-between relative overflow-hidden h-full">
      <div className="flex items-center justify-between mb-3">
        <div className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-brand-600 text-white text-xs font-semibold shadow-sm">
          <MapPin className="w-3.5 h-3.5" />
          <span>{data.municipio.nome} - SC</span>
        </div>

        <div className="flex items-center bg-slate-100 p-0.5 rounded-full border border-slate-200">
          <button
            type="button"
            onClick={() => setUnit('C')}
            className={cn(
              'px-2.5 py-0.5 rounded-full text-xs font-semibold transition-all',
              unit === 'C' ? 'bg-brand-600 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'
            )}
          >
            °C
          </button>
          <button
            type="button"
            onClick={() => setUnit('F')}
            className={cn(
              'px-2.5 py-0.5 rounded-full text-xs font-semibold transition-all',
              unit === 'F' ? 'bg-brand-600 text-white shadow-sm' : 'text-slate-600 hover:text-slate-900'
            )}
          >
            °F
          </button>
        </div>
      </div>

      <div className="flex items-start justify-between">
        <div>
          <span className="capitalize text-xs font-medium text-slate-500">{hoje}</span>
          <div className="flex items-baseline gap-2 mt-1">
            <span className="text-4xl sm:text-5xl font-bold tracking-tight text-slate-900 tabular-nums">
              {unit === 'C' ? `${tempC}°C` : `${tempF}°F`}
            </span>
          </div>
          <p className="text-xs text-slate-600 mt-1 font-medium">Praça de referência: {praca}</p>
        </div>

        <div className="flex flex-col items-center">
          <div className="relative w-16 h-16 flex items-center justify-center">
            <Sun className="w-10 h-10 text-amber-500 animate-spin-slow absolute top-1 right-1" />
            <Cloud className="w-12 h-12 text-slate-400 absolute bottom-1 left-1" />
          </div>
          <span className="text-xs font-medium text-slate-700 mt-1">Parcialmente nublado</span>
        </div>
      </div>

      <div className="mt-4 pt-3.5 border-t border-slate-100 flex items-center gap-2 text-xs text-slate-600 leading-relaxed">
        <Info className="w-4 h-4 text-brand-600 shrink-0" />
        <span>
          Previsão meteorológica integrada INMET em fase de expansão. Exibindo parâmetros médios ZARC.
        </span>
      </div>
    </div>
  );
};
