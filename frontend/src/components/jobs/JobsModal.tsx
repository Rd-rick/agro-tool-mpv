import React, { useState } from 'react';
import { X, RefreshCw, Database, CheckCircle2, AlertCircle, Loader2 } from 'lucide-react';
import { postJobEpagri, postJobZarc, postJobPam } from '../../api/endpoints';

interface JobsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccessRefresh: () => void;
}

export const JobsModal: React.FC<JobsModalProps> = ({ isOpen, onClose, onSuccessRefresh }) => {
  const [loading, setLoading] = useState<string | null>(null);
  const [feedback, setFeedback] = useState<{ tipo: 'sucesso' | 'erro'; mensagem: string } | null>(null);

  if (!isOpen) return null;

  const handleRunEpagri = async (freq?: string) => {
    setLoading(freq ? `epagri-${freq}` : 'epagri-all');
    setFeedback(null);
    try {
      const res = await postJobEpagri(freq);
      setFeedback({ tipo: 'sucesso', mensagem: res.mensagem || 'Cotações Epagri/Cepa sincronizadas!' });
      onSuccessRefresh();
    } catch {
      setFeedback({ tipo: 'erro', mensagem: 'Falha ao sincronizar Epagri. Verifique o log do backend.' });
    } finally {
      setLoading(null);
    }
  };

  const handleRunZarc = async (safra?: string) => {
    setLoading(safra ? `zarc-${safra}` : 'zarc-all');
    setFeedback(null);
    try {
      const res = await postJobZarc(safra);
      setFeedback({ tipo: 'sucesso', mensagem: res.mensagem || 'Zoneamento ZARC sincronizado!' });
      onSuccessRefresh();
    } catch {
      setFeedback({ tipo: 'erro', mensagem: 'Falha ao sincronizar ZARC. Verifique o log do backend.' });
    } finally {
      setLoading(null);
    }
  };

  const handleRunPam = async (ano?: number) => {
    setLoading(ano ? `pam-${ano}` : 'pam-all');
    setFeedback(null);
    try {
      const res = await postJobPam(ano);
      setFeedback({ tipo: 'sucesso', mensagem: res.mensagem || 'Dados de produção PAM/IBGE sincronizados!' });
      onSuccessRefresh();
    } catch {
      setFeedback({ tipo: 'erro', mensagem: 'Falha ao sincronizar PAM. Verifique o log do backend.' });
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-white rounded-3xl max-w-xl w-full shadow-2xl p-6 border border-slate-100 flex flex-col space-y-6">
        <div className="flex items-center justify-between pb-3 border-b border-slate-100">
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-2xl bg-brand-50 text-brand-600 flex items-center justify-center">
              <Database className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">Sincronização de Fontes Públicas</h3>
              <p className="text-xs text-slate-500">Execução e recarga das bases ZARC, Epagri/Cepa e IBGE PAM</p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full border border-slate-200 text-slate-500 hover:text-slate-900 flex items-center justify-center transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {feedback && (
          <div
            className={`p-3.5 rounded-2xl flex items-center gap-2 text-xs font-semibold ${
              feedback.tipo === 'sucesso'
                ? 'bg-emerald-50 text-emerald-800 border border-emerald-200'
                : 'bg-rose-50 text-rose-800 border border-rose-200'
            }`}
          >
            {feedback.tipo === 'sucesso' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
            ) : (
              <AlertCircle className="w-4 h-4 text-rose-600 shrink-0" />
            )}
            <span>{feedback.mensagem}</span>
          </div>
        )}

        <div className="space-y-4">
          <div className="bg-slate-50 p-4 rounded-2xl border border-slate-100 flex items-center justify-between">
            <div>
              <h4 className="text-xs font-bold text-slate-900">Epagri/Cepa — Preços Agrícolas de SC</h4>
              <p className="text-[11px] text-slate-500">Cotações de comercialização por produto e praça</p>
            </div>
            <button
              type="button"
              disabled={loading !== null}
              onClick={() => handleRunEpagri()}
              className="px-3.5 py-1.5 rounded-xl bg-brand-600 text-white text-xs font-bold hover:bg-brand-700 disabled:opacity-50 flex items-center gap-1.5 shadow-sm transition-all"
            >
              {loading === 'epagri-all' ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <RefreshCw className="w-3.5 h-3.5" />
              )}
              <span>Sincronizar</span>
            </button>
          </div>

          <div className="bg-slate-50 p-4 rounded-2xl border border-slate-100 flex items-center justify-between">
            <div>
              <h4 className="text-xs font-bold text-slate-900">ZARC — MAPA (Risco Climático)</h4>
              <p className="text-[11px] text-slate-500">Zoneamento, janelas decendiais e matriz de solos</p>
            </div>
            <button
              type="button"
              disabled={loading !== null}
              onClick={() => handleRunZarc()}
              className="px-3.5 py-1.5 rounded-xl bg-brand-600 text-white text-xs font-bold hover:bg-brand-700 disabled:opacity-50 flex items-center gap-1.5 shadow-sm transition-all"
            >
              {loading === 'zarc-all' ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <RefreshCw className="w-3.5 h-3.5" />
              )}
              <span>Sincronizar</span>
            </button>
          </div>

          <div className="bg-slate-50 p-4 rounded-2xl border border-slate-100 flex items-center justify-between">
            <div>
              <h4 className="text-xs font-bold text-slate-900">IBGE / PAM — Produção Agrícola Municipal</h4>
              <p className="text-[11px] text-slate-500">Histórico de rendimento médio (kg/ha) e área colhida</p>
            </div>
            <button
              type="button"
              disabled={loading !== null}
              onClick={() => handleRunPam()}
              className="px-3.5 py-1.5 rounded-xl bg-brand-600 text-white text-xs font-bold hover:bg-brand-700 disabled:opacity-50 flex items-center gap-1.5 shadow-sm transition-all"
            >
              {loading === 'pam-all' ? (
                <Loader2 className="w-3.5 h-3.5 animate-spin" />
              ) : (
                <RefreshCw className="w-3.5 h-3.5" />
              )}
              <span>Sincronizar</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
