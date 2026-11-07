package com.figueredo.ricardo.mvp.assistente.application.service;

import com.figueredo.ricardo.mvp.assistente.domain.model.ContextoProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class PromptConsultivoBuilder {

    public String construirSystemPrompt() {
        return """
                Você é o assistente inteligente consultivo do Agro Tool, especialista em apoio à decisão agrícola para o estado de Santa Catarina.
                Sua função é auxiliar produtores rurais a interpretar dados agrícolas e de mercado de forma responsável, clara e fundamentada.

                DIRETRIZES FUNDAMENTAIS DE COMPORTAMENTO:
                1. DIFERENCIAÇÃO OBRIGATÓRIA DE FONTES:
                   - Dados Oficiais: cite claramente a fonte ao falar de Zoneamento ZARC (MAPA), cotações agrícolas de SC (Epagri/Cepa), preços nacionais (CONAB) ou dados históricos municipais (IBGE/PAM).
                   - Cálculos Indicativos do Sistema: explique que scores e posições no ranking de oportunidades são calculados pelo Motor de Decisão do Agro Tool com base em regras consolidadas.
                   - Interpretação da IA: deixe claro quando estiver fazendo inferências consultivas ou orientações para reflexão do produtor.

                2. INTEGRIDADE DE DADOS E ÉTICA:
                   - NUNCA invente preços, safras, riscos climáticos, produtividades ou recomendações.
                   - Use sempre as ferramentas (functions/tools) disponíveis para consultar o backend antes de responder sobre um município ou cultura.
                   - JAMAIS afirme ou prometa rentabilidade financeira. Preço de venda elevado NÃO é sinônimo de lucro. Explique que rentabilidade líquida depende de produtividade real, custos de insumos, maquinário, combustível e frete.
                   - Não trate cotações da Epagri/Cepa e da CONAB como equivalentes diretas sem ressalvar suas respectivas praças e unidades de comercialização.
                   - Se os dados disponíveis forem insuficientes para uma conclusão, declare a limitação com total transparência e recomende consulta a um engenheiro agrônomo ou técnico da Epagri.

                3. ESTILO DE RESPOSTA:
                   - Seja acessível, empático, objetivo e profissional com o produtor rural.
                   - Estruture sua resposta com tópicos organizados quando envolver análises comparativas ou múltiplos fatores.
                   - Você apoia a decisão do produtor, nunca decide por ele.
                """.stripIndent();
    }

    public String construirPromptUsuario(PerguntaProdutor pergunta) {
        Objects.requireNonNull(pergunta, "pergunta é obrigatória");
        ContextoProdutor contexto = pergunta.contexto();

        var builder = new StringBuilder();
        builder.append("Contexto do Produtor:\n");
        builder.append("- Município (Código IBGE): ").append(contexto.codigoIbge()).append("\n");

        if (contexto.periodoPlantio() != null) {
            builder.append("- Período de interesse: ")
                    .append(contexto.periodoPlantio().mesInicio())
                    .append(" até ")
                    .append(contexto.periodoPlantio().mesFim())
                    .append("\n");
        }

        if (contexto.tipoSolo() != null) {
            builder.append("- Tipo de solo: ")
                    .append(contexto.tipoSolo().getDescricaoCompleta())
                    .append("\n");
        }

        if (contexto.areaHectares() != null) {
            builder.append("- Área disponível: ")
                    .append(contexto.areaHectares())
                    .append(" hectares\n");
        }

        builder.append("\nPergunta do produtor:\n");
        builder.append(pergunta.texto());

        return builder.toString();
    }
}
