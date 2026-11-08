package com.figueredo.ricardo.mvp.assistente.application.service;

import com.figueredo.ricardo.mvp.assistente.application.port.in.PerguntarAssistenteUseCase;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AgroToolFunctionsExecutor;
import com.figueredo.ricardo.mvp.assistente.application.port.out.AssistenteLlmPort;
import com.figueredo.ricardo.mvp.assistente.domain.model.ContextoProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.DefinicaoFerramentaIa;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import com.figueredo.ricardo.mvp.common.domain.exception.AssistenteQuotaExcedidaException;
import com.figueredo.ricardo.mvp.zarc.application.port.out.MunicipioPort;
import com.figueredo.ricardo.mvp.zarc.domain.model.Municipio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class AssistenteIaService implements PerguntarAssistenteUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AssistenteIaService.class);

    private static final Set<String> SAUDACOES = Set.of(
            "bom dia", "boa tarde", "boa noite",
            "ola", "oi", "opa", "e ai", "salve",
            "tudo bem", "como vai",
            "ola bom dia", "ola boa tarde", "ola boa noite",
            "oi bom dia", "oi boa tarde", "oi boa noite",
            "ola tudo bem", "oi tudo bem", "tudo bem com voce",
            "hello", "hi"
    );

    private final PromptConsultivoBuilder promptBuilder;
    private final AgroToolFunctionsExecutor toolExecutor;
    private final AssistenteLlmPort llmPort;
    private final MunicipioPort municipioPort;

    @Autowired
    public AssistenteIaService(
            PromptConsultivoBuilder promptBuilder,
            AgroToolFunctionsExecutor toolExecutor,
            AssistenteLlmPort llmPort,
            MunicipioPort municipioPort
    ) {
        this.promptBuilder = Objects.requireNonNull(promptBuilder);
        this.toolExecutor = Objects.requireNonNull(toolExecutor);
        this.llmPort = Objects.requireNonNull(llmPort);
        this.municipioPort = Objects.requireNonNull(municipioPort);
    }

    @Override
    public RespostaAssistente perguntar(PerguntaProdutor pergunta) {
        Objects.requireNonNull(pergunta, "pergunta é obrigatória");

        ContextoProdutor contexto = consolidarContexto(pergunta.contexto());
        PerguntaProdutor perguntaConsolidada = new PerguntaProdutor(pergunta.texto(), contexto);

        if (isSaudacao(pergunta.texto())) {
            return responderSaudacao(pergunta.texto(), contexto);
        }

        boolean temPergunta = contemPergunta(pergunta.texto());

        if (!contexto.possuiContextoMinimoCompleto()) {
            return responderContextoIncompleto(contexto);
        }

        if (!temPergunta) {
            return responderContextoCompletoSemPergunta(contexto);
        }

        try {
            String systemPrompt = promptBuilder.construirSystemPrompt();
            List<DefinicaoFerramentaIa> ferramentas = toolExecutor.obterDefinicoes();
            RespostaAssistente resLlm = llmPort.enviarMensagem(systemPrompt, perguntaConsolidada, ferramentas);
            return new RespostaAssistente(
                    resLlm.textoResposta(),
                    resLlm.ferramentasExecutadas(),
                    resLlm.fontesConsultadas(),
                    contexto
            );
        } catch (AssistenteQuotaExcedidaException ex) {
            logger.warn("Créditos ou cota da IA excedidos: {}", ex.getMessage());
            return new RespostaAssistente(
                    "Estamos temporariamente indisponíveis. Por favor, tente novamente amanhã.",
                    List.of(),
                    List.of(),
                    contexto
            );
        } catch (Exception ex) {
            logger.error("Falha ao comunicar com assistente LLM: {}", ex.getMessage(), ex);
            return new RespostaAssistente(
                    "O assistente inteligente está temporariamente indisponível no momento. Por favor, tente novamente mais tarde.",
                    List.of(),
                    List.of(),
                    contexto
            );
        }
    }

    private ContextoProdutor consolidarContexto(ContextoProdutor contexto) {
        if (contexto == null) {
            return new ContextoProdutor(null, null, null, null, null);
        }
        if (contexto.possuiMunicipio()) {
            if (contexto.nomeMunicipio() == null) {
                return municipioPort.buscarPorCodigoIbge(contexto.codigoIbge())
                        .map(m -> contexto.comMunicipio(m.codigoIbge(), m.nome()))
                        .orElse(contexto);
            }
            return contexto;
        }

        if (contexto.nomeMunicipio() != null && !contexto.nomeMunicipio().isBlank()) {
            Optional<Municipio> encontrado = buscarMunicipioPorNome(contexto.nomeMunicipio());
            if (encontrado.isPresent()) {
                return contexto.comMunicipio(encontrado.get().codigoIbge(), encontrado.get().nome());
            }
        }

        return contexto;
    }

    private Optional<Municipio> buscarMunicipioPorNome(String nome) {
        Optional<Municipio> direto = municipioPort.buscarPorNome(nome);
        if (direto.isPresent()) {
            return direto;
        }
        String normalizado = normalizarTexto(nome);
        return municipioPort.listarTodosOrdenadosPorNome().stream()
                .filter(m -> normalizarTexto(m.nome()).equals(normalizado))
                .findFirst();
    }

    private boolean isSaudacao(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        String normalizado = normalizarTexto(texto);
        return SAUDACOES.contains(normalizado);
    }

    private boolean contemPergunta(String texto) {
        if (texto == null || texto.isBlank()) {
            return false;
        }
        if (texto.contains("?")) {
            return true;
        }
        String norm = normalizarTexto(texto);
        return norm.contains("qual")
                || norm.contains("quais")
                || norm.contains("como")
                || norm.contains("o que")
                || norm.contains("quando")
                || norm.contains("quanto")
                || norm.contains("devo plantar")
                || norm.contains("posso plantar")
                || norm.contains("melhor cultura")
                || norm.contains("recomenda")
                || norm.contains("analise")
                || norm.contains("compare")
                || norm.contains("preco")
                || norm.contains("cotacao")
                || norm.contains("risco")
                || norm.contains("ranking")
                || norm.contains("oportunidade")
                || norm.contains("oportunidades")
                || norm.contains("plantar")
                || norm.contains("plantio")
                || norm.contains("cultura")
                || norm.contains("culturas")
                || norm.contains("sim")
                || norm.contains("quero")
                || norm.contains("ver")
                || norm.contains("mostrar")
                || norm.contains("ajuda");
    }

    private RespostaAssistente responderSaudacao(String texto, ContextoProdutor contexto) {
        String cumprimento = extrairCumprimento(texto);

        if (contexto.possuiContextoMinimoCompleto()) {
            String soloInfo = contexto.tipoSolo() != null ? " (" + contexto.tipoSolo().getTipoSolo() + ")" : "";
            String mensagem = String.format("""
                    %s Já estou com o contexto completo da sua lavoura em **%s** (%s ha, período de %s a %s%s) pronto para análise.

                    Como posso ajudar na sua tomada de decisão hoje?
                    """.stripIndent(),
                    cumprimento,
                    contexto.nomeMunicipio() != null ? contexto.nomeMunicipio() : "sua região",
                    contexto.areaHectares().stripTrailingZeros().toPlainString(),
                    contexto.periodoPlantio().mesInicio(),
                    contexto.periodoPlantio().mesFim(),
                    soloInfo
            ).trim();
            return new RespostaAssistente(mensagem, List.of(), List.of(), contexto);
        }

        String mensagem = cumprimento + " Sou o assistente do Agro Tool, especialista em Zoneamento Agrícola (ZARC) e cotações de Santa Catarina.\n\n"
                + "Para que eu possa analisar o Zoneamento Agrícola (ZARC), riscos climáticos e cotações para a sua lavoura, por favor selecione seu município, período de plantio e área nos filtros no topo da página e clique em **Consultar**.";

        return new RespostaAssistente(mensagem, List.of(), List.of(), contexto);
    }

    private RespostaAssistente responderContextoIncompleto(ContextoProdutor contexto) {
        String mensagem = "Olá! Para que eu possa analisar o Zoneamento Agrícola (ZARC), riscos climáticos e cotações para a sua lavoura, por favor selecione seu município, período de plantio e área nos filtros no topo da página e clique em **Consultar**.";
        return new RespostaAssistente(mensagem, List.of(), List.of(), contexto);
    }

    private RespostaAssistente responderContextoCompletoSemPergunta(ContextoProdutor contexto) {
        String soloInfo = contexto.tipoSolo() != null ? " (" + contexto.tipoSolo().getTipoSolo() + ")" : "";
        String mensagem = String.format("""
                Excelente! Contexto completo registrado:
                • **Cidade**: %s (SC)
                • **Área**: %s ha
                • **Período**: %s a %s%s

                Como posso ajudar? Você pode pedir por exemplo:
                👉 *"Ver ranking de culturas"*
                👉 *"Qual o risco climático para a soja?"*
                👉 *"Vale a pena plantar milho ou feijão?"*
                """.stripIndent(),
                contexto.nomeMunicipio() != null ? contexto.nomeMunicipio() : "sua região",
                contexto.areaHectares().stripTrailingZeros().toPlainString(),
                contexto.periodoPlantio().mesInicio(),
                contexto.periodoPlantio().mesFim(),
                soloInfo
        ).trim();
        return new RespostaAssistente(mensagem, List.of(), List.of(), contexto);
    }

    private String extrairCumprimento(String texto) {
        String normalizado = normalizarTexto(texto);
        if (normalizado.contains("bom dia")) {
            return "Bom dia!";
        }
        if (normalizado.contains("boa tarde")) {
            return "Boa tarde!";
        }
        if (normalizado.contains("boa noite")) {
            return "Boa noite!";
        }
        return "Olá!";
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
