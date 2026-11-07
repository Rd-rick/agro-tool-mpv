import React, { useState } from 'react';
import {
  Sprout,
  RefreshCw,
  BarChart2,
  Layers,
  Cpu,
  Menu,
  X,
} from 'lucide-react';
import { cn } from '../../utils/cn';

export type NavTab = 'dashboard' | 'culturas' | 'comparador' | 'jobs';

interface HeaderProps {
  activeTab: NavTab;
  onTabChange: (tab: NavTab) => void;
  onOpenJobsModal: () => void;
}

export const Header: React.FC<HeaderProps> = ({ activeTab, onTabChange, onOpenJobsModal }) => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState<boolean>(false);

  const handleSelectTab = (tab: NavTab) => {
    onTabChange(tab);
    setMobileMenuOpen(false);
  };

  const handleOpenJobs = () => {
    onOpenJobsModal();
    setMobileMenuOpen(false);
  };

  return (
    <header className="w-full bg-white border-b border-slate-100 sticky top-0 z-40">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-2xl bg-brand-50 border border-brand-100 flex items-center justify-center text-brand-600 shadow-sm shrink-0">
            <Sprout className="w-6 h-6 stroke-[2.2]" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="text-xl font-bold tracking-tight text-slate-900">Agro</span>
              <span className="text-xl font-bold tracking-tight text-brand-600">Tool</span>
              <span className="text-xs font-semibold px-2 py-0.5 rounded-full bg-brand-50 text-brand-700 border border-brand-200">
                SC
              </span>
            </div>
            <p className="text-xs text-slate-600 font-medium">Decisão e Risco Climático</p>
          </div>
        </div>

        <nav className="hidden md:flex items-center p-1.5 bg-slate-100/80 rounded-full border border-slate-200/60 shadow-inner">
          <button
            type="button"
            onClick={() => handleSelectTab('dashboard')}
            className={cn(
              'px-5 py-2 text-sm font-semibold rounded-full transition-all duration-200 flex items-center gap-2',
              activeTab === 'dashboard'
                ? 'bg-brand-600 text-white shadow-sm'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50'
            )}
          >
            <BarChart2 className="w-4 h-4" />
            <span>Dashboard</span>
          </button>

          <button
            type="button"
            onClick={() => handleSelectTab('culturas')}
            className={cn(
              'px-5 py-2 text-sm font-semibold rounded-full transition-all duration-200 flex items-center gap-2',
              activeTab === 'culturas'
                ? 'bg-brand-600 text-white shadow-sm'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50'
            )}
          >
            <Layers className="w-4 h-4" />
            <span>Culturas</span>
          </button>

          <button
            type="button"
            onClick={() => handleSelectTab('comparador')}
            className={cn(
              'px-5 py-2 text-sm font-semibold rounded-full transition-all duration-200 flex items-center gap-2',
              activeTab === 'comparador'
                ? 'bg-brand-600 text-white shadow-sm'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/50'
            )}
          >
            <Cpu className="w-4 h-4" />
            <span>Comparador</span>
          </button>
        </nav>

        <div className="hidden md:flex items-center gap-3">
          <button
            type="button"
            onClick={onOpenJobsModal}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 hover:border-slate-300 transition-all shadow-sm"
            title="Sincronização e Carga de Dados"
          >
            <RefreshCw className="w-3.5 h-3.5 text-slate-500" />
            <span>Cargas</span>
          </button>
        </div>

        <div className="flex md:hidden items-center gap-2">
          <button
            type="button"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="w-10 h-10 rounded-2xl bg-slate-50 border border-slate-200 flex items-center justify-center text-slate-700 hover:bg-slate-100 transition-colors"
            aria-label={mobileMenuOpen ? 'Fechar menu de navegação' : 'Abrir menu de navegação'}
            aria-expanded={mobileMenuOpen}
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {mobileMenuOpen && (
        <>
          <div
            className="fixed inset-0 top-20 bg-slate-900/20 backdrop-blur-xs z-30 md:hidden"
            onClick={() => setMobileMenuOpen(false)}
          />

          <div className="relative z-40 bg-white border-b border-slate-200 shadow-xl px-4 py-5 space-y-3 md:hidden animate-in fade-in slide-in-from-top-2 duration-200">
            <div className="space-y-1">
              <button
                type="button"
                onClick={() => handleSelectTab('dashboard')}
                className={cn(
                  'w-full px-4 py-3 rounded-2xl text-sm font-semibold transition-all flex items-center gap-3 text-left',
                  activeTab === 'dashboard'
                    ? 'bg-brand-600 text-white shadow-sm'
                    : 'text-slate-700 hover:bg-slate-50'
                )}
              >
                <BarChart2 className="w-5 h-5" />
                <span>Dashboard</span>
              </button>

              <button
                type="button"
                onClick={() => handleSelectTab('culturas')}
                className={cn(
                  'w-full px-4 py-3 rounded-2xl text-sm font-semibold transition-all flex items-center gap-3 text-left',
                  activeTab === 'culturas'
                    ? 'bg-brand-600 text-white shadow-sm'
                    : 'text-slate-700 hover:bg-slate-50'
                )}
              >
                <Layers className="w-5 h-5" />
                <span>Culturas</span>
              </button>

              <button
                type="button"
                onClick={() => handleSelectTab('comparador')}
                className={cn(
                  'w-full px-4 py-3 rounded-2xl text-sm font-semibold transition-all flex items-center gap-3 text-left',
                  activeTab === 'comparador'
                    ? 'bg-brand-600 text-white shadow-sm'
                    : 'text-slate-700 hover:bg-slate-50'
                )}
              >
                <Cpu className="w-5 h-5" />
                <span>Comparador</span>
              </button>
            </div>

            <div className="pt-3 border-t border-slate-100 flex flex-col gap-2.5">
              <button
                type="button"
                onClick={handleOpenJobs}
                className="w-full px-4 py-2.5 rounded-2xl bg-slate-50 hover:bg-slate-100 border border-slate-200 text-xs font-semibold text-slate-800 transition-colors flex items-center justify-between"
              >
                <span className="flex items-center gap-2">
                  <RefreshCw className="w-4 h-4 text-slate-500" />
                  <span>Sincronização de Cargas</span>
                </span>
                <span className="text-xs text-slate-500 font-medium">ZARC / Epagri / PAM</span>
              </button>
            </div>
          </div>
        </>
      )}
    </header>
  );
};
