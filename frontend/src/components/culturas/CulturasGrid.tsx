import React, { useState } from 'react';
import { Search, Sprout, ArrowUpRight, Check, Plus } from 'lucide-react';
import { BadgeRisk } from '../common/BadgeRisk';
import { formatarSolosAptosResumo } from '../../utils/formatters';
import type { CulturaPossivelDashboard } from '../../types/api';

interface CulturasGridProps {
  culturas: CulturaPossivelDashboard[];
  selectedToCompare: number[];
  onToggleCompare: (id: number) => void;
  onSelectCultura: (id: number) => void;
}

export const CulturasGrid: React.FC<CulturasGridProps> = ({
  culturas,
  selectedToCompare,
  onToggleCompare,
  onSelectCultura,
}) => {
  const [search, setSearch] = useState<string>('');

  const filtered = culturas.filter((c) => {
    return (
      c.nomeCultura.toLowerCase().includes(search.toLowerCase()) ||
      (c.nomeCientifico && c.nomeCientifico.toLowerCase().includes(search.toLowerCase()))
    );
  });

  return (
    <div className="space-y-6">
      <div className="bg-white p-4 rounded-3xl border border-slate-100 shadow-soft">
        <div className="relative">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Buscar cultura ou espécie..."
            className="w-full pl-10 pr-4 py-2.5 text-sm bg-slate-50 border border-slate-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-brand-500/20"
          />
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filtered.map((c) => {
          const isSelected = selectedToCompare.includes(c.culturaId);
          const resumoSolos = formatarSolosAptosResumo(c.solos);
          return (
            <div
              key={c.culturaId}
              className="bg-white rounded-3xl p-5 border border-slate-100 shadow-soft flex flex-col justify-between hover:border-brand-200 transition-all group"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-3">
                  <div className="flex items-center gap-2.5">
                    <div className="w-10 h-10 rounded-2xl bg-brand-50 text-brand-600 flex items-center justify-center shrink-0">
                      <Sprout className="w-5 h-5" />
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-slate-900 group-hover:text-brand-600 transition-colors">
                        {c.nomeCultura}
                      </h4>
                      <p className="text-xs text-slate-500 italic truncate max-w-[170px]">
                        {c.nomeCientifico || 'Zoneamento ZARC'}
                      </p>
                    </div>
                  </div>

                  <BadgeRisk risco={c.riscoMinimo} />
                </div>

                <div className="space-y-2.5 py-3 border-y border-slate-100 text-xs sm:text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-slate-600 font-medium">Situação:</span>
                    <span className="font-semibold text-slate-900 text-right">{c.situacao}</span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-slate-600 font-medium">Melhor período:</span>
                    <span className="font-semibold text-brand-700 text-right">
                      {c.recomendacaoMelhorPeriodo || c.epocaPlantio || 'Janela ZARC'}
                    </span>
                  </div>

                  <div className="flex items-center justify-between gap-2">
                    <span className="text-slate-600 font-medium shrink-0">Solos aptos:</span>
                    <span
                      className="font-semibold text-slate-900 text-right truncate max-w-[200px]"
                      title={resumoSolos.tooltip}
                    >
                      {resumoSolos.texto}
                    </span>
                  </div>
                </div>
              </div>

              <div className="mt-4 pt-2 flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => onSelectCultura(c.culturaId)}
                  className="flex-1 py-2 px-3 rounded-2xl bg-slate-50 hover:bg-brand-50 hover:text-brand-700 border border-slate-200/80 text-xs font-bold text-slate-700 transition-all flex items-center justify-center gap-1.5"
                >
                  <span>Análise Completa</span>
                  <ArrowUpRight className="w-3.5 h-3.5" />
                </button>

                <button
                  type="button"
                  onClick={() => onToggleCompare(c.culturaId)}
                  title={isSelected ? 'Remover do comparador' : 'Adicionar ao comparador'}
                  className={`px-3 py-2 rounded-2xl border text-xs font-bold transition-all flex items-center gap-1 ${
                    isSelected
                      ? 'bg-brand-600 text-white border-brand-600 shadow-sm'
                      : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'
                  }`}
                >
                  {isSelected ? <Check className="w-3.5 h-3.5" /> : <Plus className="w-3.5 h-3.5" />}
                  <span className="hidden sm:inline">{isSelected ? 'Comparando' : 'Comparar'}</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
