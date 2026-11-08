import React, { useEffect, useRef } from 'react';
import { X, Droplets } from 'lucide-react';

interface TiposSolosModalProps {
  isOpen: boolean;
  onClose: () => void;
}

const SOLOS_GRANULOMETRIA = [
  {
    tipo: 'Tipo 1 — Arenoso',
    descricao: 'Solo com maior proporção de areia e menor capacidade de retenção de água.',
    badge: 'Arenoso',
  },
  {
    tipo: 'Tipo 2 — Textura média',
    descricao: 'Apresenta composição intermediária de areia, silte e argila.',
    badge: 'Médio',
  },
  {
    tipo: 'Tipo 3 — Argiloso',
    descricao: 'Possui maior proporção de argila e, geralmente, maior retenção de água.',
    badge: 'Argiloso',
  },
];

const SOLOS_AD = [
  {
    classe: 'AD1',
    faixa: '0,34 a 0,46 mm/cm',
    descricao: 'Menor faixa de água disponível.',
  },
  {
    classe: 'AD2',
    faixa: '0,46 a 0,61 mm/cm',
    descricao: 'Baixa disponibilidade de água.',
  },
  {
    classe: 'AD3',
    faixa: '0,61 a 0,80 mm/cm',
    descricao: 'Disponibilidade intermediária.',
  },
  {
    classe: 'AD4',
    faixa: '0,80 a 1,06 mm/cm',
    descricao: 'Boa disponibilidade de água.',
  },
  {
    classe: 'AD5',
    faixa: '1,06 a 1,40 mm/cm',
    descricao: 'Alta disponibilidade de água.',
  },
  {
    classe: 'AD6',
    faixa: '≥ 1,40 mm/cm',
    descricao: 'Maior faixa de água disponível.',
  },
];

export const TiposSolosModal: React.FC<TiposSolosModalProps> = ({ isOpen, onClose }) => {
  const modalRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div
      onClick={(e) => {
        if (modalRef.current && !modalRef.current.contains(e.target as Node)) {
          onClose();
        }
      }}
      className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4 animate-in fade-in duration-200"
    >
      <div
        ref={modalRef}
        className="bg-white rounded-3xl max-w-2xl w-full max-h-[90vh] shadow-2xl flex flex-col overflow-hidden border border-slate-100"
      >
        <div className="p-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50 shrink-0">
          <div className="flex items-center gap-3">
            <div>
              <h3 className="text-base sm:text-lg font-bold text-slate-900">
                Tipos de Solos e Classes
              </h3>
              <p className="text-xs sm:text-sm text-slate-500">
                Critérios técnicos de retenção hídrica e textura do zoneamento agrícola
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full border border-slate-200 text-slate-500 hover:text-slate-900 flex items-center justify-center transition-colors hover:bg-slate-100"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="p-6 overflow-y-auto space-y-6">
          <section className="space-y-3">
            <div className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-brand-600" />
              <h4 className="text-sm font-bold text-slate-900">
                Composição Granulométrica (Textura)
              </h4>
            </div>
            <p className="text-xs sm:text-sm text-slate-600">
              Identifica o tipo de solo conforme sua composição granulométrica.
            </p>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 pt-1">
              {SOLOS_GRANULOMETRIA.map((item, idx) => (
                <div
                  key={idx}
                  className="bg-slate-50 rounded-2xl p-4 border border-slate-100 flex flex-col justify-between space-y-2 hover:border-brand-200 transition-colors"
                >
                  <span className="text-xs sm:text-sm font-bold text-slate-900">
                    {item.tipo}
                  </span>
                  <p className="text-xs text-slate-600 leading-relaxed">
                    {item.descricao}
                  </p>
                </div>
              ))}
            </div>
          </section>

          <section className="space-y-3 pt-2 border-t border-slate-100">
            <div className="flex items-center gap-2">
              <div className="w-5 h-5 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
                <Droplets className="w-3.5 h-3.5" />
              </div>
              <h4 className="text-sm font-bold text-slate-900">
                Classes de Água Disponível (AD)
              </h4>
            </div>
            <p className="text-xs sm:text-sm text-slate-600">
              Classifica o solo conforme sua capacidade de disponibilizar água às plantas.
            </p>

            <div className="space-y-2 pt-1">
              {SOLOS_AD.map((item) => (
                <div
                  key={item.classe}
                  className="p-3 flex flex-col sm:flex-row sm:items-center justify-between gap-2 transition-colors"
                >
                  <div className="flex items-center gap-3">
                    <span className="px-2.5 py-1 rounded-xl bg-white text-xs font-bold text-slate-900 shadow-2xs shrink-0">
                      {item.classe}
                    </span>
                    <span className="text-xs sm:text-sm text-slate-700 font-medium">
                      {item.descricao}
                    </span>
                  </div>
                  <span className="text-xs font-bold text-brand-700 tabular-nums self-end sm:self-auto shrink-0 bg-brand-50/60 px-2.5 py-0.5 rounded-lg">
                    {item.faixa}
                  </span>
                </div>
              ))}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};
