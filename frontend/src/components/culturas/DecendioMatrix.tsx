import React from 'react';
import type { DecendioMatriz } from '../../types/api';

interface DecendioMatrixProps {
  matriz: DecendioMatriz[];
}

export const DecendioMatrix: React.FC<DecendioMatrixProps> = ({ matriz }) => {
  const meses = [
    { mes: 1, nome: 'Jan' },
    { mes: 2, nome: 'Fev' },
    { mes: 3, nome: 'Mar' },
    { mes: 4, nome: 'Abr' },
    { mes: 5, nome: 'Mai' },
    { mes: 6, nome: 'Jun' },
    { mes: 7, nome: 'Jul' },
    { mes: 8, nome: 'Ago' },
    { mes: 9, nome: 'Set' },
    { mes: 10, nome: 'Out' },
    { mes: 11, nome: 'Nov' },
    { mes: 12, nome: 'Dez' },
  ];

  return (
    <div className="bg-white rounded-2xl p-5 border border-slate-100 shadow-sm">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
        <h4 className="text-xs sm:text-sm font-bold text-slate-900 tracking-tight">
          Calendário decendial ZARC (36 decêndios anuais)
        </h4>
        <div className="flex items-center gap-3 text-xs font-semibold">
          <span className="flex items-center gap-1.5 text-slate-700">
            <span className="w-3 h-3 rounded bg-emerald-600 inline-block" /> 20%
          </span>
          <span className="flex items-center gap-1.5 text-slate-700">
            <span className="w-3 h-3 rounded bg-amber-400 inline-block" /> 30%
          </span>
          <span className="flex items-center gap-1.5 text-slate-700">
            <span className="w-3 h-3 rounded bg-rose-500 inline-block" /> 40%
          </span>
          <span className="flex items-center gap-1.5 text-slate-500 font-medium">
            <span className="w-3 h-3 rounded bg-slate-200 border border-slate-300 inline-block" /> Inapto
          </span>
        </div>
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-6 gap-2 text-center">
        {meses.map((m) => {
          const decendiosDoMes = matriz.filter((d) => d.mes === m.mes);
          return (
            <div key={m.mes} className="bg-slate-50/80 rounded-xl p-1.5 border border-slate-100">
              <span className="text-xs font-bold text-slate-700 block mb-1.5">
                {m.nome}
              </span>
              <div className="grid grid-cols-3 gap-1">
                {[1, 2, 3].map((pos) => {
                  const item = decendiosDoMes.find((d) => d.decendioNoMes === pos);
                  const risco = item?.menorRisco;
                  const apto = item?.favoravel;

                  let bgClass = 'bg-slate-200 text-slate-500';
                  if (apto) {
                    if (risco === 20) bgClass = 'bg-emerald-600 text-white font-bold';
                    else if (risco === 30) bgClass = 'bg-amber-400 text-slate-900 font-bold';
                    else if (risco === 40) bgClass = 'bg-rose-500 text-white font-bold';
                  }

                  return (
                    <div
                      key={pos}
                      title={
                        item
                          ? `${item.descricaoPeriodo}: ${apto ? `Risco ${risco}%` : 'Inapto'}`
                          : `D${pos}`
                      }
                      className={`h-7 rounded flex items-center justify-center text-xs tabular-nums transition-transform hover:scale-110 cursor-default ${bgClass}`}
                    >
                      {pos}
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
