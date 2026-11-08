import React, { useState, useRef, useEffect } from 'react';
import { Calendar, ChevronLeft, ChevronRight, ChevronDown } from 'lucide-react';

interface MonthPickerInputProps {
  label?: string;
  required?: boolean;
  value: string; // 'YYYY-MM' ou ''
  onChange: (value: string) => void;
  placeholder?: string;
  min?: string; // 'YYYY-MM'
  max?: string; // 'YYYY-MM'
  error?: string;
  disabled?: boolean;
  helperText?: string;
  align?: 'left' | 'right';
}

const MESES = [
  { abrev: 'Jan', nome: 'Janeiro' },
  { abrev: 'Fev', nome: 'Fevereiro' },
  { abrev: 'Mar', nome: 'Março' },
  { abrev: 'Abr', nome: 'Abril' },
  { abrev: 'Mai', nome: 'Maio' },
  { abrev: 'Jun', nome: 'Junho' },
  { abrev: 'Jul', nome: 'Julho' },
  { abrev: 'Ago', nome: 'Agosto' },
  { abrev: 'Set', nome: 'Setembro' },
  { abrev: 'Out', nome: 'Outubro' },
  { abrev: 'Nov', nome: 'Novembro' },
  { abrev: 'Dez', nome: 'Dezembro' },
];

export const MonthPickerInput: React.FC<MonthPickerInputProps> = ({
  label,
  required = false,
  value,
  onChange,
  placeholder = 'Selecione o mês',
  min,
  max,
  error,
  disabled = false,
  helperText,
  align = 'left',
}) => {
  const [isOpen, setIsOpen] = useState(false);

  // Calcula o ano ativo inicial para exibição na grade
  const initialYear = () => {
    if (value && value.includes('-')) {
      const y = parseInt(value.split('-')[0], 10);
      if (!isNaN(y)) return y;
    }
    if (min && min.includes('-')) {
      const y = parseInt(min.split('-')[0], 10);
      if (!isNaN(y)) return y;
    }
    return new Date().getFullYear();
  };

  const [activeYear, setActiveYear] = useState<number>(initialYear());
  const containerRef = useRef<HTMLDivElement>(null);

  // Atualiza o ano ativo quando o valor externo mudar
  useEffect(() => {
    if (value && value.includes('-')) {
      const y = parseInt(value.split('-')[0], 10);
      if (!isNaN(y)) setActiveYear(y);
    }
  }, [value]);

  // Fechar ao clicar fora
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setIsOpen(false);
      }
    }

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      document.addEventListener('keydown', handleKeyDown);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen]);

  const minYear = min ? parseInt(min.split('-')[0], 10) : undefined;
  const maxYear = max ? parseInt(max.split('-')[0], 10) : undefined;

  const canPrevYear = minYear === undefined || activeYear > minYear;
  const canNextYear = maxYear === undefined || activeYear < maxYear;

  const formatDisplay = (val: string) => {
    if (!val || !val.includes('-')) return '';
    const [yStr, mStr] = val.split('-');
    const mIdx = parseInt(mStr, 10) - 1;
    if (mIdx >= 0 && mIdx < 12) {
      return `${MESES[mIdx].abrev} / ${yStr}`;
    }
    return val;
  };

  const isMonthDisabled = (monthIndex: number) => {
    const ym = `${activeYear}-${String(monthIndex + 1).padStart(2, '0')}`;
    if (min && ym < min) return true;
    if (max && ym > max) return true;
    return false;
  };

  const isMonthSelected = (monthIndex: number) => {
    const ym = `${activeYear}-${String(monthIndex + 1).padStart(2, '0')}`;
    return value === ym;
  };

  const handleSelectMonth = (monthIndex: number) => {
    if (isMonthDisabled(monthIndex)) return;
    const ym = `${activeYear}-${String(monthIndex + 1).padStart(2, '0')}`;
    onChange(ym);
    setIsOpen(false);
  };

  return (
    <div className="relative w-full" ref={containerRef}>
      {label && (
        <label className="block text-xs font-medium text-slate-700 mb-1">
          {label} {required && <span className="text-brand-600">*</span>}
        </label>
      )}

      {/* Botão de disparo (sem digitação por teclado) */}
      <button
        type="button"
        disabled={disabled}
        onClick={() => !disabled && setIsOpen((prev) => !prev)}
        className={`w-full flex items-center justify-between px-3 py-2 bg-slate-50 border rounded-xl text-sm font-medium transition-all text-left ${
          error
            ? 'border-rose-400 text-rose-800'
            : isOpen
            ? 'border-brand-500 ring-2 ring-brand-500/20 bg-white'
            : 'border-slate-200 text-slate-800 hover:border-brand-500 hover:bg-white'
        } ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
      >
        <span className="flex items-center gap-2 truncate">
          <Calendar className="w-4 h-4 text-brand-600 shrink-0" />
          {value ? (
            <span className="text-slate-800 font-semibold tabular-nums">{formatDisplay(value)}</span>
          ) : (
            <span className="text-slate-400 font-normal">{placeholder}</span>
          )}
        </span>
        <ChevronDown
          className={`w-3.5 h-3.5 text-slate-400 shrink-0 ml-1 transition-transform ${
            isOpen ? 'rotate-180' : ''
          }`}
        />
      </button>

      {helperText && !error && (
        <span className="text-xs text-slate-500 block mt-1">{helperText}</span>
      )}

      {error && (
        <span className="text-xs text-rose-600 font-medium block mt-1">{error}</span>
      )}

      {/* Popover de seleção de mês e ano */}
      {isOpen && (
        <div
          className={`absolute top-full mt-2 w-64 bg-white rounded-2xl shadow-xl border border-slate-100 p-3 z-50 animate-in fade-in zoom-in-95 duration-100 ${
            align === 'right' ? 'right-0' : 'left-0'
          }`}
        >
          {/* Cabeçalho do Ano */}
          <div className="flex items-center justify-between mb-2.5 pb-2 border-b border-slate-100">
            <button
              type="button"
              disabled={!canPrevYear}
              onClick={(e) => {
                e.stopPropagation();
                if (canPrevYear) setActiveYear((y) => y - 1);
              }}
              className="p-1 rounded-lg text-slate-500 hover:text-slate-900 hover:bg-slate-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
              title="Ano anterior"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="text-sm font-bold text-slate-800">{activeYear}</span>
            <button
              type="button"
              disabled={!canNextYear}
              onClick={(e) => {
                e.stopPropagation();
                if (canNextYear) setActiveYear((y) => y + 1);
              }}
              className="p-1 rounded-lg text-slate-500 hover:text-slate-900 hover:bg-slate-100 disabled:opacity-30 disabled:cursor-not-allowed transition-colors"
              title="Próximo ano"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          {/* Grade de Meses */}
          <div className="grid grid-cols-3 gap-1.5">
            {MESES.map((m, idx) => {
              const disabledMonth = isMonthDisabled(idx);
              const selected = isMonthSelected(idx);

              return (
                <button
                  key={m.abrev}
                  type="button"
                  disabled={disabledMonth}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleSelectMonth(idx);
                  }}
                  title={m.nome}
                  className={`py-2 px-1 text-xs font-semibold rounded-xl text-center transition-all ${
                    selected
                      ? 'bg-brand-600 text-white shadow-sm font-bold'
                      : disabledMonth
                      ? 'text-slate-300 bg-slate-50/50 cursor-not-allowed'
                      : 'text-slate-700 hover:bg-brand-50 hover:text-brand-800 active:scale-95'
                  }`}
                >
                  {m.abrev}
                </button>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};
