import { getCurrentYearMonth } from './constants';

export interface FilterValues {
  codigoIbge: string;
  nomeMunicipio: string;
  mesInicio: string;
  mesFim: string;
  soloCodigo: string;
  areaHa: string;
}

export type ValidationErrors = Partial<Record<keyof FilterValues, string>>;

export function validarFiltrosDashboard(values: FilterValues): ValidationErrors {
  const errors: ValidationErrors = {};
  const currentYM = getCurrentYearMonth();

  if (!values.codigoIbge || !values.codigoIbge.trim()) {
    errors.codigoIbge = 'Selecione o município de Santa Catarina';
  } else if (!/^42\d{5}$/.test(values.codigoIbge.trim())) {
    errors.codigoIbge = 'Código IBGE deve conter 7 dígitos e iniciar com 42';
  }

  const regexMes = /^\d{4}-(0[1-9]|1[0-2])$/;

  if (!values.mesInicio || !values.mesInicio.trim()) {
    errors.mesInicio = 'Mês de início é obrigatório';
  } else if (!regexMes.test(values.mesInicio.trim())) {
    errors.mesInicio = 'Formato deve ser AAAA-MM';
  } else if (values.mesInicio.trim() < currentYM) {
    errors.mesInicio = `Mês de início deve ser maior ou igual ao mês atual (${currentYM})`;
  }

  if (!values.mesFim || !values.mesFim.trim()) {
    errors.mesFim = 'Mês de fim é obrigatório';
  } else if (!regexMes.test(values.mesFim.trim())) {
    errors.mesFim = 'Formato deve ser AAAA-MM';
  } else if (values.mesInicio && regexMes.test(values.mesInicio.trim())) {
    if (values.mesFim.trim() < values.mesInicio.trim()) {
      errors.mesFim = 'Mês de fim não pode ser anterior ao mês de início';
    } else {
      const [anoIni, mIni] = values.mesInicio.trim().split('-').map(Number);
      const [anoFim, mFim] = values.mesFim.trim().split('-').map(Number);
      const diffMeses = (anoFim - anoIni) * 12 + (mFim - mIni);
      if (diffMeses >= 12) {
        errors.mesFim = 'O período não deve exceder 12 meses';
      }
    }
  }

  if (!values.areaHa || values.areaHa.trim() === '') {
    errors.areaHa = 'Área para plantio é obrigatória (ex: 10 ha)';
  } else {
    const areaNum = parseFloat(values.areaHa.trim());
    if (isNaN(areaNum) || areaNum <= 0) {
      errors.areaHa = 'Área deve ser um valor numérico positivo';
    } else if (areaNum > 1000000) {
      errors.areaHa = 'Área máxima é de 1.000.000 hectares';
    }
  }

  return errors;
}
