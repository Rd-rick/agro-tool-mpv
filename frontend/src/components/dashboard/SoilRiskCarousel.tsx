import React, { useState, useEffect } from 'react';
import { Layers, ShieldCheck } from 'lucide-react';
import type { DashboardResponse } from '../../types/api';

interface SoilRiskCarouselProps {
  data: DashboardResponse;
}

export const SoilRiskCarousel: React.FC<SoilRiskCarouselProps> = ({ data }) => {
  const [slideIndex, setSlideIndex] = useState<number>(0);
  const [isHovered, setIsHovered] = useState<boolean>(false);

  const bestOportunidade = data.melhorOportunidade;
  const riscoMinimo = bestOportunidade?.riscoMinimoZarc ?? 20;
  const decendiosFavoraveis = bestOportunidade?.quantidadeDecendiosFavoraveis ?? 8;
  const solosAptos = bestOportunidade?.solosAptos?.length ?? 3;

  const slides = [
    {
      title: 'Risco climático ZARC',
      value: `${riscoMinimo}%`,
      subtitle: 'Nível mínimo identificado',
    },
    {
      title: 'Decêndios favoráveis',
      value: `${decendiosFavoraveis}`,
      subtitle: 'Janela de plantio recomendada',
    },
    {
      title: 'Solos aptos na região',
      value: `${solosAptos}/9`,
      subtitle: 'Tipos aceitos pelo zoneamento',
    },
  ];

  useEffect(() => {
    if (isHovered) return;
    const interval = setInterval(() => {
      setSlideIndex((prev) => (prev + 1) % slides.length);
    }, 4000);
    return () => clearInterval(interval);
  }, [isHovered, slides.length]);

  const current = slides[slideIndex];

  return (
    <div
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      className="bg-white rounded-3xl p-6 border border-slate-100 shadow-soft flex flex-col items-center justify-center text-center h-full relative select-none"
    >
      <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-50 border border-slate-200 text-xs font-semibold text-slate-700 mb-4 shadow-2xs">
        <Layers className="w-3.5 h-3.5 text-brand-600" />
        <span>{data.soloSelecionado ? data.soloSelecionado.tipoSolo : 'Solos catalogados'}</span>
      </div>

      <div className="transition-all duration-500 ease-in-out flex flex-col items-center">
        <span className="text-xs font-semibold text-slate-600 tracking-wide block">
          {current.title}
        </span>

        <div className="text-4xl sm:text-5xl font-bold text-slate-900 tracking-tight my-2 tabular-nums">
          {current.value}
        </div>

        <p className="text-xs text-brand-700 font-semibold inline-flex items-center gap-1.5">
          <ShieldCheck className="w-4 h-4 text-brand-600" />
          <span>{current.subtitle}</span>
        </p>
      </div>

      <div className="flex items-center gap-1.5 mt-5">
        {slides.map((_, idx) => (
          <button
            key={idx}
            type="button"
            onClick={() => setSlideIndex(idx)}
            className={`transition-all duration-300 rounded-full ${
              slideIndex === idx
                ? 'w-5 h-1.5 bg-brand-600'
                : 'w-1.5 h-1.5 bg-slate-200 hover:bg-slate-300'
            }`}
            aria-label={`Ir para indicador ${idx + 1}`}
          />
        ))}
      </div>
    </div>
  );
};
