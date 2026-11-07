package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.assistente.domain.model.ContextoProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.ChamadaFerramentaResponse;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.PerguntaAssistenteRequest;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.RespostaAssistenteResponse;
import com.figueredo.ricardo.mvp.zarc.domain.model.PeriodoPlantioMeses;
import com.figueredo.ricardo.mvp.zarc.domain.model.TipoSoloZarc;
import org.springframework.stereotype.Component;

@Component
public class AssistenteWebMapper {

    public PerguntaProdutor toDomain(PerguntaAssistenteRequest request) {
        PeriodoPlantioMeses periodo = resolverPeriodo(request.mesInicio(), request.mesFim());
        TipoSoloZarc tipoSolo = resolverTipoSolo(request.tipoSolo());

        var contexto = new ContextoProdutor(
                request.codigoIbge(),
                periodo,
                tipoSolo,
                request.areaHectares()
        );
        return new PerguntaProdutor(request.texto(), contexto);
    }

    public RespostaAssistenteResponse toResponse(RespostaAssistente domain) {
        var ferramentas = domain.ferramentasExecutadas().stream()
                .map(f -> new ChamadaFerramentaResponse(f.nomeFerramenta(), f.argumentos()))
                .toList();

        return new RespostaAssistenteResponse(
                domain.textoResposta(),
                ferramentas,
                domain.fontesConsultadas()
        );
    }

    private PeriodoPlantioMeses resolverPeriodo(String mesInicio, String mesFim) {
        if ((mesInicio == null || mesInicio.isBlank()) && (mesFim == null || mesFim.isBlank())) {
            return null;
        }
        return PeriodoPlantioMeses.resolverOuPadrao(mesInicio, mesFim, null);
    }

    private TipoSoloZarc resolverTipoSolo(String tipoSoloTexto) {
        if (tipoSoloTexto == null || tipoSoloTexto.isBlank()) {
            return null;
        }
        return TipoSoloZarc.buscarPorCodigoOuNome(tipoSoloTexto).orElse(null);
    }
}
