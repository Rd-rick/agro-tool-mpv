import React from 'react';
import { Sprout, ShieldCheck, TrendingUp, Bot, Sparkles, ArrowUp } from 'lucide-react';
import { useFilter } from '../../context/FilterContext';

interface DashboardEmptyStateProps {
  onApplyPreset?: () => void;
}

export const DashboardEmptyState: React.FC<DashboardEmptyStateProps> = () => {
  const { fillCuritibanosPreset } = useFilter();

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-3xl p-8 sm:p-12 border border-slate-100 shadow-soft text-center relative overflow-hidden">
        <div className="max-w-2xl mx-auto flex flex-col items-center">
          <div className="w-16 h-16 rounded-3xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600 mb-5 shadow-sm">
            <Sprout className="w-8 h-8 stroke-[2.2]" />
          </div>

          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-100 text-slate-700 text-xs font-semibold mb-3">
            <Sparkles className="w-3.5 h-3.5 text-brand-600" />
            <span>Diagnóstico Agroclimático & Mercado</span>
          </div>

          <h2 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Pronto para planejar sua safra?
          </h2>

          <p className="text-sm text-slate-500 font-medium mt-3 leading-relaxed">
            Selecione o <strong>Município</strong> e a <strong>Janela Safra / Plantio</strong> nos filtros acima e clique em{' '}
            <span className="text-brand-700 font-bold">Consultar Oportunidades</span> para carregar o zoneamento ZARC e as cotações de Santa Catarina.
          </p>

          <div className="mt-4 flex items-center gap-1.5 text-xs text-brand-700 font-semibold bg-brand-50 px-4 py-2 rounded-2xl border border-brand-100/80">
            <ArrowUp className="w-4 h-4 animate-bounce" />
            <span>Preencha os campos obrigatórios acima para iniciar</span>
          </div>

          <div className="mt-8 pt-6 border-t border-slate-100 w-full flex flex-col sm:flex-row items-center justify-center gap-3">
            <span className="text-xs text-slate-400 font-medium">Quer testar rapidamente?</span>
            <button
              type="button"
              onClick={fillCuritibanosPreset}
              className="px-4 py-2 rounded-xl bg-slate-100 hover:bg-brand-50 hover:text-brand-800 text-slate-700 text-xs font-bold transition-all border border-slate-200/80"
            >
              Preencher com Curitibanos - SC
            </button>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white/80 backdrop-blur-sm rounded-3xl p-6 border border-dashed border-slate-200 flex flex-col justify-between">
          <div className="w-10 h-10 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mb-4">
            <ShieldCheck className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900">Zoneamento Climático ZARC</h4>
            <p className="text-xs text-slate-500 mt-1 leading-relaxed">
              Consulte quais culturas são aptas para o município com níveis de risco de 20%, 30% ou 40% em 36 decêndios anuais.
            </p>
          </div>
          <span className="text-[11px] font-semibold text-slate-400 mt-4 block">
            Aguardando seleção do município
          </span>
        </div>

        <div className="bg-white/80 backdrop-blur-sm rounded-3xl p-6 border border-dashed border-slate-200 flex flex-col justify-between">
          <div className="w-10 h-10 rounded-2xl bg-brand-50 text-brand-600 flex items-center justify-center mb-4">
            <TrendingUp className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900">Ranking & Preços Epagri / CONAB</h4>
            <p className="text-xs text-slate-500 mt-1 leading-relaxed">
              O motor de decisão cruza segurança climática com cotações recentes para ranquear as melhores oportunidades.
            </p>
          </div>
          <span className="text-[11px] font-semibold text-slate-400 mt-4 block">
            Aguardando definição da janela
          </span>
        </div>

        <div className="bg-white/80 backdrop-blur-sm rounded-3xl p-6 border border-dashed border-slate-200 flex flex-col justify-between">
          <div className="w-10 h-10 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mb-4">
            <Bot className="w-5 h-5" />
          </div>
          <div>
            <h4 className="text-sm font-bold text-slate-900">Assistente IA Especializado</h4>
            <p className="text-xs text-slate-500 mt-1 leading-relaxed">
              Tire dúvidas em linguagem natural com ferramentas conectadas diretamente às bases oficiais da plataforma.
            </p>
          </div>
          <span className="text-[11px] font-semibold text-slate-400 mt-4 block">
            Aguardando contexto regional
          </span>
        </div>
      </div>
    </div>
  );
};
