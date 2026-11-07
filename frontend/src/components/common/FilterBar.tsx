import React, { useState, useEffect, useRef } from 'react';
import {
  MapPin,
  Calendar,
  Layers,
  Search,
  ChevronDown,
  Check,
  Clock,
  ArrowRight,
  AlertCircle,
  Sparkles,
  Loader2,
} from 'lucide-react';
import { useFilter } from '../../context/FilterContext';
import { getMunicipios } from '../../api/endpoints';
import { validarFiltrosDashboard, type ValidationErrors, type FilterValues } from '../../utils/validators';
import type { MunicipioItem } from '../../types/api';
import { SOLOS_CONFIG, addMonthsToYearMonth } from '../../utils/constants';

interface FilterBarProps {
  loading?: boolean;
  onConsultar: (filters: FilterValues) => void;
}

export const FilterBar: React.FC<FilterBarProps> = ({ loading = false, onConsultar }) => {
  const {
    codigoIbge,
    nomeMunicipio,
    mesInicio,
    mesFim,
    soloCodigo,
    areaHa,
    minMesInicio,
    maxMesFim,
    setMesInicio,
    setMesFim,
    setSoloCodigo,
    setAreaHa,
    setMunicipio,
    setPeriodo,
  } = useFilter();

  const [municipios, setMunicipios] = useState<MunicipioItem[]>([]);
  const [loadingMunicipios, setLoadingMunicipios] = useState<boolean>(false);
  const [muniDropdownOpen, setMuniDropdownOpen] = useState<boolean>(false);
  const [periodoDropdownOpen, setPeriodoDropdownOpen] = useState<boolean>(false);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [errors, setErrors] = useState<ValidationErrors>({});

  const muniRef = useRef<HTMLDivElement>(null);
  const periodoRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    let mounted = true;
    async function load() {
      setLoadingMunicipios(true);
      try {
        const res = await getMunicipios();
        if (mounted && res.municipios) {
          setMunicipios(res.municipios);
        }
      } catch {
        if (mounted) {
          setMunicipios([
            { nome: 'Curitibanos', uf: 'SC', codigoIbge: '4204809', pracaReferenciaId: null, observacaoPraca: null },
            { nome: 'Chapecó', uf: 'SC', codigoIbge: '4204205', pracaReferenciaId: null, observacaoPraca: null },
            { nome: 'Campos Novos', uf: 'SC', codigoIbge: '4203603', pracaReferenciaId: null, observacaoPraca: null },
            { nome: 'Joaçaba', uf: 'SC', codigoIbge: '4209006', pracaReferenciaId: null, observacaoPraca: null },
            { nome: 'Canoinhas', uf: 'SC', codigoIbge: '4203801', pracaReferenciaId: null, observacaoPraca: null },
          ]);
        }
      } finally {
        if (mounted) setLoadingMunicipios(false);
      }
    }
    load();
    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (muniRef.current && !muniRef.current.contains(event.target as Node)) {
        setMuniDropdownOpen(false);
      }
      if (periodoRef.current && !periodoRef.current.contains(event.target as Node)) {
        setPeriodoDropdownOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const filteredMunicipios = municipios.filter((m) =>
    m.nome.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const formatAnoMesDisplay = (ym: string) => {
    if (!ym) return 'Selecione a janela';
    const [ano, mes] = ym.split('-');
    const meses = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];
    const idx = parseInt(mes, 10) - 1;
    return `${meses[idx] || mes}/${ano}`;
  };

  const presets = [
    { label: 'Próximos 3 meses', inicio: minMesInicio, fim: addMonthsToYearMonth(minMesInicio, 2) },
    { label: 'Safra 6 meses', inicio: minMesInicio, fim: addMonthsToYearMonth(minMesInicio, 5) },
    { label: 'Ciclo 9 meses', inicio: minMesInicio, fim: addMonthsToYearMonth(minMesInicio, 8) },
    { label: 'Ano agrícola (12 meses)', inicio: minMesInicio, fim: addMonthsToYearMonth(minMesInicio, 11) },
  ];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const currentValues: FilterValues = {
      codigoIbge,
      nomeMunicipio,
      mesInicio,
      mesFim,
      soloCodigo,
      areaHa,
    };

    const validationErrors = validarFiltrosDashboard(currentValues);
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length === 0) {
      onConsultar(currentValues);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="bg-white rounded-3xl p-5 sm:p-6 border border-slate-100 shadow-soft mb-8">
      <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-start">
        <div className="md:col-span-3 relative" ref={muniRef}>
          <label className="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5 flex items-center gap-1.5">
            <MapPin className="w-4 h-4 text-brand-600" />
            <span>Município (SC)</span>
            <span className="text-brand-600">*</span>
          </label>
          <button
            type="button"
            onClick={() => setMuniDropdownOpen(!muniDropdownOpen)}
            className={`w-full flex items-center justify-between px-4 py-3 bg-slate-50 border rounded-2xl text-left text-sm sm:text-base font-medium transition-all ${
              errors.codigoIbge
                ? 'border-rose-400 text-rose-800'
                : 'border-slate-200/90 text-slate-800 hover:border-brand-500'
            }`}
          >
            <span className="truncate">
              {nomeMunicipio ? `${nomeMunicipio} - SC` : 'Selecione o município'}
            </span>
            <ChevronDown className="w-4 h-4 text-slate-500 shrink-0 ml-2" />
          </button>

          {errors.codigoIbge && (
            <p className="mt-1.5 text-xs text-rose-600 font-medium flex items-center gap-1">
              <AlertCircle className="w-3.5 h-3.5 shrink-0" />
              {errors.codigoIbge}
            </p>
          )}

          {muniDropdownOpen && (
            <div className="absolute top-full mt-2 left-0 w-full min-w-[280px] bg-white rounded-2xl shadow-xl border border-slate-100 p-2.5 z-50">
              <div className="relative mb-2">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Buscar município catarinense..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 text-sm bg-slate-50 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/20"
                  autoFocus
                />
              </div>
              <div className="max-h-60 overflow-y-auto space-y-1">
                {loadingMunicipios ? (
                  <p className="p-3 text-sm text-slate-500 text-center">Carregando municípios...</p>
                ) : filteredMunicipios.length === 0 ? (
                  <p className="p-3 text-sm text-slate-500 text-center">Nenhum município encontrado</p>
                ) : (
                  filteredMunicipios.map((m) => (
                    <button
                      key={m.codigoIbge}
                      type="button"
                      onClick={() => {
                        setMunicipio(m);
                        setErrors((prev) => ({ ...prev, codigoIbge: undefined }));
                        setMuniDropdownOpen(false);
                        setSearchQuery('');
                      }}
                      className="w-full flex items-center justify-between px-3 py-2.5 text-sm rounded-xl hover:bg-brand-50/70 hover:text-brand-900 text-left transition-colors font-medium text-slate-800"
                    >
                      <span>{m.nome}</span>
                      {m.codigoIbge === codigoIbge && (
                        <Check className="w-4 h-4 text-brand-600 shrink-0 ml-2" />
                      )}
                    </button>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        <div className="md:col-span-3 relative" ref={periodoRef}>
          <label className="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5 flex items-center gap-1.5">
            <Calendar className="w-4 h-4 text-brand-600" />
            <span>Janela de plantio</span>
            <span className="text-brand-600">*</span>
          </label>
          <button
            type="button"
            onClick={() => setPeriodoDropdownOpen(!periodoDropdownOpen)}
            className={`w-full flex items-center justify-between px-4 py-3 bg-slate-50 border rounded-2xl text-left text-sm sm:text-base font-medium transition-all ${
              errors.mesInicio || errors.mesFim
                ? 'border-rose-400 text-rose-800'
                : 'border-slate-200/90 text-slate-800 hover:border-brand-500'
            }`}
          >
            <span className="truncate flex items-center gap-1.5 tabular-nums">
              {mesInicio ? (
                <>
                  <span>{formatAnoMesDisplay(mesInicio)}</span>
                  <ArrowRight className="w-4 h-4 text-slate-400 shrink-0" />
                  <span>{formatAnoMesDisplay(mesFim)}</span>
                </>
              ) : (
                <span className="text-slate-400 font-normal">Selecione o período</span>
              )}
            </span>
            <ChevronDown className="w-4 h-4 text-slate-500 shrink-0 ml-2" />
          </button>

          {(errors.mesInicio || errors.mesFim) && (
            <p className="mt-1.5 text-xs text-rose-600 font-medium flex items-center gap-1">
              <AlertCircle className="w-3.5 h-3.5 shrink-0" />
              {errors.mesInicio || errors.mesFim}
            </p>
          )}

          {periodoDropdownOpen && (
            <div className="absolute top-full mt-2 left-0 w-full min-w-[320px] bg-white rounded-3xl shadow-2xl border border-slate-100 p-5 z-50 space-y-4">
              <div>
                <span className="text-xs sm:text-sm font-semibold text-slate-700 block mb-3">
                  Definir mês inicial e final
                </span>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-medium text-slate-700 mb-1">
                      Mês inicial <span className="text-brand-600">*</span>
                    </label>
                    <input
                      type="month"
                      min={minMesInicio}
                      value={mesInicio}
                      onChange={(e) => {
                        setMesInicio(e.target.value);
                        setErrors((prev) => ({ ...prev, mesInicio: undefined }));
                      }}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm font-medium text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 tabular-nums"
                    />
                    <span className="text-xs text-slate-500 block mt-0.5">
                      Mínimo: {minMesInicio}
                    </span>
                  </div>

                  <div>
                    <label className="block text-xs font-medium text-slate-700 mb-1">
                      Mês final <span className="text-brand-600">*</span>
                    </label>
                    <input
                      type="month"
                      min={mesInicio || minMesInicio}
                      max={maxMesFim}
                      value={mesFim}
                      onChange={(e) => {
                        setMesFim(e.target.value);
                        setErrors((prev) => ({ ...prev, mesFim: undefined }));
                      }}
                      className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-sm font-medium text-slate-800 focus:outline-none focus:ring-2 focus:ring-brand-500/20 tabular-nums"
                    />
                    {maxMesFim && (
                      <span className="text-xs text-slate-500 block mt-0.5">
                        Máximo: {maxMesFim}
                      </span>
                    )}
                  </div>
                </div>
              </div>

              <div className="pt-3 border-t border-slate-100">
                <span className="text-xs sm:text-sm font-semibold text-slate-700 block mb-2 flex items-center gap-1.5">
                  <Clock className="w-4 h-4 text-brand-600" />
                  <span>Atalhos de safra</span>
                </span>
                <div className="grid grid-cols-2 gap-2">
                  {presets.map((p, idx) => (
                    <button
                      key={idx}
                      type="button"
                      onClick={() => {
                        setPeriodo(p.inicio, p.fim);
                        setErrors((prev) => ({ ...prev, mesInicio: undefined, mesFim: undefined }));
                        setPeriodoDropdownOpen(false);
                      }}
                      className="px-3 py-2 text-xs sm:text-sm text-left font-medium rounded-xl bg-slate-50 hover:bg-brand-50 hover:text-brand-800 border border-slate-200/60 transition-colors"
                    >
                      {p.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="pt-2 flex justify-end">
                <button
                  type="button"
                  onClick={() => setPeriodoDropdownOpen(false)}
                  className="px-4 py-2 rounded-xl bg-brand-600 text-white text-xs sm:text-sm font-semibold hover:bg-brand-700 transition-colors shadow-sm"
                >
                  Confirmar período
                </button>
              </div>
            </div>
          )}
        </div>

        <div className="md:col-span-2">
          <label className="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5 flex items-center gap-1.5">
            <Layers className="w-4 h-4 text-brand-600" />
            <span>Tipo de solo</span>
          </label>
          <select
            value={soloCodigo}
            onChange={(e) => setSoloCodigo(e.target.value)}
            className="w-full px-3 py-3 bg-slate-50 border border-slate-200/90 rounded-2xl text-sm sm:text-base font-medium text-slate-800 hover:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20 transition-all cursor-pointer"
          >
            {SOLOS_CONFIG.map((s) => (
              <option key={s.codigo} value={s.codigo}>
                {s.label}
              </option>
            ))}
          </select>
        </div>

        <div className="md:col-span-2">
          <label className="block text-xs sm:text-sm font-semibold text-slate-700 mb-1.5">
            Área de plantio <span className="text-slate-500 font-normal">(ha)</span>
          </label>
          <input
            type="number"
            min="1"
            max="1000000"
            placeholder="Ex: 50"
            value={areaHa}
            onChange={(e) => {
              setAreaHa(e.target.value);
              setErrors((prev) => ({ ...prev, areaHa: undefined }));
            }}
            className={`w-full px-3 py-3 bg-slate-50 border rounded-2xl text-sm sm:text-base font-medium text-slate-800 hover:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20 transition-all placeholder:text-slate-400 tabular-nums ${
              errors.areaHa ? 'border-rose-400' : 'border-slate-200/90'
            }`}
          />
          {errors.areaHa && (
            <p className="mt-1.5 text-xs text-rose-600 font-medium flex items-center gap-1">
              <AlertCircle className="w-3.5 h-3.5 shrink-0" />
              {errors.areaHa}
            </p>
          )}
        </div>

        <div className="md:col-span-2 pt-7">
          <button
            type="submit"
            disabled={loading}
            className="w-full py-3 px-4 bg-brand-600 hover:bg-brand-700 active:scale-[0.98] text-white rounded-2xl text-sm sm:text-base font-bold shadow-sm transition-all flex items-center justify-center gap-2 disabled:opacity-50 cursor-pointer"
          >
            {loading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                <span>Buscando...</span>
              </>
            ) : (
              <>
                <Sparkles className="w-4 h-4" />
                <span>Consultar</span>
              </>
            )}
          </button>
        </div>
      </div>
    </form>
  );
};
