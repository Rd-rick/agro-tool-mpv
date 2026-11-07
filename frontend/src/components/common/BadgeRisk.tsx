import React from 'react';
import { cn } from '../../utils/cn';
import { ShieldCheck, ShieldAlert, AlertTriangle } from 'lucide-react';

interface BadgeRiskProps {
  risco: number | null | undefined;
  showIcon?: boolean;
  className?: string;
}

export const BadgeRisk: React.FC<BadgeRiskProps> = ({ risco, showIcon = true, className }) => {
  if (risco === null || risco === undefined) {
    return (
      <span className={cn('inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium bg-slate-100 text-slate-600', className)}>
        Risco Indefinido
      </span>
    );
  }

  if (risco <= 20) {
    return (
      <span
        className={cn(
          'inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200/60',
          className
        )}
      >
        {showIcon && <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" />}
        <span>{risco}% Risco (Baixo)</span>
      </span>
    );
  }

  if (risco <= 30) {
    return (
      <span
        className={cn(
          'inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-800 border border-amber-200/60',
          className
        )}
      >
        {showIcon && <ShieldAlert className="w-3.5 h-3.5 text-amber-600" />}
        <span>{risco}% Risco (Médio)</span>
      </span>
    );
  }

  return (
    <span
      className={cn(
        'inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-50 text-rose-700 border border-rose-200/60',
        className
      )}
    >
      {showIcon && <AlertTriangle className="w-3.5 h-3.5 text-rose-600" />}
      <span>{risco}% Risco (Elevado)</span>
    </span>
  );
};
