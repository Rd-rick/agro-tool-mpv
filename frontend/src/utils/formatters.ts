import type { SoloResumo } from '../types/api';

export function formatCurrency(value?: number | string | null): string {
  if (value === null || value === undefined || value === '') return 'R$ —';
  const num = typeof value === 'string' ? parseFloat(value) : value;
  if (isNaN(num)) return 'R$ —';
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(num);
}

export function formatPercent(value?: number | string | null, includeSign: boolean = true): string {
  if (value === null || value === undefined || value === '') return '—';
  const num = typeof value === 'string' ? parseFloat(value) : value;
  if (isNaN(num)) return '—';
  const prefix = includeSign && num > 0 ? '+' : '';
  return `${prefix}${num.toFixed(1)}%`;
}

export function formatNumber(value?: number | string | null): string {
  if (value === null || value === undefined || value === '') return '—';
  const num = typeof value === 'string' ? parseFloat(value) : value;
  if (isNaN(num)) return '—';
  return new Intl.NumberFormat('pt-BR').format(num);
}

export function formatDate(isoDate?: string | null): string {
  if (!isoDate) return '—';
  try {
    const [year, month, day] = isoDate.split('-');
    if (!year || !month || !day) return isoDate;
    return `${day}/${month}/${year}`;
  } catch {
    return isoDate;
  }
}

const SOLOS_MAPA: Record<string, { rotuloCurto: string; rotuloCompleto: string }> = {
  '1': { rotuloCurto: 'Arenoso', rotuloCompleto: 'Arenoso' },
  '2': { rotuloCurto: 'Médio', rotuloCompleto: 'Médio' },
  '3': { rotuloCurto: 'Argiloso', rotuloCompleto: 'Argiloso' },
  '11': { rotuloCurto: 'AD1', rotuloCompleto: 'AD1' },
  '12': { rotuloCurto: 'AD2', rotuloCompleto: 'AD2' },
  '13': { rotuloCurto: 'AD3', rotuloCompleto: 'AD3' },
  '14': { rotuloCurto: 'AD4', rotuloCompleto: 'AD4' },
  '15': { rotuloCurto: 'AD5', rotuloCompleto: 'AD5' },
  '16': { rotuloCurto: 'AD6', rotuloCompleto: 'AD6' },
};

export function obterRotuloCurtoSolo(solo?: Partial<SoloResumo> | string | null): string {
  if (!solo) return '';
  const codigo = typeof solo === 'string' ? solo.trim() : (solo.codigo?.trim() || '');
  if (SOLOS_MAPA[codigo]) {
    return SOLOS_MAPA[codigo].rotuloCurto;
  }
  if (typeof solo !== 'string') {
    const classif = solo.classificacao?.trim();
    if (classif && !/^\d+$/.test(classif)) {
      return classif;
    }
    const tipo = solo.tipoSolo?.trim();
    if (tipo && !/^\d+$/.test(tipo)) {
      return tipo.replace(/^Solo\s+/i, '');
    }
  }
  return codigo;
}

export function formatarSolosListaTexto(solos?: SoloResumo[] | null): string {
  if (!solos || solos.length === 0) return 'Todos';
  return solos
    .map((s) => obterRotuloCurtoSolo(s))
    .filter(Boolean)
    .join(', ');
}

export function formatarSolosAptosResumo(solos?: SoloResumo[] | null): {
  texto: string;
  tooltip: string;
  badges: string[];
} {
  if (!solos || solos.length === 0) {
    return {
      texto: 'Não informado',
      tooltip: 'Nenhum solo apto catalogado',
      badges: [],
    };
  }

  const codigos = solos.map((s) => s.codigo?.trim()).filter(Boolean);
  const adCodes = ['11', '12', '13', '14', '15', '16'];
  const classicCodes = ['1', '2', '3'];

  const todosSaoAd = codigos.length > 0 && codigos.every((c) => adCodes.includes(c));
  const todosSaoClassicos = codigos.length > 0 && codigos.every((c) => classicCodes.includes(c));

  const badges = solos.map((s) => obterRotuloCurtoSolo(s));
  const tooltip = badges.join(', ');

  if (todosSaoAd) {
    const ordenados = [...codigos].sort((a, b) => Number(a) - Number(b));
    const minNum = Number(ordenados[0]) - 10;
    const maxNum = Number(ordenados[ordenados.length - 1]) - 10;

    if (ordenados.length === 6) {
      return {
        texto: 'AD1 a AD6',
        tooltip,
        badges,
      };
    }

    if (ordenados.length > 1 && maxNum - minNum === ordenados.length - 1) {
      return {
        texto: `AD${minNum} a AD${maxNum}`,
        tooltip,
        badges,
      };
    }

    return {
      texto: badges.join(', '),
      tooltip,
      badges,
    };
  }

  if (todosSaoClassicos) {
    const ordenados = [...codigos].sort((a, b) => Number(a) - Number(b));
    if (ordenados.length === 3) {
      return {
        texto: 'Arenoso, Médio e Argiloso',
        tooltip,
        badges,
      };
    }
    return {
      texto: badges.join(', '),
      tooltip,
      badges,
    };
  }

  return {
    texto: badges.join(', '),
    tooltip,
    badges,
  };
}
