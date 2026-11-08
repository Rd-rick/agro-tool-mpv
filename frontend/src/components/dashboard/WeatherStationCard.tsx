import React, { useState, useEffect } from 'react';
import { MapPin, Sun, Cloud, CloudRain, CloudOff, Info } from 'lucide-react';
import { cn } from '../../utils/cn';
import { getClimaAtual } from '../../api/endpoints';
import type { DashboardResponse, ClimaAtualResponse } from '../../types/api';

interface WeatherStationCardProps {
  data: DashboardResponse;
}

export const WeatherStationCard: React.FC<WeatherStationCardProps> = ({ data }) => {
  const [unit, setUnit] = useState<'C' | 'F'>('C');
  const [clima, setClima] = useState<ClimaAtualResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const codigoIbge = data.municipio.codigoIbge;
  const praca = data.municipio.pracaReferenciaNome || 'Planalto Serrano / SC';
  const hoje = new Intl.DateTimeFormat('pt-BR', {
    weekday: 'long',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(new Date());

  useEffect(() => {
    let ativo = true;
    setLoading(true);

    getClimaAtual(codigoIbge)
      .then((resposta) => {
        if (ativo) {
          setClima(resposta);
          setLoading(false);
        }
      })
      .catch(() => {
        if (ativo) {
          setClima({
            disponivel: false,
            mensagem: 'Temporariamente indisponível',
            temperatura: null,
            condicao: null,
            descricaoCondicao: null,
          });
          setLoading(false);
        }
      });

    return () => {
      ativo = false;
    };
  }, [codigoIbge]);

  const isDisponivel = !loading && !!clima && clima.disponivel && clima.temperatura !== null;
  const tempC = clima?.temperatura ?? 0;
  const tempF = Math.round((tempC * 9) / 5 + 32);

  const renderIconeCondicao = () => {
    if (loading) {
      return (
        <div className="w-12 h-12 rounded-full bg-slate-200 animate-pulse" />
      );
    }

    if (!isDisponivel) {
      return <CloudOff className="w-10 h-10 text-slate-400" />;
    }

    switch (clima?.condicao) {
      case 'CHOVENDO':
        return <CloudRain className="w-12 h-12 text-blue-500" />;
      case 'NUBLADO':
        return <Cloud className="w-12 h-12 text-slate-400" />;
      case 'ENSOLARADO':
      default:
        return <Sun className="w-12 h-12 text-amber-500 animate-spin-slow" />;
    }
  };

  const getDescricaoTexto = () => {
    if (loading) return 'Carregando...';
    if (!isDisponivel) return 'Sem dados';
    if (clima?.descricaoCondicao) return clima.descricaoCondicao;
    if (clima?.condicao === 'CHOVENDO') return 'Chovendo';
    if (clima?.condicao === 'NUBLADO') return 'Nublado';
    return 'Ensolarado';
  };

  return (
    <div
      className={cn(
        'rounded-3xl p-6 border shadow-soft flex flex-col justify-between relative overflow-hidden h-full transition-colors',
        isDisponivel
          ? 'bg-white border-slate-100'
          : 'bg-slate-100/90 border-slate-200 text-slate-600'
      )}
    >
      <div className="flex items-center justify-between mb-3">
        <div
          className={cn(
            'inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold shadow-sm',
            isDisponivel ? 'bg-brand-600 text-white' : 'bg-slate-300 text-slate-700'
          )}
        >
          <MapPin className="w-3.5 h-3.5" />
          <span>{data.municipio.nome} - SC</span>
        </div>

        <div className="flex items-center bg-slate-100 p-0.5 rounded-full border border-slate-200">
          <button
            type="button"
            onClick={() => setUnit('C')}
            disabled={!isDisponivel}
            className={cn(
              'px-2.5 py-0.5 rounded-full text-xs font-semibold transition-all',
              unit === 'C' && isDisponivel
                ? 'bg-brand-600 text-white shadow-sm'
                : unit === 'C'
                ? 'bg-slate-300 text-slate-700'
                : 'text-slate-600 hover:text-slate-900',
              !isDisponivel && 'cursor-not-allowed opacity-60'
            )}
          >
            °C
          </button>
          <button
            type="button"
            onClick={() => setUnit('F')}
            disabled={!isDisponivel}
            className={cn(
              'px-2.5 py-0.5 rounded-full text-xs font-semibold transition-all',
              unit === 'F' && isDisponivel
                ? 'bg-brand-600 text-white shadow-sm'
                : unit === 'F'
                ? 'bg-slate-300 text-slate-700'
                : 'text-slate-600 hover:text-slate-900',
              !isDisponivel && 'cursor-not-allowed opacity-60'
            )}
          >
            °F
          </button>
        </div>
      </div>

      <div className="flex items-start justify-between">
        <div>
          <span className="capitalize text-xs font-medium text-slate-500">{hoje}</span>
          <div className="flex items-baseline gap-2 mt-1">
            {loading ? (
              <div className="h-10 w-24 bg-slate-200 animate-pulse rounded-lg my-1" />
            ) : isDisponivel ? (
              <span className="text-4xl sm:text-5xl font-bold tracking-tight text-slate-900 tabular-nums">
                {unit === 'C' ? `${tempC}°C` : `${tempF}°F`}
              </span>
            ) : (
              <span className="text-base sm:text-lg font-bold tracking-tight text-slate-600">
                Temporariamente indisponível
              </span>
            )}
          </div>
          <p className="text-xs text-slate-600 mt-1 font-medium">Praça de referência: {praca}</p>
        </div>

        <div className="flex flex-col items-center">
          <div className="relative w-16 h-16 flex items-center justify-center">
            {renderIconeCondicao()}
          </div>
          <span className="text-xs font-medium text-slate-700 mt-1 capitalize">
            {getDescricaoTexto()}
          </span>
        </div>
      </div>

      <div className="mt-4 pt-3.5 border-t border-slate-200/60 flex items-center gap-2 text-xs text-slate-600 leading-relaxed">
        <Info
          className={cn(
            'w-4 h-4 shrink-0',
            isDisponivel ? 'text-brand-600' : 'text-slate-400'
          )}
        />
        <span>
          {isDisponivel
            ? 'Previsão oficial do dia fornecida pelo INMET.'
            : 'Previsão meteorológica do INMET temporariamente indisponível.'}
        </span>
      </div>
    </div>
  );
};
