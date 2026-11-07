package com.figueredo.ricardo.mvp.assistente.application.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AgroToolFunctionsExecutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.ChamadaFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.ResultadoFerramentaIa;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CalcularRankingOportunidadesUseCase.FiltroRankingOportunidades;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.CompararCulturasUseCase.FiltroCompararCulturas;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase;
import com.figueredo.ricardo.mvp.decisao.application.port.in.ConsultarAnaliseCulturaUseCase.FiltroAnaliseCultura;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase;
import com.figueredo.ricardo.mvp.zarc.application.port.in.ConsultarZoneamentoMunicipioUseCase.FiltroPeriodoSoloZoneamento;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ExecutorFerramentasAgroService implements AgroToolFunctionsExecutor {

    public static final String FERRAMENTA_ZONEAMENTO = "consultar_zoneamento_municipio";
    public static final String FERRAMENTA_RANKING = "calcular_ranking_oportunidades";
    public static final String FERRAMENTA_ANALISE = "consultar_analise_cultura";
    public static final String FERRAMENTA_COMPARACAO = "comparar_culturas";

    private final ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase;
    private final CalcularRankingOportunidadesUseCase calcularRankingUseCase;
    private final ConsultarAnaliseCulturaUseCase consultarAnaliseCulturaUseCase;
    private final CompararCulturasUseCase compararCulturasUseCase;
    private final ObjectMapper objectMapper;

    @Autowired
    public ExecutorFerramentasAgroService(
            ConsultarZoneamentoMunicipioUseCase consultarZoneamentoUseCase,
            CalcularRankingOportunidadesUseCase calcularRankingUseCase,
            ConsultarAnaliseCulturaUseCase consultarAnaliseCulturaUseCase,
            CompararCulturasUseCase compararCulturasUseCase,
            ObjectMapper objectMapper
    ) {
        this.consultarZoneamentoUseCase = Objects.requireNonNull(consultarZoneamentoUseCase);
        this.calcularRankingUseCase = Objects.requireNonNull(calcularRankingUseCase);
        this.consultarAnaliseCulturaUseCase = Objects.requireNonNull(consultarAnaliseCulturaUseCase);
        this.compararCulturasUseCase = Objects.requireNonNull(compararCulturasUseCase);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    @Override
    public List<DefinicaoFerramentaIa> obterDefinicoes() {
        return List.of(
                definirFerramentaZoneamento(),
                definirFerramentaRanking(),
                definirFerramentaAnalise(),
                definirFerramentaComparacao()
        );
    }

    @Override
    public ResultadoFerramentaIa executar(ChamadaFerramentaIa chamada) {
        Objects.requireNonNull(chamada, "chamada é obrigatória");
        String nome = chamada.nomeFerramenta();
        Map<String, Object> args = chamada.argumentos();

        try {
            return switch (nome) {
                case FERRAMENTA_ZONEAMENTO -> executarZoneamento(nome, args);
                case FERRAMENTA_RANKING -> executarRanking(nome, args);
                case FERRAMENTA_ANALISE -> executarAnalise(nome, args);
                case FERRAMENTA_COMPARACAO -> executarComparacao(nome, args);
                default -> ResultadoFerramentaIa.comErro(nome, "Ferramenta desconhecida: " + nome);
            };
        } catch (Exception ex) {
            return ResultadoFerramentaIa.comErro(nome, "Erro ao executar ferramenta: " + ex.getMessage());
        }
    }

    private ResultadoFerramentaIa executarZoneamento(String nome, Map<String, Object> args) throws JacksonException {
        String codigoIbge = extrairString(args, "codigoIbge");
        if (codigoIbge == null || codigoIbge.isBlank()) {
            return ResultadoFerramentaIa.comErro(nome, "codigoIbge é obrigatório");
        }
        String mesInicio = extrairString(args, "mesInicio");
        String mesFim = extrairString(args, "mesFim");
        String soloTexto = extrairString(args, "tipoSolo");

        PeriodoPlantioMeses periodo = resolverPeriodo(mesInicio, mesFim);
        Optional<TipoSoloZarc> tipoSoloOpt = resolverTipoSolo(soloTexto);

        Object resultado;
        if (tipoSoloOpt.isPresent()) {
            resultado = consultarZoneamentoUseCase.consultarAptasPorMunicipioPeriodoESolo(
                    new FiltroPeriodoSoloZoneamento(codigoIbge, periodo, tipoSoloOpt.get())
            );
        } else {
            resultado = consultarZoneamentoUseCase.consultarAptasPorMunicipioEPeriodo(codigoIbge, periodo);
        }

        return ResultadoFerramentaIa.comSucesso(nome, objectMapper.writeValueAsString(resultado));
    }

    private ResultadoFerramentaIa executarRanking(String nome, Map<String, Object> args) throws JacksonException {
        String codigoIbge = extrairString(args, "codigoIbge");
        if (codigoIbge == null || codigoIbge.isBlank()) {
            return ResultadoFerramentaIa.comErro(nome, "codigoIbge é obrigatório");
        }
        Integer safraIni = extrairInteger(args, "safraIni");
        Integer safraFim = extrairInteger(args, "safraFim");

        var resultado = calcularRankingUseCase.calcularRankingPorMunicipioESafra(
                new FiltroRankingOportunidades(codigoIbge, safraIni, safraFim)
        );

        return ResultadoFerramentaIa.comSucesso(nome, objectMapper.writeValueAsString(resultado));
    }

    private ResultadoFerramentaIa executarAnalise(String nome, Map<String, Object> args) throws JacksonException {
        String codigoIbge = extrairString(args, "codigoIbge");
        if (codigoIbge == null || codigoIbge.isBlank()) {
            return ResultadoFerramentaIa.comErro(nome, "codigoIbge é obrigatório");
        }
        Long culturaId = extrairLong(args, "culturaId");
        if (culturaId == null || culturaId <= 0) {
            return ResultadoFerramentaIa.comErro(nome, "culturaId é obrigatório e deve ser positivo");
        }
        String mesInicio = extrairString(args, "mesInicio");
        String mesFim = extrairString(args, "mesFim");
        String soloTexto = extrairString(args, "tipoSolo");

        PeriodoPlantioMeses periodo = resolverPeriodo(mesInicio, mesFim);
        TipoSoloZarc tipoSolo = resolverTipoSolo(soloTexto).orElse(null);

        var resultado = consultarAnaliseCulturaUseCase.consultarAnaliseCultura(
                new FiltroAnaliseCultura(codigoIbge, culturaId, periodo, tipoSolo)
        );

        return ResultadoFerramentaIa.comSucesso(nome, objectMapper.writeValueAsString(resultado));
    }

    private ResultadoFerramentaIa executarComparacao(String nome, Map<String, Object> args) throws JacksonException {
        String codigoIbge = extrairString(args, "codigoIbge");
        if (codigoIbge == null || codigoIbge.isBlank()) {
            return ResultadoFerramentaIa.comErro(nome, "codigoIbge é obrigatório");
        }
        List<Long> culturaIds = extrairListaLong(args, "culturaIds");
        if (culturaIds == null || culturaIds.size() < 2 || culturaIds.size() > 3) {
            return ResultadoFerramentaIa.comErro(nome, "culturaIds deve conter entre 2 e 3 culturas");
        }
        String mesInicio = extrairString(args, "mesInicio");
        String mesFim = extrairString(args, "mesFim");
        String soloTexto = extrairString(args, "tipoSolo");

        PeriodoPlantioMeses periodo = resolverPeriodo(mesInicio, mesFim);
        TipoSoloZarc tipoSolo = resolverTipoSolo(soloTexto).orElse(null);

        var resultado = compararCulturasUseCase.compararCulturas(
                new FiltroCompararCulturas(codigoIbge, periodo, tipoSolo, culturaIds)
        );

        return ResultadoFerramentaIa.comSucesso(nome, objectMapper.writeValueAsString(resultado));
    }

    private PeriodoPlantioMeses resolverPeriodo(String mesInicio, String mesFim) {
        return PeriodoPlantioMeses.resolverOuPadrao(mesInicio, mesFim, null);
    }

    private Optional<TipoSoloZarc> resolverTipoSolo(String soloTexto) {
        if (soloTexto == null || soloTexto.isBlank()) {
            return Optional.empty();
        }
        return TipoSoloZarc.buscarPorCodigoOuNome(soloTexto);
    }

    private String extrairString(Map<String, Object> args, String chave) {
        Object val = args.get(chave);
        return val != null ? val.toString().trim() : null;
    }

    private Integer extrairInteger(Map<String, Object> args, String chave) {
        Object val = args.get(chave);
        if (val == null) {
            return null;
        }
        if (val instanceof Number num) {
            return num.intValue();
        }
        return Integer.parseInt(val.toString().trim());
    }

    private Long extrairLong(Map<String, Object> args, String chave) {
        Object val = args.get(chave);
        if (val == null) {
            return null;
        }
        if (val instanceof Number num) {
            return num.longValue();
        }
        return Long.parseLong(val.toString().trim());
    }

    @SuppressWarnings("unchecked")
    private List<Long> extrairListaLong(Map<String, Object> args, String chave) {
        Object val = args.get(chave);
        if (val instanceof Collection<?> colecao) {
            return colecao.stream()
                    .map(item -> item instanceof Number num ? num.longValue() : Long.parseLong(item.toString().trim()))
                    .toList();
        }
        throw new IllegalArgumentException("Parâmetro " + chave + " deve ser uma lista de IDs numéricos");
    }

    private DefinicaoFerramentaIa definirFerramentaZoneamento() {
        return new DefinicaoFerramentaIa(
                FERRAMENTA_ZONEAMENTO,
                "Consulta o Zoneamento Agrícola de Risco Climático (ZARC - MAPA) de culturas aptas para um município em SC.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "codigoIbge", Map.of("type", "string", "description", "Código IBGE de 7 dígitos do município em Santa Catarina (ex: '4204809')"),
                                "mesInicio", Map.of("type", "string", "description", "Mês inicial no formato YYYY-MM (opcional)"),
                                "mesFim", Map.of("type", "string", "description", "Mês final no formato YYYY-MM (opcional)"),
                                "tipoSolo", Map.of("type", "string", "description", "Tipo de solo: 1 (arenoso), 2 (textura média), 3 (argiloso) (opcional)")
                        ),
                        "required", List.of("codigoIbge")
                )
        );
    }

    private DefinicaoFerramentaIa definirFerramentaRanking() {
        return new DefinicaoFerramentaIa(
                FERRAMENTA_RANKING,
                "Calcula o ranking indicativo de oportunidades do Motor de Decisão para as culturas no município, cruzando ZARC e preços.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "codigoIbge", Map.of("type", "string", "description", "Código IBGE de 7 dígitos do município em Santa Catarina (ex: '4204809')"),
                                "safraIni", Map.of("type", "integer", "description", "Ano inicial da safra, ex: 2024 (opcional)"),
                                "safraFim", Map.of("type", "integer", "description", "Ano final da safra, ex: 2025 (opcional)")
                        ),
                        "required", List.of("codigoIbge")
                )
        );
    }

    private DefinicaoFerramentaIa definirFerramentaAnalise() {
        return new DefinicaoFerramentaIa(
                FERRAMENTA_ANALISE,
                "Consulta a análise detalhada de uma cultura em um município (zoneamento ZARC, cotações Epagri/CONAB, histórico PAM).",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "codigoIbge", Map.of("type", "string", "description", "Código IBGE de 7 dígitos do município em Santa Catarina"),
                                "culturaId", Map.of("type", "integer", "description", "ID numérico da cultura agrícola (obtido na listagem de culturas ou ranking)"),
                                "mesInicio", Map.of("type", "string", "description", "Mês inicial no formato YYYY-MM (opcional)"),
                                "mesFim", Map.of("type", "string", "description", "Mês final no formato YYYY-MM (opcional)"),
                                "tipoSolo", Map.of("type", "string", "description", "Tipo de solo: 1, 2 ou 3 (opcional)")
                        ),
                        "required", List.of("codigoIbge", "culturaId")
                )
        );
    }

    private DefinicaoFerramentaIa definirFerramentaComparacao() {
        return new DefinicaoFerramentaIa(
                FERRAMENTA_COMPARACAO,
                "Compara de 2 a 3 culturas agrícolas para o mesmo município e período, confrontando riscos climáticos e preços.",
                Map.of(
                        "type", "object",
                        "properties", Map.of(
                                "codigoIbge", Map.of("type", "string", "description", "Código IBGE de 7 dígitos do município em Santa Catarina"),
                                "culturaIds", Map.of("type", "array", "items", Map.of("type", "integer"), "description", "Lista com 2 ou 3 IDs numéricos de culturas"),
                                "mesInicio", Map.of("type", "string", "description", "Mês inicial no formato YYYY-MM (opcional)"),
                                "mesFim", Map.of("type", "string", "description", "Mês final no formato YYYY-MM (opcional)"),
                                "tipoSolo", Map.of("type", "string", "description", "Tipo de solo: 1, 2 ou 3 (opcional)")
                        ),
                        "required", List.of("codigoIbge", "culturaIds")
                )
        );
    }
}
