export function getCurrentYearMonth(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
}

export function addMonthsToYearMonth(yearMonth: string, monthsToAdd: number): string {
  if (!yearMonth) return '';
  const [yStr, mStr] = yearMonth.split('-');
  const date = new Date(parseInt(yStr, 10), parseInt(mStr, 10) - 1, 1);
  date.setMonth(date.getMonth() + monthsToAdd);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}`;
}

export const DEFAULT_CODIGO_IBGE = '';
export const DEFAULT_NOME_MUNICIPIO = '';
export const DEFAULT_MES_INICIO = '';
export const DEFAULT_MES_FIM = '';
export const DEFAULT_SOLO_CODIGO = '';
export const DEFAULT_AREA_HA = '';

export const SOLOS_CONFIG = [
  { codigo: '', label: 'Todos os tipos de solo' },
  { codigo: '1', label: 'Tipo 1 — Solo Arenoso' },
  { codigo: '2', label: 'Tipo 2 — Textura Média' },
  { codigo: '3', label: 'Tipo 3 — Solo Argiloso' },
];
