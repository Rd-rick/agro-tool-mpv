package com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.mapper;

import com.figueredo.ricardo.mvp.assistente.domain.model.ContextoProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.PerguntaProdutor;
import com.figueredo.ricardo.mvp.assistente.domain.model.RespostaAssistente;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.ChamadaFerramentaResponse;
import com.figueredo.ricardo.mvp.assistente.infrastructure.adapter.in.web.dto.ContextoProdutorResponse;
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
        String ibge = request.codigoIbge() != null && !request.codigoIbge().isBlank()
                ? request.codigoIbge().trim()
                : null;

        String nomeMun = request.nomeMunicipio() != null && !request.nomeMunicipio().isBlank()
                ? request.nomeMunicipio().trim()
                : null;

        var contexto = new ContextoProdutor(
                ibge,
                nomeMun,
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

        ContextoProdutorResponse contextoResponse = null;
        if (domain.contextoConsolidado() != null) {
            ContextoProdutor c = domain.contextoConsolidado();
            String mesInicio = c.periodoPlantio() != null ? c.periodoPlantio().mesInicio().toString() : null;
            String mesFim = c.periodoPlantio() != null ? c.periodoPlantio().mesFim().toString() : null;
            String tipoSolo = c.tipoSolo() != null ? c.tipoSolo().getTipoSolo() : null;
            contextoResponse = new ContextoProdutorResponse(
                    c.codigoIbge(),
                    c.nomeMunicipio(),
                    mesInicio,
                    mesFim,
                    tipoSolo,
                    c.areaHectares()
            );
        }

        return new RespostaAssistenteResponse(
                domain.textoResposta(),
                ferramentas,
                domain.fontesConsultadas(),
                contextoResponse
        );
    }

    private PeriodoPlantioMeses resolverPeriodo(String mesInicio, String mesFim) {
        if ((mesInicio == null || mesInicio.isBlank()) && (mesFim == null || mesFim.isBlank())) {
            return null;
        }
        try {
            String inicioLimpo = mesInicio != null ? mesInicio.trim().replace('/', '-') : null;
            String fimLimpo = mesFim != null ? mesFim.trim().replace('/', '-') : null;
            return PeriodoPlantioMeses.resolverOuPadrao(inicioLimpo, fimLimpo, null);
        } catch (Exception ex) {
            return null;
        }
    }

    private TipoSoloZarc resolverTipoSolo(String tipoSoloTexto) {
        if (tipoSoloTexto == null || tipoSoloTexto.isBlank()) {
            return null;
        }
        return TipoSoloZarc.buscarPorCodigoOuNome(tipoSoloTexto).orElse(null);
    }
}
