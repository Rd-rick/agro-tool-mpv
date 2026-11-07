package com.figueredo.ricardo.mvp.mercado.application.service;

import com.figueredo.ricardo.mvp.common.domain.exception.MunicipioNaoEncontradoException;
import com.figueredo.ricardo.mvp.mercado.application.port.out.MercadoConsultaPort;
import com.figueredo.ricardo.mvp.mercado.domain.model.CotacaoPrecoItem;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.CulturaMercadoResultado;
import com.figueredo.ricardo.mvp.mercado.domain.model.MunicipioMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoCotacaoMercado;
import com.figueredo.ricardo.mvp.mercado.domain.model.ProdutoMercadoAnalise;
import com.figueredo.ricardo.mvp.mercado.domain.model.RegiaoPracaMercado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarMercadoCulturasServiceTest {

    @Mock
    private MercadoConsultaPort mercadoConsultaPort;

    @InjectMocks
    private ConsultarMercadoCulturasService service;

    @Test
    void deveConsultarCotacoesNaPracaDeReferenciaDoMunicipioCalculandoVariacaoEHistoricos30_90_180Dias() {
        String codigoIbge = "4204806";
        MunicipioMercado curitibanos = new MunicipioMercado(
                93L,
                codigoIbge,
                "Curitibanos",
                "SC",
                7L,
                "Praça regional Meio Oeste"
        );

        when(mercadoConsultaPort.buscarMunicipioPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(curitibanos));
        when(mercadoConsultaPort.buscarCulturasAtivasPorIds(List.of(1L)))
                .thenReturn(List.of(new CulturaMercado(1L, "Soja")));
        when(mercadoConsultaPort.buscarProdutosAtivosPorCulturasEFonte(List.of(1L), "EPAGRI_CEPA"))
                .thenReturn(List.of(new ProdutoCotacaoMercado(
                        10L,
                        1L,
                        "Soja industrial",
                        "industrial",
                        "Saca",
                        new BigDecimal("60.00"),
                        "EPAGRI_CEPA"
                )));
        when(mercadoConsultaPort.buscarPracasAtivasPorUf("SC"))
                .thenReturn(List.of(new RegiaoPracaMercado(7L, "Meio Oeste", "SC")));

        LocalDate dataFim = LocalDate.of(2026, 10, 30);
        when(mercadoConsultaPort.buscarUltimaDataCotacao(10L, 7L, "DIARIA"))
                .thenReturn(Optional.of(dataFim));

        List<CotacaoPrecoItem> serie = List.of(
                new CotacaoPrecoItem(10L, 7L, dataFim.minusDays(180), new BigDecimal("98.00"), new BigDecimal("100.00"), new BigDecimal("102.00"), "DIARIA"),
                new CotacaoPrecoItem(10L, 7L, dataFim.minusDays(90), new BigDecimal("108.00"), new BigDecimal("110.00"), new BigDecimal("112.00"), "DIARIA"),
                new CotacaoPrecoItem(10L, 7L, dataFim.minusDays(30), new BigDecimal("118.00"), new BigDecimal("120.00"), new BigDecimal("122.00"), "DIARIA"),
                new CotacaoPrecoItem(10L, 7L, dataFim, new BigDecimal("124.00"), new BigDecimal("126.00"), new BigDecimal("128.00"), "DIARIA")
        );

        when(mercadoConsultaPort.buscarSeriePorPracaEPeriodo(10L, 7L, "DIARIA", dataFim.minusDays(180), dataFim))
                .thenReturn(serie);

        List<CulturaMercadoResultado> resultados = service.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(1L));

        assertEquals(1, resultados.size());
        CulturaMercadoResultado soja = resultados.get(0);
        assertEquals("Soja", soja.nomeCultura());
        assertEquals(1, soja.produtos().size());

        ProdutoMercadoAnalise produto = soja.produtos().get(0);
        assertEquals("Soja industrial", produto.nomeComercial());
        assertEquals(new BigDecimal("126.00"), produto.precoAtualReferencia());
        assertFalse(produto.origem().fallbackEstadual());
        assertEquals("Meio Oeste", produto.origem().pracaNome());
        assertEquals(7L, produto.origem().pracaId());

        assertEquals(new BigDecimal("5.00"), produto.variacao30DiasPercentual());
        assertEquals(new BigDecimal("14.55"), produto.variacao90DiasPercentual());
        assertEquals(new BigDecimal("26.00"), produto.variacao180DiasPercentual());

        assertEquals(2, produto.historico30Dias().serieHistorica().size());
        assertEquals(3, produto.historico90Dias().serieHistorica().size());
        assertEquals(4, produto.historico180Dias().serieHistorica().size());
    }

    @Test
    void deveExecutarFallbackTransparenteParaMediaEstadualScQuandoPracaLocalNaoPossuirCotacao() {
        String codigoIbge = "4205407";
        MunicipioMercado florianopolis = new MunicipioMercado(
                103L,
                codigoIbge,
                "Florianópolis",
                "SC",
                4L,
                "Grande Florianópolis"
        );

        when(mercadoConsultaPort.buscarMunicipioPorCodigoIbge(codigoIbge)).thenReturn(Optional.of(florianopolis));
        when(mercadoConsultaPort.buscarCulturasAtivasPorIds(List.of(1L)))
                .thenReturn(List.of(new CulturaMercado(1L, "Soja")));
        when(mercadoConsultaPort.buscarProdutosAtivosPorCulturasEFonte(List.of(1L), "EPAGRI_CEPA"))
                .thenReturn(List.of(new ProdutoCotacaoMercado(
                        10L,
                        1L,
                        "Soja industrial",
                        "industrial",
                        "Saca",
                        new BigDecimal("60.00"),
                        "EPAGRI_CEPA"
                )));
        when(mercadoConsultaPort.buscarPracasAtivasPorUf("SC"))
                .thenReturn(List.of(
                        new RegiaoPracaMercado(4L, "Grande Florianópolis", "SC"),
                        new RegiaoPracaMercado(7L, "Meio Oeste", "SC"),
                        new RegiaoPracaMercado(10L, "Planalto Sul", "SC")
                ));

        when(mercadoConsultaPort.buscarUltimaDataCotacao(10L, 4L, "DIARIA")).thenReturn(Optional.empty());
        when(mercadoConsultaPort.buscarUltimaDataCotacao(10L, 4L, "SEMANAL")).thenReturn(Optional.empty());

        LocalDate dataFim = LocalDate.of(2026, 10, 30);
        when(mercadoConsultaPort.buscarUltimaDataCotacaoEmPracas(eq(10L), any(), eq("DIARIA")))
                .thenReturn(Optional.of(dataFim));

        List<CotacaoPrecoItem> serieEstadual = List.of(
                new CotacaoPrecoItem(10L, 7L, dataFim.minusDays(30), new BigDecimal("118.00"), new BigDecimal("120.00"), new BigDecimal("122.00"), "DIARIA"),
                new CotacaoPrecoItem(10L, 10L, dataFim.minusDays(30), new BigDecimal("126.00"), new BigDecimal("130.00"), new BigDecimal("132.00"), "DIARIA"),
                new CotacaoPrecoItem(10L, 7L, dataFim, new BigDecimal("128.00"), new BigDecimal("130.00"), new BigDecimal("132.00"), "DIARIA"),
                new CotacaoPrecoItem(10L, 10L, dataFim, new BigDecimal("138.00"), new BigDecimal("140.00"), new BigDecimal("142.00"), "DIARIA")
        );

        when(mercadoConsultaPort.buscarSerieEmPracasEPeriodo(
                eq(10L),
                eq(Set.of(4L, 7L, 10L)),
                eq("DIARIA"),
                eq(dataFim.minusDays(180)),
                eq(dataFim)
        )).thenReturn(serieEstadual);

        List<CulturaMercadoResultado> resultados = service.consultarMercadoPorMunicipioECulturas(codigoIbge, List.of(1L));

        assertEquals(1, resultados.size());
        ProdutoMercadoAnalise produto = resultados.get(0).produtos().get(0);

        assertTrue(produto.origem().fallbackEstadual());
        assertNull(produto.origem().pracaId());
        assertEquals("Média Estadual SC", produto.origem().pracaNome());
        assertTrue(produto.origem().observacaoOrigem().contains("Meio Oeste, Planalto Sul"));
        assertEquals(new BigDecimal("135.00"), produto.precoAtualReferencia());
        assertEquals(new BigDecimal("8.00"), produto.variacao30DiasPercentual());
    }

    @Test
    void deveLancarMunicipioNaoEncontradoExceptionQuandoMunicipioNaoExistir() {
        when(mercadoConsultaPort.buscarMunicipioPorCodigoIbge("4299999")).thenReturn(Optional.empty());

        assertThrows(
                MunicipioNaoEncontradoException.class,
                () -> service.consultarMercadoPorMunicipioECulturas("4299999", List.of(1L))
        );
    }
}
