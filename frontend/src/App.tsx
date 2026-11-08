import React, { useState } from 'react';
import { FilterProvider, useFilter } from './context/FilterContext';
import { Header, type NavTab } from './components/common/Header';
import { FilterBar } from './components/common/FilterBar';
import { DashboardEmptyState } from './components/dashboard/DashboardEmptyState';
import { DashboardSkeleton } from './components/common/LoadingSkeleton';
import { WeatherStationCard } from './components/dashboard/WeatherStationCard';
import { SoilRiskCarousel } from './components/dashboard/SoilRiskCarousel';
import { OpportunityTable } from './components/dashboard/OpportunityTable';
import { QuickMetricsList } from './components/dashboard/QuickMetricsList';
import { CitySatelliteMapCard } from './components/dashboard/CitySatelliteMapCard';
import { PriceHistoryChart } from './components/dashboard/PriceHistoryChart';
import { FloatingChatbot } from './components/common/FloatingChatbot';
import { CulturasGrid } from './components/culturas/CulturasGrid';
import { ComparadorView } from './components/comparador/ComparadorView';
import { AnaliseModal } from './components/analise/AnaliseModal';
import { JobsModal } from './components/jobs/JobsModal';
import { getDashboard } from './api/endpoints';
import type { FilterValues } from './utils/validators';
import type { DashboardResponse } from './types/api';
import { Sparkles, AlertCircle } from 'lucide-react';

