import React, { createContext, useContext, useState } from 'react';
import {
  DEFAULT_CODIGO_IBGE,
  DEFAULT_NOME_MUNICIPIO,
  DEFAULT_MES_INICIO,
  DEFAULT_MES_FIM,
  DEFAULT_SOLO_CODIGO,
  DEFAULT_AREA_HA,
  getCurrentYearMonth,
  addMonthsToYearMonth,
} from '../utils/constants';
import type { FilterValues } from '../utils/validators';
import type { MunicipioItem } from '../types/api';

interface FilterContextType {
  codigoIbge: string;
  nomeMunicipio: string;
  mesInicio: string;
  mesFim: string;
  soloCodigo: string;
  areaHa: string;
  minMesInicio: string;
  maxMesFim: string;
  appliedFilters: FilterValues | null;
  isConsultado: boolean;
  setCodigoIbge: (val: string) => void;
  setNomeMunicipio: (val: string) => void;
  setMesInicio: (val: string) => void;
  setMesFim: (val: string) => void;
  setSoloCodigo: (val: string) => void;
  setAreaHa: (val: string) => void;
  setMunicipio: (municipio: MunicipioItem) => void;
  setPeriodo: (inicio: string, fim: string) => void;
  applyCurrentFilters: (filters: FilterValues) => void;
  resetFilters: () => void;
  fillCuritibanosPreset: () => void;
}

const FilterContext = createContext<FilterContextType | undefined>(undefined);

export const FilterProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const minMesInicio = getCurrentYearMonth();

  const [codigoIbge, setCodigoIbge] = useState<string>(DEFAULT_CODIGO_IBGE);
  const [nomeMunicipio, setNomeMunicipio] = useState<string>(DEFAULT_NOME_MUNICIPIO);
  const [mesInicio, setMesInicioState] = useState<string>(DEFAULT_MES_INICIO);
  const [mesFim, setMesFimState] = useState<string>(DEFAULT_MES_FIM);
  const [soloCodigo, setSoloCodigo] = useState<string>(DEFAULT_SOLO_CODIGO);
  const [areaHa, setAreaHa] = useState<string>(DEFAULT_AREA_HA);

  const [appliedFilters, setAppliedFilters] = useState<FilterValues | null>(null);

  const maxMesFim = mesInicio ? addMonthsToYearMonth(mesInicio, 11) : '';

  const setMunicipio = (m: MunicipioItem) => {
    setCodigoIbge(m.codigoIbge);
    setNomeMunicipio(m.nome);
  };

  const setMesInicio = (val: string) => {
    setMesInicioState(val);
    if (val && mesFim && mesFim < val) {
      setMesFimState(val);
    }
  };

  const setMesFim = (val: string) => {
    setMesFimState(val);
  };

  const setPeriodo = (inicio: string, fim: string) => {
    setMesInicioState(inicio);
    setMesFimState(fim);
  };

  const applyCurrentFilters = (filters: FilterValues) => {
    setAppliedFilters(filters);
  };

  const resetFilters = () => {
    setCodigoIbge(DEFAULT_CODIGO_IBGE);
    setNomeMunicipio(DEFAULT_NOME_MUNICIPIO);
    setMesInicioState(DEFAULT_MES_INICIO);
    setMesFimState(DEFAULT_MES_FIM);
    setSoloCodigo(DEFAULT_SOLO_CODIGO);
    setAreaHa(DEFAULT_AREA_HA);
    setAppliedFilters(null);
  };

  const fillCuritibanosPreset = () => {
    setCodigoIbge('4204809');
    setNomeMunicipio('Curitibanos');
    const start = getCurrentYearMonth();
    setMesInicioState(start);
    setMesFimState(addMonthsToYearMonth(start, 5));
    setSoloCodigo('2');
  };

  return (
    <FilterContext.Provider
      value={{
        codigoIbge,
        nomeMunicipio,
        mesInicio,
        mesFim,
        soloCodigo,
        areaHa,
        minMesInicio,
        maxMesFim,
        appliedFilters,
        isConsultado: appliedFilters !== null,
        setCodigoIbge,
        setNomeMunicipio,
        setMesInicio,
        setMesFim,
        setSoloCodigo,
        setAreaHa,
        setMunicipio,
        setPeriodo,
        applyCurrentFilters,
        resetFilters,
        fillCuritibanosPreset,
      }}
    >
      {children}
    </FilterContext.Provider>
  );
};

export function useFilter() {
  const context = useContext(FilterContext);
  if (!context) {
    throw new Error('useFilter must be used within a FilterProvider');
  }
  return context;
}
