import { apiFetch } from './client';
import type {
  ListarMunicipiosResponse,
  DashboardResponse,
  AnaliseCulturaResponse,
  CompararCulturasResponse,
  PerguntaAssistenteRequest,
  RespostaAssistenteResponse,
  ClimaAtualResponse,
} from '../types/api';

export async function getClimaAtual(codigoIbge: string): Promise<ClimaAtualResponse> {
  return apiFetch<ClimaAtualResponse>(`/api/v1/clima/atual/${encodeURIComponent(codigoIbge)}`);
}

export async function getMunicipios(): Promise<ListarMunicipiosResponse> {
  return apiFetch<ListarMunicipiosResponse>('/api/v1/municipios');
}

export async function getDashboard(params: {
  codigoIbge: string;
  mesInicio: string;
  mesFim: string;
  soloCodigo?: string;
  areaHa?: number | string;
}): Promise<DashboardResponse> {
  const query = new URLSearchParams();
  query.set('codigoIbge', params.codigoIbge);
  query.set('mesInicio', params.mesInicio);
  query.set('mesFim', params.mesFim);
  if (params.soloCodigo) query.set('soloCodigo', params.soloCodigo);
  if (params.areaHa && String(params.areaHa).trim() !== '') {
    query.set('areaHa', String(params.areaHa));
  }
  return apiFetch<DashboardResponse>(`/api/v1/dashboard?${query.toString()}`);
}

export async function getAnaliseCultura(
  codigoIbge: string,
  culturaId: number,
  params: {
    mesInicio: string;
    mesFim: string;
    soloCodigo?: string;
    areaHa?: number | string;
  }
): Promise<AnaliseCulturaResponse> {
  const query = new URLSearchParams();
  query.set('mesInicio', params.mesInicio);
  query.set('mesFim', params.mesFim);
  if (params.soloCodigo) query.set('soloCodigo', params.soloCodigo);
  if (params.areaHa && String(params.areaHa).trim() !== '') {
    query.set('areaHa', String(params.areaHa));
  }
  return apiFetch<AnaliseCulturaResponse>(
    `/api/v1/municipios/${codigoIbge}/culturas/${culturaId}/analise?${query.toString()}`
  );
}

export async function getComparador(params: {
  codigoIbge: string;
  mesInicio: string;
  mesFim: string;
  soloCodigo?: string;
  culturaIds: number[];
}): Promise<CompararCulturasResponse> {
  const query = new URLSearchParams();
  query.set('codigoIbge', params.codigoIbge);
  query.set('mesInicio', params.mesInicio);
  query.set('mesFim', params.mesFim);
  if (params.soloCodigo) query.set('soloCodigo', params.soloCodigo);
  params.culturaIds.forEach((id) => query.append('culturaIds', String(id)));
  return apiFetch<CompararCulturasResponse>(`/api/v1/comparador?${query.toString()}`);
}

export async function postPerguntaAssistente(
  payload: PerguntaAssistenteRequest
): Promise<RespostaAssistenteResponse> {
  return apiFetch<RespostaAssistenteResponse>('/api/v1/assistente/perguntar', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
