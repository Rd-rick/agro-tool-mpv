import React, { useState } from 'react';
import { Globe, ExternalLink, Satellite } from 'lucide-react';
import type { DashboardResponse } from '../../types/api';

interface CitySatelliteMapCardProps {
  data: DashboardResponse;
}

export const CitySatelliteMapCard: React.FC<CitySatelliteMapCardProps> = ({ data }) => {
  const [mapMode, setMapMode] = useState<'k' | 'h'>('k');

  const cidade = data.municipio.nome || 'Santa Catarina';
  const query = `${cidade}, Santa Catarina, Brasil`;
  const mapSrc = `https://maps.google.com/maps?q=${encodeURIComponent(query)}&t=${mapMode}&z=12&ie=UTF8&iwloc=&output=embed`;
  const externalMapUrl = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(query)}`;

  return (
    <div className="bg-white rounded-3xl p-5 border border-slate-100 shadow-soft flex flex-col justify-between h-full relative overflow-hidden group">
      <div className="flex items-center justify-between gap-2 mb-3 z-10">
        <div className="flex items-center bg-slate-100 p-0.5 rounded-full border border-slate-200">
          <button
            type="button"
            onClick={() => setMapMode('k')}
            className={`px-2.5 py-0.5 rounded-full text-xs font-semibold transition-all flex items-center gap-1 ${
              mapMode === 'k'
                ? 'bg-brand-600 text-white shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Satellite className="w-3 h-3" />
            <span>Satélite</span>
          </button>
          <button
            type="button"
            onClick={() => setMapMode('h')}
            className={`px-2.5 py-0.5 rounded-full text-xs font-semibold transition-all flex items-center gap-1 ${
              mapMode === 'h'
                ? 'bg-brand-600 text-white shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            <Globe className="w-3 h-3" />
            <span>Híbrido</span>
          </button>
        </div>

        <a
          href={externalMapUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="w-7 h-7 rounded-full bg-slate-50 border border-slate-200 flex items-center justify-center text-slate-500 hover:text-slate-900 hover:bg-slate-100 transition-colors"
          title="Abrir no Google Maps"
        >
          <ExternalLink className="w-3.5 h-3.5" />
        </a>
      </div>

      <div className="relative w-full flex-1 rounded-2xl overflow-hidden border border-slate-200/80 shadow-inner min-h-[190px]">
        <iframe
          title={`Mapa satélite de ${cidade}`}
          src={mapSrc}
          className="w-[calc(100%+24px)] h-[calc(100%+60px)] -mt-14 -ml-3 border-0 filter brightness-95 contrast-105"
          loading="lazy"
          allowFullScreen
        />
      </div>
    </div>
  );
};
