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
  { codigo: '11', label: 'Solo AD1 (0,34 a 0,46 mm/cm)' },
  { codigo: '12', label: 'Solo AD2 (0,46 a 0,61 mm/cm)' },
  { codigo: '13', label: 'Solo AD3 (0,61 a 0,80 mm/cm)' },
  { codigo: '14', label: 'Solo AD4 (0,80 a 1,06 mm/cm)' },
  { codigo: '15', label: 'Solo AD5 (1,06 a 1,40 mm/cm)' },
  { codigo: '16', label: 'Solo AD6 (≥ 1,40 mm/cm)' },
  { codigo: '1', label: 'Solo Arenoso' },
  { codigo: '2', label: 'Solo Textura Média' },
  { codigo: '3', label: 'Solo Argiloso' },
];