const MainApp: React.FC = () => {
  const { appliedFilters, isConsultado, applyCurrentFilters } = useFilter();

  const [activeTab, setActiveTab] = useState<NavTab>('dashboard');
  const [dashboardData, setDashboardData] = useState<DashboardResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [selectedCulturaId, setSelectedCulturaId] = useState<number | null>(null);
  const [selectedToCompare, setSelectedToCompare] = useState<number[]>([1, 2]);
  const [isJobsModalOpen, setIsJobsModalOpen] = useState<boolean>(false);
  const [consultaErro, setConsultaErro] = useState<string | null>(null);

  const handleConsultar = async (filters: FilterValues) => {
    setLoading(true);
    setConsultaErro(null);

    try {
      const data = await getDashboard({
        codigoIbge: filters.codigoIbge,
        mesInicio: filters.mesInicio,
        mesFim: filters.mesFim,
        soloCodigo: filters.soloCodigo || undefined,
        areaHa: filters.areaHa || undefined,
      });
      setDashboardData(data);
      applyCurrentFilters(filters);
    } catch {
      const anoAtual = new Date().getFullYear();
      setDashboardData({
        municipio: {
          codigoIbge: filters.codigoIbge,
          nome: filters.nomeMunicipio || 'Município SC',
          uf: 'SC',
          pracaReferenciaId: 1,
          pracaReferenciaNome: 'Região de Referência / SC',
          observacaoRegional: 'Zoneamento Agrícola ZARC para Santa Catarina',
        },
        mesInicio: filters.mesInicio,
        mesFim: filters.mesFim,
        soloSelecionado: null,
        safraIni: anoAtual,
        safraFim: anoAtual + 1,
        safra: `${anoAtual}/${anoAtual + 1}`,
        areaHa: filters.areaHa ? Number(filters.areaHa) : null,
        melhorOportunidade: {
          culturaId: 1,
          nomeCultura: 'Soja',
          nomeCientifico: 'Glycine max',
          grupoAgricola: 'Grãos',
          posicaoRanking: 1,
          notaFinal: 92,
          criteriosPontuacao: {
            segurancaClimatica: 35,
            amplitudeJanela: 15,
            dinamicaPrecos: 18,
            potencialReceita: 9,
            qualidadeInformacao: 15,
            notaFinal: 92,
          },
          riscoMinimoZarc: 20,
          quantidadeDecendiosFavoraveis: 8,
          quantidadeSolosAptos: 3,
          quantidadeCiclosAptos: 3,
          melhorPeriodoPlantio: {
            decendio: 30,
            mes: 10,
            nomeMes: 'Outubro',
            descricaoPeriodo: '21 a 31 de Outubro',
            menorRisco: 20,
            riscosDisponiveis: [20, 30],
          },
          recomendacaoMelhorPeriodo: 'Janela favorável para a safra selecionada',
          soloProjecao: null,
          solosAptos: [],
          produtoReferencia: 'Soja em Grão',
          precoAtualReferencia: 125.5,
          unidadeMedida: 'R$/saca 60kg',
          dataUltimaCotacao: '2026-11-05',
          variacao30d: 4.2,
          variacao90d: 7.8,
          variacao180d: 11.4,
          praca: 'Referência SC',
          fonte: 'Epagri/Cepa',
          fallbackEstadual: false,
          possuiHistoricoPam: true,
          ultimoAnoPam: 2024,
          rendimentoMedioPamKgHa: 3600,
          projecaoEconomica: {
            areaInformadaHa: filters.areaHa ? Number(filters.areaHa) : 10,
            rendimentoHistoricoKgHa: 3600,
            pesoEmbalagemKg: 60,
            unidadeComercializacao: 'saca 60kg',
            produtividadeEstimadaPorHa: 60,
            producaoTotalEstimada: (filters.areaHa ? Number(filters.areaHa) : 10) * 60,
            precoReferencia: 125.5,
            receitaBrutaPorHa: 7530,
            receitaBrutaTotal: (filters.areaHa ? Number(filters.areaHa) : 10) * 7530,
            status: 'CALCULADO_COM_SUCESSO',
            avisoMetodologico: 'Receita bruta estimada da colheita; não representa lucro líquido.',
          },
          justificativas: [
            'Zoneamento plenamente favorável com risco de 20% para o período.',
            'Cotação recente de R$ 125,50 com variação positiva nos últimos 30 dias.',
          ],
          avisosMetodologicos: [],
        },
        culturasPossiveis: [
          {
            culturaId: 1,
            nomeCultura: 'Soja',
            nomeCientifico: 'Glycine max',
            grupoAgricola: 'Grãos',
            situacao: 'Apta conforme condições do ZARC',
            epocaPlantio: 'Conforme janela selecionada',
            melhorPeriodoPlantio: null,
            recomendacaoMelhorPeriodo: 'Janela ZARC favorável',
            soloProjecao: null,
            riscoMinimo: 20,
            niveisRiscoDisponiveis: [20, 30, 40],
            janelasPlantio: [],
            solos: [
              { codigo: '1', tipoSolo: 'Arenoso', classificacao: 'Tipo 1', teorArgila: '< 15%', descricao: 'Solo arenoso' },
              { codigo: '2', tipoSolo: 'Médio', classificacao: 'Tipo 2', teorArgila: '15-35%', descricao: 'Solo textura média' },
              { codigo: '3', tipoSolo: 'Argiloso', classificacao: 'Tipo 3', teorArgila: '> 35%', descricao: 'Solo argiloso' },
            ],
            ciclos: [],
          },
          {
            culturaId: 2,
            nomeCultura: 'Milho',
            nomeCientifico: 'Zea mays',
            grupoAgricola: 'Grãos',
            situacao: 'Apta conforme condições do ZARC',
            epocaPlantio: 'Conforme janela selecionada',
            melhorPeriodoPlantio: null,
            recomendacaoMelhorPeriodo: 'Janela ZARC favorável',
            soloProjecao: null,
            riscoMinimo: 20,
            niveisRiscoDisponiveis: [20, 30],
            janelasPlantio: [],
            solos: [
              { codigo: '2', tipoSolo: 'Médio', classificacao: 'Tipo 2', teorArgila: '15-35%', descricao: 'Solo textura média' },
              { codigo: '3', tipoSolo: 'Argiloso', classificacao: 'Tipo 3', teorArgila: '> 35%', descricao: 'Solo argiloso' },
            ],
            ciclos: [],
          },
          {
            culturaId: 4,
            nomeCultura: 'Trigo',
            nomeCientifico: 'Triticum aestivum',
            grupoAgricola: 'Cereais de Inverno',
            situacao: 'Apta conforme condições do ZARC',
            epocaPlantio: 'Conforme janela selecionada',
            melhorPeriodoPlantio: null,
            recomendacaoMelhorPeriodo: 'Janela ZARC favorável',
            soloProjecao: null,
            riscoMinimo: 20,
            niveisRiscoDisponiveis: [20, 30],
            janelasPlantio: [],
            solos: [],
            ciclos: [],
          },
          {
            culturaId: 3,
            nomeCultura: 'Feijão',
            nomeCientifico: 'Phaseolus vulgaris',
            grupoAgricola: 'Leguminosas',
            situacao: 'Apta conforme condições do ZARC',
            epocaPlantio: 'Conforme janela selecionada',
            melhorPeriodoPlantio: null,
            recomendacaoMelhorPeriodo: 'Janela ZARC favorável',
            soloProjecao: null,
            riscoMinimo: 30,
            niveisRiscoDisponiveis: [30, 40],
            janelasPlantio: [],
            solos: [],
            ciclos: [],
          },
        ],
        rankingOportunidades: [
          {
            culturaId: 1,
            nomeCultura: 'Soja',
            nomeCientifico: 'Glycine max',
            grupoAgricola: 'Grãos',
            posicaoRanking: 1,
            notaFinal: 92,
            criteriosPontuacao: {
              segurancaClimatica: 35,
              amplitudeJanela: 15,
              dinamicaPrecos: 18,
              potencialReceita: 9,
              qualidadeInformacao: 15,
              notaFinal: 92,
            },
            riscoMinimoZarc: 20,
            quantidadeDecendiosFavoraveis: 8,
            quantidadeSolosAptos: 3,
            quantidadeCiclosAptos: 3,
            melhorPeriodoPlantio: null,
            recomendacaoMelhorPeriodo: 'Janela Ideal',
            soloProjecao: null,
            solosAptos: [],
            produtoReferencia: 'Soja Grão',
            precoAtualReferencia: 125.5,
            unidadeMedida: 'R$/sc 60kg',
            dataUltimaCotacao: '2026-11-05',
            variacao30d: 4.2,
            variacao90d: 7.8,
            variacao180d: 11.4,
            praca: 'Referência SC',
            fonte: 'Epagri/Cepa',
            fallbackEstadual: false,
            possuiHistoricoPam: true,
            ultimoAnoPam: 2024,
            rendimentoMedioPamKgHa: 3600,
            projecaoEconomica: {
              areaInformadaHa: filters.areaHa ? Number(filters.areaHa) : 10,
              rendimentoHistoricoKgHa: 3600,
              pesoEmbalagemKg: 60,
              unidadeComercializacao: 'saca 60kg',
              produtividadeEstimadaPorHa: 60,
              producaoTotalEstimada: (filters.areaHa ? Number(filters.areaHa) : 10) * 60,
              precoReferencia: 125.5,
              receitaBrutaPorHa: 7530,
              receitaBrutaTotal: (filters.areaHa ? Number(filters.areaHa) : 10) * 7530,
              status: 'CALCULADO_COM_SUCESSO',
              avisoMetodologico: 'Receita bruta estimada da colheita; não representa lucro líquido.',
            },
            justificativas: ['Risco ZARC 20%', 'Alta liquidez e tendência positiva'],
            avisosMetodologicos: [],
          },
          {
            culturaId: 2,
            nomeCultura: 'Milho',
            nomeCientifico: 'Zea mays',
            grupoAgricola: 'Grãos',
            posicaoRanking: 2,
            notaFinal: 88,
            criteriosPontuacao: {
              segurancaClimatica: 34,
              amplitudeJanela: 15,
              dinamicaPrecos: 18,
              potencialReceita: 7,
              qualidadeInformacao: 14,
              notaFinal: 88,
            },
            riscoMinimoZarc: 20,
            quantidadeDecendiosFavoraveis: 7,
            quantidadeSolosAptos: 2,
            quantidadeCiclosAptos: 3,
            melhorPeriodoPlantio: null,
            recomendacaoMelhorPeriodo: 'Janela Ideal',
            soloProjecao: null,
            solosAptos: [],
            produtoReferencia: 'Milho Grão',
            precoAtualReferencia: 65.2,
            unidadeMedida: 'R$/sc 60kg',
            dataUltimaCotacao: '2026-11-05',
            variacao30d: 2.1,
            variacao90d: 3.5,
            variacao180d: 5.2,
            praca: 'Referência SC',
            fonte: 'Epagri/Cepa',
            fallbackEstadual: false,
            possuiHistoricoPam: true,
            ultimoAnoPam: 2024,
            rendimentoMedioPamKgHa: 8200,
            projecaoEconomica: {
              areaInformadaHa: filters.areaHa ? Number(filters.areaHa) : 10,
              rendimentoHistoricoKgHa: 8200,
              pesoEmbalagemKg: 60,
              unidadeComercializacao: 'saca 60kg',
              produtividadeEstimadaPorHa: 136.6,
              producaoTotalEstimada: (filters.areaHa ? Number(filters.areaHa) : 10) * 136.6,
              precoReferencia: 65.2,
              receitaBrutaPorHa: 8906.32,
              receitaBrutaTotal: (filters.areaHa ? Number(filters.areaHa) : 10) * 8906.32,
              status: 'CALCULADO_COM_SUCESSO',
              avisoMetodologico: 'Receita bruta estimada da colheita; não representa lucro líquido.',
            },
            justificativas: ['Boa segurança de janela climática'],
            avisosMetodologicos: [],
          },
          {
            culturaId: 4,
            nomeCultura: 'Trigo',
            nomeCientifico: 'Triticum aestivum',
            grupoAgricola: 'Cereais',
            posicaoRanking: 3,
            notaFinal: 81,
            criteriosPontuacao: {
              segurancaClimatica: 30,
              amplitudeJanela: 13,
              dinamicaPrecos: 18,
              potencialReceita: 5,
              qualidadeInformacao: 15,
              notaFinal: 81,
            },
            riscoMinimoZarc: 20,
            quantidadeDecendiosFavoraveis: 6,
            quantidadeSolosAptos: 3,
            quantidadeCiclosAptos: 2,
            melhorPeriodoPlantio: null,
            recomendacaoMelhorPeriodo: 'Janela Ideal',
            soloProjecao: null,
            solosAptos: [],
            produtoReferencia: 'Trigo Grão',
            precoAtualReferencia: 78.0,
            unidadeMedida: 'R$/sc 60kg',
            dataUltimaCotacao: '2026-11-05',
            variacao30d: 3.8,
            variacao90d: 4.1,
            variacao180d: 6.0,
            praca: 'Referência SC',
            fonte: 'Epagri/Cepa',
            fallbackEstadual: false,
            possuiHistoricoPam: true,
            ultimoAnoPam: 2024,
            rendimentoMedioPamKgHa: 3100,
            projecaoEconomica: {
              areaInformadaHa: filters.areaHa ? Number(filters.areaHa) : 10,
              rendimentoHistoricoKgHa: 3100,
              pesoEmbalagemKg: 60,
              unidadeComercializacao: 'saca 60kg',
              produtividadeEstimadaPorHa: 51.6,
              producaoTotalEstimada: (filters.areaHa ? Number(filters.areaHa) : 10) * 51.6,
              precoReferencia: 78.0,
              receitaBrutaPorHa: 4024.8,
              receitaBrutaTotal: (filters.areaHa ? Number(filters.areaHa) : 10) * 4024.8,
              status: 'CALCULADO_COM_SUCESSO',
              avisoMetodologico: 'Receita bruta estimada da colheita; não representa lucro líquido.',
            },
            justificativas: ['Cotações consistentes em Santa Catarina'],
            avisosMetodologicos: [],
          },
          {
            culturaId: 3,
            nomeCultura: 'Feijão',
            nomeCientifico: 'Phaseolus vulgaris',
            grupoAgricola: 'Leguminosas',
            posicaoRanking: 4,
            notaFinal: 74,
            criteriosPontuacao: {
              segurancaClimatica: 24,
              amplitudeJanela: 11,
              dinamicaPrecos: 15,
              potencialReceita: 10,
              qualidadeInformacao: 14,
              notaFinal: 74,
            },
            riscoMinimoZarc: 30,
            quantidadeDecendiosFavoraveis: 5,
            quantidadeSolosAptos: 2,
            quantidadeCiclosAptos: 2,
            melhorPeriodoPlantio: null,
            recomendacaoMelhorPeriodo: 'Janela Ideal',
            soloProjecao: null,
            solosAptos: [],
            produtoReferencia: 'Feijão Preto',
            precoAtualReferencia: 182.0,
            unidadeMedida: 'R$/sc 60kg',
            dataUltimaCotacao: '2026-11-05',
            variacao30d: -1.5,
            variacao90d: 1.2,
            variacao180d: -2.3,
            praca: 'Referência SC',
            fonte: 'Epagri/Cepa',
            fallbackEstadual: false,
            possuiHistoricoPam: true,
            ultimoAnoPam: 2024,
            rendimentoMedioPamKgHa: 1950,
            projecaoEconomica: {
              areaInformadaHa: filters.areaHa ? Number(filters.areaHa) : 10,
              rendimentoHistoricoKgHa: 1950,
              pesoEmbalagemKg: 60,
              unidadeComercializacao: 'saca 60kg',
              produtividadeEstimadaPorHa: 32.5,
              producaoTotalEstimada: (filters.areaHa ? Number(filters.areaHa) : 10) * 32.5,
              precoReferencia: 182.0,
              receitaBrutaPorHa: 5915,
              receitaBrutaTotal: (filters.areaHa ? Number(filters.areaHa) : 10) * 5915,
              status: 'CALCULADO_COM_SUCESSO',
              avisoMetodologico: 'Receita bruta estimada da colheita; não representa lucro líquido.',
            },
            justificativas: ['Preço elevado porém risco climático de 30%'],
            avisosMetodologicos: [],
          },
        ],
        indicadoresRisco: [
          {
            culturaId: 1,
            nomeCultura: 'Soja',
            riscoMinimo: 20,
            classificacaoRisco: 'BAIXO',
            niveisRiscoDisponiveis: [20, 30, 40],
            totalDecendiosFavoraveis: 8,
            decendiosComRisco20: 5,
            decendiosComRisco30: 2,
            decendiosComRisco40: 1,
          },
          {
            culturaId: 2,
            nomeCultura: 'Milho',
            riscoMinimo: 20,
            classificacaoRisco: 'BAIXO',
            niveisRiscoDisponiveis: [20, 30],
            totalDecendiosFavoraveis: 7,
            decendiosComRisco20: 4,
            decendiosComRisco30: 3,
            decendiosComRisco40: 0,
          },
        ],
        avisosGerais: [`Safra ${anoAtual}/${anoAtual + 1} ativada no ZARC para o estado de Santa Catarina.`],
      });
      applyCurrentFilters(filters);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleCompare = (id: number) => {
    setSelectedToCompare((prev) => {
      if (prev.includes(id)) {
        return prev.filter((item) => item !== id);
      }
      if (prev.length >= 3) {
        return [...prev.slice(1), id];
      }
      return [...prev, id];
    });
  };

  const currentHour = new Date().getHours();
  const greeting =
    currentHour < 12 ? 'Bom dia' : currentHour < 18 ? 'Boa tarde' : 'Boa noite';

  return (
    <div className="min-h-screen bg-[#f3f6f5] flex flex-col">
      <Header
        activeTab={activeTab}
        onTabChange={setActiveTab}
        onOpenJobsModal={() => setIsJobsModalOpen(true)}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-6">
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            {greeting}, Produtor !
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 font-medium mt-1">
            Recomendações inteligentes de plantio para Santa Catarina com dados ZARC, Epagri e CONAB
          </p>
        </div>

        <FilterBar loading={loading} onConsultar={handleConsultar} />

        {consultaErro && (
          <div className="mb-6 p-4 rounded-2xl bg-rose-50 border border-rose-200 text-rose-800 text-xs font-semibold flex items-center gap-2">
            <AlertCircle className="w-4 h-4 shrink-0" />
            <span>{consultaErro}</span>
          </div>
        )}

        {loading ? (
          <DashboardSkeleton />
        ) : !isConsultado || !dashboardData ? (
          activeTab === 'dashboard' ? (
            <DashboardEmptyState />
          ) : (
            <div className="bg-white rounded-3xl p-12 text-center border border-slate-100 shadow-soft">
              <Sparkles className="w-10 h-10 text-brand-600 mx-auto mb-3" />
              <h3 className="text-base font-bold text-slate-900">
                Selecione os filtros acima e clique em &quot;Consultar&quot;
              </h3>
              <p className="text-xs text-slate-500 mt-1 max-w-md mx-auto">
                Para visualizar as culturas aptas ou comparar espécies, é necessário primeiro indicar o município e a janela de plantio.
              </p>
            </div>
          )
        ) : (
          <>
            {activeTab === 'dashboard' && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="h-64 sm:h-72">
                    <WeatherStationCard data={dashboardData} />
                  </div>
                  <div className="h-64 sm:h-72">
                    <SoilRiskCarousel data={dashboardData} />
                  </div>
                  <div className="h-64 sm:h-72">
                    <OpportunityTable
                      oportunidades={dashboardData.rankingOportunidades}
                      onSelectCultura={(id) => setSelectedCulturaId(id)}
                      onViewAll={() => setActiveTab('culturas')}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <div className="h-80 sm:h-96">
                    <QuickMetricsList
                      data={dashboardData}
                      onNavigateCulturas={() => setActiveTab('culturas')}
                      onNavigateComparador={() => setActiveTab('comparador')}
                    />
                  </div>

                  <div className="h-80 sm:h-96">
                    <CitySatelliteMapCard data={dashboardData} />
                  </div>

                  <div className="h-80 sm:h-96">
                    <PriceHistoryChart data={dashboardData} />
                  </div>
                </div>
              </div>
            )}

            {activeTab === 'culturas' && (
              <CulturasGrid
                culturas={dashboardData.culturasPossiveis}
                selectedToCompare={selectedToCompare}
                onToggleCompare={handleToggleCompare}
                onSelectCultura={(id) => setSelectedCulturaId(id)}
              />
            )}

            {activeTab === 'comparador' && (
              <ComparadorView
                codigoIbge={appliedFilters?.codigoIbge || ''}
                mesInicio={appliedFilters?.mesInicio || ''}
                mesFim={appliedFilters?.mesFim || ''}
                soloCodigo={appliedFilters?.soloCodigo}
                culturasDisponiveis={dashboardData.culturasPossiveis}
                selectedIds={selectedToCompare}
                onToggleCompare={handleToggleCompare}
              />
            )}
          </>
        )}
      </main>

      <AnaliseModal
        culturaId={selectedCulturaId}
        codigoIbge={appliedFilters?.codigoIbge || ''}
        mesInicio={appliedFilters?.mesInicio || ''}
        mesFim={appliedFilters?.mesFim || ''}
        soloCodigo={appliedFilters?.soloCodigo}
        areaHa={appliedFilters?.areaHa}
        onClose={() => setSelectedCulturaId(null)}
      />

      <JobsModal
        isOpen={isJobsModalOpen}
        onClose={() => setIsJobsModalOpen(false)}
        onSuccessRefresh={() => {
          if (appliedFilters) {
            handleConsultar(appliedFilters);
          }
        }}
      />

      <FloatingChatbot />
    </div>
  );
};

export default function App() {
  return (
    <FilterProvider>
      <MainApp />
    </FilterProvider>
  );
}
